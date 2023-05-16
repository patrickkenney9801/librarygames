package util

import (
  "context"
  "fmt"

  "google.golang.org/grpc/metadata"
)

func GetGRPCMetadata(ctx context.Context) (metadata.MD, error) {
  md, ok := metadata.FromIncomingContext(ctx)
  if !ok {
    return nil, fmt.Errorf("metadata is not provided")
  }
  return md, nil
}

func GetGRPCUsername(ctx context.Context) (string, error) {
  md, err := GetGRPCMetadata(ctx)
  if err != nil {
    return "", err
  }
  userValues := md["username"]
  if len(userValues) == 0 {
    return "", fmt.Errorf("username is not provided")
  }
  return userValues[0], nil
}

func GetGRPCAccessToken(ctx context.Context) (string, error) {
  md, err := GetGRPCMetadata(ctx)
  if err != nil {
    return "", err
  }
  authValues := md["authorization"]
  if len(authValues) == 0 {
    return "", fmt.Errorf("authorization token is not provided")
  }
  return authValues[0], nil
}
