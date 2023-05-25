package account

import (
	"context"

	pbs "github.com/patrickkenney9801/librarygames/internal/pbs/v1"
	"github.com/patrickkenney9801/librarygames/internal/util"

	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

type GetUsersServer struct {
	pbs.UnimplementedGetUsersServer

	userManager *UserManager
}

func (s *GetUsersServer) GetUsers(ctx context.Context, getUsersRequest *pbs.GetUsersRequest) (*pbs.GetUsersResponse, error) {
	username, err := util.GetGRPCUsername(ctx)
	if err != nil {
		return &pbs.GetUsersResponse{}, status.Error(codes.Unauthenticated, err.Error())
	}
	peerUsers, err := s.userManager.getUsers(ctx, username)
	if err != nil {
		return &pbs.GetUsersResponse{}, status.Error(codes.InvalidArgument, err.Error())
	}

	var users []*pbs.GetUsersResponse_PeerUser
	for _, user := range peerUsers {
		users = append(users, &pbs.GetUsersResponse_PeerUser{
			Username: user.username,
			Online:   user.online,
			Friend:   user.friend,
		})
	}

	return &pbs.GetUsersResponse{
		Users: users,
	}, nil
}

func NewGetUsersServer(userManager *UserManager) *GetUsersServer {
	return &GetUsersServer{
		userManager: userManager,
	}
}
