package game

import (
	"context"

	pbs "github.com/patrickkenney9801/librarygames/internal/pbs/v1"
	"github.com/patrickkenney9801/librarygames/internal/util"

	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

type GetGamesServer struct {
	pbs.UnimplementedGetGamesServer

	gameManager *GameManager
}

func (s *GetGamesServer) GetGames(ctx context.Context, getGamesRequest *pbs.GetGamesRequest) (*pbs.GetGamesResponse, error) {
	username, err := util.GetGRPCUsername(ctx)
	if err != nil {
		return &pbs.GetGamesResponse{}, status.Error(codes.Unauthenticated, err.Error())
	}
	userGames, err := s.gameManager.getGames(ctx, username)
	if err != nil {
		return &pbs.GetGamesResponse{}, status.Error(codes.InvalidArgument, err.Error())
	}

	var games []*pbs.GameMetadata
	for _, game := range userGames {
		games = append(games, &pbs.GameMetadata{
			GameType: util.GetGameType(game.GameType),
			GameKey:  game.GameKey,
			User1:    game.User1,
			User2:    game.User2,
			Moves:    game.Moves,
			Winner:   game.Winner,
		})
	}

	return &pbs.GetGamesResponse{
		Games: games,
	}, nil
}

func NewGetGamesServer(gameManager *GameManager) *GetGamesServer {
	return &GetGamesServer{
		gameManager: gameManager,
	}
}
