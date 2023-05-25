package game

import (
	"context"

	pbs "github.com/patrickkenney9801/librarygames/internal/pbs/v1"
	"github.com/patrickkenney9801/librarygames/internal/util"

	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

type CreateGameServer struct {
	pbs.UnimplementedCreateGameServer

	gameManager *GameManager
}

func (s *CreateGameServer) CreateGame(ctx context.Context, createGameRequest *pbs.CreateGameRequest) (*pbs.CreateGameResponse, error) {
	username, err := util.GetGRPCUsername(ctx)
	if err != nil {
		return &pbs.CreateGameResponse{}, status.Error(codes.InvalidArgument, err.Error())
	}

	switch gameType := createGameRequest.GameType; gameType {
	case pbs.GameType_GO_9X9:
		board := createBoard(9 * 9)
		gameKey, err := s.gameManager.CreateGoGame(ctx, util.GetIntGameType(createGameRequest.GameType), username, createGameRequest.OtherUser, createGameRequest.GetGo().CreatorGoesFirst, board)
		if err != nil {
			return &pbs.CreateGameResponse{}, status.Error(codes.InvalidArgument, err.Error())
		}
		return &pbs.CreateGameResponse{
			GameType: gameType,
			GameKey:  gameKey,
			GameData: &pbs.CreateGameResponse_Go{},
		}, nil
	case pbs.GameType_GO_13X13:
		board := createBoard(13 * 13)
		gameKey, err := s.gameManager.CreateGoGame(ctx, util.GetIntGameType(createGameRequest.GameType), username, createGameRequest.OtherUser, createGameRequest.GetGo().CreatorGoesFirst, board)
		if err != nil {
			return &pbs.CreateGameResponse{}, status.Error(codes.InvalidArgument, err.Error())
		}
		return &pbs.CreateGameResponse{
			GameType: gameType,
			GameKey:  gameKey,
			GameData: &pbs.CreateGameResponse_Go{},
		}, nil
	case pbs.GameType_GO_19X19:
		board := createBoard(19 * 19)
		gameKey, err := s.gameManager.CreateGoGame(ctx, util.GetIntGameType(createGameRequest.GameType), username, createGameRequest.OtherUser, createGameRequest.GetGo().CreatorGoesFirst, board)
		if err != nil {
			return &pbs.CreateGameResponse{}, status.Error(codes.InvalidArgument, err.Error())
		}
		return &pbs.CreateGameResponse{
			GameType: gameType,
			GameKey:  gameKey,
			GameData: &pbs.CreateGameResponse_Go{},
		}, nil
	default:
		return &pbs.CreateGameResponse{}, status.Error(codes.InvalidArgument, "invalid game type")
	}
}

func NewCreateGameServer(gameManager *GameManager) *CreateGameServer {
	return &CreateGameServer{
		gameManager: gameManager,
	}
}

func createBoard(size int) string {
	board := make([]byte, size)
	for i := range board {
		board[i] = '0'
	}
	return string(board)
}
