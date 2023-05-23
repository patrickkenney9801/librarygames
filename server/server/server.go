package server

import (
  "context"
  "fmt"
  "net"
  "net/http"
  "runtime/debug"
  "strconv"
  "strings"

  "github.com/patrickkenney9801/librarygames/internal/chat"
  "github.com/patrickkenney9801/librarygames/internal/config"
  "github.com/patrickkenney9801/librarygames/internal/database"
  "github.com/patrickkenney9801/librarygames/internal/game"
  pbs "github.com/patrickkenney9801/librarygames/internal/pbs/v1"
  "github.com/patrickkenney9801/librarygames/metrics"

  "github.com/patrickkenney9801/librarygames/internal/account"

  "github.com/gin-contrib/pprof"
  "github.com/gin-gonic/gin"
  grpcprom "github.com/grpc-ecosystem/go-grpc-middleware/providers/prometheus"
  "github.com/grpc-ecosystem/go-grpc-middleware/v2/interceptors/logging"
  "github.com/grpc-ecosystem/go-grpc-middleware/v2/interceptors/recovery"
  "github.com/prometheus/client_golang/prometheus"
  "github.com/prometheus/client_golang/prometheus/promauto"
  "go.opentelemetry.io/contrib/instrumentation/google.golang.org/grpc/otelgrpc"
  "go.opentelemetry.io/contrib/instrumentation/google.golang.org/grpc/otelgrpc/filters"
  "go.opentelemetry.io/otel"
  "go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracegrpc"
  "go.opentelemetry.io/otel/sdk/resource"
  tracesdk "go.opentelemetry.io/otel/sdk/trace"
  semconv "go.opentelemetry.io/otel/semconv/v1.17.0"
  "go.opentelemetry.io/otel/trace"
  "golang.org/x/exp/slog"
  "golang.org/x/net/http2"
  "golang.org/x/net/http2/h2c"
  "google.golang.org/grpc"
  "google.golang.org/grpc/codes"
  "google.golang.org/grpc/health"
  healthpb "google.golang.org/grpc/health/grpc_health_v1"
  "google.golang.org/grpc/status"
)

const (
  librarygamesServiceName = "librarygames"
)

type Server struct {
  cfg     *config.Config
  version string

  database      database.Database
  grpcSrv       *grpc.Server
  listener      net.Listener
  traceProvider *tracesdk.TracerProvider
  metrics       *metrics.Client

  userManager *account.UserManager
  chatManager *chat.ChatManager
  gameManager *game.GameManager
}

func NewServer(ctx context.Context, cfg *config.Config, version string) (*Server, error) {
  server := &Server{
    cfg:     cfg,
    version: version,
  }

  database, err := database.NewMsqlBackend(cfg)
  if err != nil {
    return nil, err
  }
  server.database = database

  gin.SetMode(gin.ReleaseMode)
  router := gin.Default()

  setupPprof(router)

  traceProvider, err := setupTracing(ctx, cfg, version)
  if err != nil {
    return nil, err
  }
  server.traceProvider = traceProvider

  srvMetrics := grpcprom.NewServerMetrics(
    grpcprom.WithServerHandlingTimeHistogram(
      grpcprom.WithHistogramBuckets([]float64{0.001, 0.01, 0.1, 0.3, 0.6, 1, 3, 6, 9, 20, 30, 60, 90, 120}),
    ),
  )

  metrics, err := setupMetrics(router, srvMetrics)
  if err != nil {
    return nil, err
  }
  server.metrics = metrics

  userManager, err := account.NewUserManager(database)
  if err != nil {
    return nil, err
  }
  server.userManager = userManager

  server.chatManager = chat.NewChatManager(database, server.metrics)

  gameManager, err := game.NewGameManager(database)
  if err != nil {
    return nil, err
  }
  server.gameManager = gameManager

  grpcSrv, err := setupGrpc(server.metrics.Registry(), srvMetrics, server.userManager)
  if err != nil {
    return nil, err
  }

  // Register protobufs
  healthpb.RegisterHealthServer(grpcSrv, health.NewServer())

  pbs.RegisterLoginServer(grpcSrv, account.NewLoginServer(userManager))
  pbs.RegisterCreateAccountServer(grpcSrv, account.NewCreateAccountServer(userManager))

  pbs.RegisterLogoutServer(grpcSrv, account.NewLogoutServer(userManager))
  pbs.RegisterGetUsersServer(grpcSrv, account.NewGetUsersServer(userManager))
  pbs.RegisterAddFriendServer(grpcSrv, account.NewAddFriendServer(userManager))

  pbs.RegisterChatServer(grpcSrv, chat.NewChatServer(server.chatManager))

  pbs.RegisterCreateGameServer(grpcSrv, game.NewCreateGameServer(server.gameManager))
  pbs.RegisterGetGamesServer(grpcSrv, game.NewGetGamesServer(server.gameManager))
  pbs.RegisterGetSpectatorGamesServer(grpcSrv, game.NewGetSpectatorGamesServer(server.gameManager))
  pbs.RegisterGoServer(grpcSrv, game.NewGameGoServer(server.gameManager))

  listener, err := makeListener(cfg.Server.Port)
  if err != nil {
    return nil, err
  }

  if err = http.Serve(listener, newHTTP2Handler(router, grpcSrv)); err != nil {
    return nil, err
  }

  server.grpcSrv = grpcSrv
  server.listener = listener

  return server, nil
}

func (s *Server) Close(ctx context.Context) {
  s.grpcSrv.GracefulStop()
  s.grpcSrv.Stop()
  s.listener.Close()
  s.traceProvider.Shutdown(ctx)
}

type http2Handler struct {
  h2s *http2.Server

  handler http.Handler
}

func newHTTP2Handler(httpHandler http.Handler, grpcServer *grpc.Server) http.Handler {
  h := http2Handler{
    h2s: &http2.Server{},
  }

  interceptGRPC := func(w http.ResponseWriter, r *http.Request) {
    if grpcServer != nil && r.ProtoMajor == 2 && strings.Contains(r.Header.Get("Content-Type"), "application/grpc") {
      grpcServer.ServeHTTP(w, r)
    } else {
      httpHandler.ServeHTTP(w, r)
    }
  }

  h.handler = h2c.NewHandler(http.HandlerFunc(interceptGRPC), h.h2s)

  return h
}

func (h http2Handler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
  h.handler.ServeHTTP(w, r)
}

func makeListener(port int) (net.Listener, error) {
  listener, err := net.Listen("tcp", ":"+strconv.Itoa(port))
  if err != nil {
    return nil, err
  }
  slog.Info("server started and listening", slog.Int("port", port))
  return listener, nil
}

func interceptorLogger(l *slog.Logger) logging.Logger {
  return logging.LoggerFunc(func(_ context.Context, lvl logging.Level, msg string, fields ...any) {
    largs := append([]any{"msg", msg}, fields...)
    switch lvl {
    case logging.LevelDebug:
      slog.Debug(msg, largs...)
    case logging.LevelInfo:
      slog.Info(msg, largs...)
    case logging.LevelWarn:
      slog.Warn(msg, largs...)
    case logging.LevelError:
      slog.Error(msg, largs...)
    default:
      slog.Warn(msg, slog.String("unknown_log_level", fmt.Sprintf("%d", lvl)))
    }
  })
}

func setupGrpc(registry *prometheus.Registry, srvMetrics *grpcprom.ServerMetrics, userManager *account.UserManager) (*grpc.Server, error) {
  logTraceID := func(ctx context.Context) logging.Fields {
    if span := trace.SpanContextFromContext(ctx); span.IsSampled() {
      return logging.Fields{"traceID", span.TraceID().String()}
    }
    return nil
  }

  exemplarFromContext := func(ctx context.Context) prometheus.Labels {
    if span := trace.SpanContextFromContext(ctx); span.IsSampled() {
      return prometheus.Labels{"traceID": span.TraceID().String()}
    }
    return nil
  }

  rpcLogger := slog.With(slog.String("service", "gRPC/server"))

  // Setup metric for panic recoveries.
  panicsTotal := promauto.With(registry).NewCounter(prometheus.CounterOpts{
    Name: "grpc_req_panics_recovered_total",
    Help: "Total number of gRPC requests recovered from internal panic.",
  })
  grpcPanicRecoveryHandler := func(p any) (err error) {
    panicsTotal.Inc()
    rpcLogger.Error("recovered from panic", slog.String("panic", fmt.Sprintf("%v", p)), slog.String("stack", string(debug.Stack())))
    return status.Errorf(codes.Internal, "%s", p)
  }

  grpcSrv := grpc.NewServer(
    grpc.ChainUnaryInterceptor(
      // Order matters e.g. tracing interceptor have to create span first for the later exemplars to work.
      userManager.UnaryAuthIntercepter(),
      otelgrpc.UnaryServerInterceptor(
        otelgrpc.WithInterceptorFilter(
          filters.Not(
            filters.HealthCheck(),
          ),
        ),
      ),
      srvMetrics.UnaryServerInterceptor(grpcprom.WithExemplarFromContext(exemplarFromContext)),
      logging.UnaryServerInterceptor(interceptorLogger(rpcLogger), logging.WithFieldsFromContext(logTraceID)),
      recovery.UnaryServerInterceptor(recovery.WithRecoveryHandler(grpcPanicRecoveryHandler)),
    ),
    grpc.ChainStreamInterceptor(
      userManager.StreamAuthIntercepter(),
      otelgrpc.StreamServerInterceptor(),
      srvMetrics.StreamServerInterceptor(grpcprom.WithExemplarFromContext(exemplarFromContext)),
      logging.StreamServerInterceptor(interceptorLogger(rpcLogger), logging.WithFieldsFromContext(logTraceID)),
      recovery.StreamServerInterceptor(recovery.WithRecoveryHandler(grpcPanicRecoveryHandler)),
    ),
  )
  srvMetrics.InitializeMetrics(grpcSrv)
  return grpcSrv, nil
}

func setupMetrics(router *gin.Engine, promCollectors ...prometheus.Collector) (*metrics.Client, error) {
  metrics, err := metrics.New(prometheus.NewRegistry(), promCollectors...)
  if err != nil {
    return nil, err
  }
  router.GET("/metrics", metrics.Handler())
  return metrics, nil
}

func setupTracing(ctx context.Context, cfg *config.Config, version string) (*tracesdk.TracerProvider, error) {
  spanExporter, err := otlptracegrpc.New(ctx, otlptracegrpc.WithInsecure(), otlptracegrpc.WithEndpoint(cfg.Observability.Trace.CollectorEndpoint))
  if err != nil {
    return nil, err
  }
  traceResource, err := resource.Merge(
    resource.Default(),
    resource.NewWithAttributes(
      semconv.SchemaURL,
      semconv.ServiceNameKey.String(librarygamesServiceName),
      semconv.ServiceVersionKey.String(version),
    ),
  )
  if err != nil {
    return nil, err
  }
  traceProvider := tracesdk.NewTracerProvider(tracesdk.WithBatcher(spanExporter), tracesdk.WithResource(traceResource), tracesdk.WithSampler(tracesdk.AlwaysSample()))
  otel.SetTracerProvider(traceProvider)
  return traceProvider, nil
}

func setupPprof(router *gin.Engine) {
  pprof.Register(router, "debug/pprof")
}
