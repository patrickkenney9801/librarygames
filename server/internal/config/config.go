package config

import (
  "bytes"
  "encoding/json"
  "os"
  "strconv"

  "github.com/ghodss/yaml"
  "github.com/pkg/errors"
)

const (
  defaultConfigFile = "/etc/librarygames/config.yaml"
)

type TraceConfig struct {
  CollectorEndpoint string `json:"collectorEndpoint,omitempty"`
}

type ObservabilityConfig struct {
  Trace *TraceConfig `json:"trace,omitempty"`
}

type DatabaseConfig struct {
  Address  string `json:"address,omitempty"`
  Name     string `json:"name,omitempty"`
  Username string `json:"username,omitempty"`
  Password string `json:"password,omitempty"`
}

type ServerConfig struct {
  Port int `json:"port,omitempty"`
}

type Config struct {
  Database      *DatabaseConfig      `json:"database,omitempty"`
  Server        *ServerConfig        `json:"server,omitempty"`
  Observability *ObservabilityConfig `json:"observability,omitempty"`
}

func Load() (*Config, error) {
  var cfg Config

  fileName := defaultConfigFile
  if p := os.Getenv("LIBRARYGAMES_CONFIG_FILE"); len(p) != 0 {
    fileName = p
  }

  if err := loadYAMLConfig(fileName, &cfg); err != nil {
    return nil, errors.Wrap(err, "parsing config file")
  }

  if cfg.Server == nil {
    cfg.Server = &ServerConfig{}
    if p := os.Getenv("LIBRARYGAMES_GRPC_PORT"); len(p) != 0 {
      port, err := strconv.Atoi(p)
      if err != nil {
        return nil, errors.Wrap(err, "invalid gRPC port")
      }
      cfg.Server.Port = port
    }
  }

  return &cfg, nil
}

func loadYAMLConfig(fileName string, obj interface{}) error {
  contents, err := os.ReadFile(fileName)
  if err != nil {
    return err
  }

  jsonData, err := yaml.YAMLToJSON(contents)
  if err != nil {
    return errors.WithMessage(err, "parsing YAML")
  }

  buf := bytes.NewBuffer(jsonData)
  dec := json.NewDecoder(buf)
  dec.DisallowUnknownFields()
  return dec.Decode(obj)
}
