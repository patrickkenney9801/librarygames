package metrics

import (
  "fmt"
  "regexp"

  "github.com/gin-gonic/gin"
  "github.com/prometheus/client_golang/prometheus"
  "github.com/prometheus/client_golang/prometheus/collectors"
  "github.com/prometheus/client_golang/prometheus/promauto"
  "github.com/prometheus/client_golang/prometheus/promhttp"
  "golang.org/x/exp/slog"
)

const (
  librarygamesPrefix = "librarygames_"

  ChatMessagesDroppedCounter = "chat_messages_dropped_total"
)

var (
  counters = map[string]string{
    ChatMessagesDroppedCounter: "messages dropped by chat handler once buffer is filled",
  }
)

type Client struct {
  reg *prometheus.Registry

  counters map[string]prometheus.Counter
}

func New(registry *prometheus.Registry, promCollectors ...prometheus.Collector) (*Client, error) {

  for _, collector := range promCollectors {
    if err := registry.Register(collector); err != nil {
      slog.Warn("failed to register metrics collector")
    }
  }

  // enabledMetrics selects the metrics classes which prometheus will export by default.
  // Note: prometheus is ultimately exporting the runtime metrics from the go runtime (listed here https://golang.bg/src/runtime/metrics/description.go)
  // We enable all three present classes (gc, memory, sched) explicitly to avoid enabling them all by default (collectors.MetricsAll) to avoid surprises
  // when we upgrade our go packages and deps. So, instead, new metrics -- should they ever appear -- will necessitate an extension to enabledMetrics.
  // Note: using the matchers exported by prometheus.collectors is for convenience and we can always create our own metrics rule by simply writing our regex matcher.
  enabledGoCollectorMetrics := []collectors.GoRuntimeMetricsRule{
    {
      Matcher: collectors.MetricsGC.Matcher,
    },
    {
      Matcher: collectors.MetricsMemory.Matcher,
    },
    {
      Matcher: collectors.MetricsScheduler.Matcher,
    },
  }
  // disabledMetrics can introduce deny-list type control allowing us to disable any specific metric we like (again from https://golang.bg/src/runtime/metrics/description.go)
  // by writing the regexp matcher which matches the denied  metric
  disabledGoCollectorMetrics := []*regexp.Regexp{}
  controllerMetricsCollectors := map[string]prometheus.Collector{
    // note order matters: enable the classes then disable the individual metrics we wish to filter
    "controller goruntime": collectors.NewGoCollector(collectors.WithGoCollectorRuntimeMetrics(enabledGoCollectorMetrics...), collectors.WithoutGoCollectorRuntimeMetrics(disabledGoCollectorMetrics...)),
    "controller process":   collectors.NewProcessCollector(collectors.ProcessCollectorOpts{}), // the process metrics exports process-level metrics on the current pid with information about fds, cpu time, memory, and process start time
  }
  for kind, collector := range controllerMetricsCollectors {
    if err := registry.Register(collector); err != nil {
      slog.Warn("failed to register metrics collector", slog.String("collector", kind))
    }
  }

  client := &Client{
    reg: registry,

    counters: make(map[string]prometheus.Counter),
  }

  factory := promauto.With(registry)
  for name, help := range counters {
    client.counters[name] = factory.NewCounter(prometheus.CounterOpts{
      Name: fmt.Sprintf("%s%s", librarygamesPrefix, name),
      Help: help,
    })
  }
  return client, nil
}

func (c *Client) Handler() gin.HandlerFunc {
  return gin.WrapH(promhttp.HandlerFor(c.reg, promhttp.HandlerOpts{
    Registry:          c.reg,
    EnableOpenMetrics: true,
  }))
}

func (c *Client) Registry() *prometheus.Registry {
  return c.reg
}

func (c *Client) GetCounter(name string) prometheus.Counter {
  counter, ok := c.counters[name]
  if !ok {
    panic(fmt.Errorf("attempted to access counter \"%s%s\", which was not registered", librarygamesPrefix, name))
  }
  return counter
}
