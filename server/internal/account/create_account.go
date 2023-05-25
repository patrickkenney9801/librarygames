package account

import (
	"context"

	pbs "github.com/patrickkenney9801/librarygames/internal/pbs/v1"

	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

type CreateAccountServer struct {
	pbs.UnimplementedCreateAccountServer

	userManager *UserManager
}

func (s *CreateAccountServer) CreateAccount(ctx context.Context, createAccountRequest *pbs.CreateAccountRequest) (*pbs.CreateAccountResponse, error) {
	if err := s.userManager.createAccount(ctx, createAccountRequest.Username, createAccountRequest.Password, createAccountRequest.Email); err != nil {
		return &pbs.CreateAccountResponse{}, status.Error(codes.InvalidArgument, err.Error())
	}

	return &pbs.CreateAccountResponse{}, nil
}

func NewCreateAccountServer(userManager *UserManager) *CreateAccountServer {
	return &CreateAccountServer{
		userManager: userManager,
	}
}
