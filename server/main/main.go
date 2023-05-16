package main

import (
  "context"
  "os"
  "time"

  "github.com/patrickkenney9801/librarygames/internal/config"
  "github.com/patrickkenney9801/librarygames/server"

  "golang.org/x/exp/slog"
)

// This is set by the go build -ldflags "-X main.version=<value>"
var version = "unknown"

func main() {
  ctx, cancel := context.WithTimeout(context.Background(), time.Second*10)
  defer cancel()

  initLogging()

  cfg, err := config.Load()
  if err != nil {
    slog.Error("could not load config")
    panic(err)
  }

  server, err := server.NewServer(ctx, cfg, version)
  if err != nil {
    slog.Error("could not create server")
    panic(err)
  }
  defer server.Close(context.Background())
}

func initLogging() {
  jsonHandler := slog.NewJSONHandler(os.Stdout, nil)
  logger := slog.New(jsonHandler)
  slog.SetDefault(logger)
}
