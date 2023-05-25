package account

import (
	"context"

	pbs "github.com/patrickkenney9801/librarygames/internal/pbs/v1"
	"github.com/patrickkenney9801/librarygames/internal/util"

	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

type AddFriendServer struct {
	pbs.UnimplementedAddFriendServer

	userManager *UserManager
}

func (s *AddFriendServer) AddFriend(ctx context.Context, addFriendRequest *pbs.AddFriendRequest) (*pbs.AddFriendResponse, error) {
	username, err := util.GetGRPCUsername(ctx)
	if err != nil {
		return &pbs.AddFriendResponse{}, status.Error(codes.Unauthenticated, err.Error())
	}
	if err := s.userManager.addFriend(ctx, username, addFriendRequest.Friend); err != nil {
		return &pbs.AddFriendResponse{}, status.Error(codes.Internal, err.Error())
	}
	return &pbs.AddFriendResponse{}, nil
}

func NewAddFriendServer(userManager *UserManager) *AddFriendServer {
	return &AddFriendServer{
		userManager: userManager,
	}
}
