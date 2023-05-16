package account

import (
  "context"

  pbs "github.com/patrickkenney9801/librarygames/internal/pbs/v1"
  "github.com/patrickkenney9801/librarygames/internal/util"

  "google.golang.org/grpc/codes"
  "google.golang.org/grpc/status"
)

type LogoutServer struct {
  pbs.UnimplementedLogoutServer

  userManager *UserManager
}

func (s *LogoutServer) LogoutServer(ctx context.Context, logoutRequest *pbs.LogoutRequest) (*pbs.LogoutResponse, error) {
  username, err := util.GetGRPCUsername(ctx)
  if err != nil {
    return &pbs.LogoutResponse{}, status.Error(codes.Unauthenticated, err.Error())
  }
  if err := s.userManager.logout(username); err != nil {
    return &pbs.LogoutResponse{}, status.Error(codes.Internal, err.Error())
  }
  return &pbs.LogoutResponse{}, nil
}

func NewLogoutServer(userManager *UserManager) *LogoutServer {
  return &LogoutServer{
    userManager: userManager,
  }
}
