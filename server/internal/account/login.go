package account

import (
	"context"
	"sync"

	pbs "github.com/patrickkenney9801/librarygames/internal/pbs/v1"

	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

type LoginServer struct {
	pbs.UnimplementedLoginServer

	mu          sync.Mutex
	userManager *UserManager
}

func (s *LoginServer) Login(ctx context.Context, loginRequest *pbs.LoginRequest) (*pbs.LoginResponse, error) {
	s.mu.Lock()
	defer s.mu.Unlock()

	user, err := s.userManager.login(ctx, loginRequest.Username, loginRequest.Password)
	if err != nil {
		return &pbs.LoginResponse{}, status.Error(codes.PermissionDenied, err.Error())
	}

	return &pbs.LoginResponse{
		Username:  loginRequest.Username,
		UserToken: user.token.token,
	}, nil
}

func NewLoginServer(userManager *UserManager) *LoginServer {
	return &LoginServer{
		userManager: userManager,
	}
}
