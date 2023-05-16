package game

import (
  "context"

  pbs "github.com/patrickkenney9801/librarygames/internal/pbs/v1"
  "github.com/patrickkenney9801/librarygames/internal/util"

  "google.golang.org/grpc/codes"
  "google.golang.org/grpc/status"
)

type GetSpectatorGamesServer struct {
  pbs.UnimplementedGetSpectatorGamesServer

  gameManager *GameManager
}

func (s *GetGamesServer) GetSpectatorGames(ctx context.Context, getSpectatorGamesRequest *pbs.GetSpectatorGamesRequest) (*pbs.GetSpectatorGamesResponse, error) {
  username, err := util.GetGRPCUsername(ctx)
  if err != nil {
    return &pbs.GetSpectatorGamesResponse{}, status.Error(codes.Unauthenticated, err.Error())
  }
  spectatorGames, err := s.gameManager.getSpectatorGames(ctx, username)
  if err != nil {
    return &pbs.GetSpectatorGamesResponse{}, status.Error(codes.InvalidArgument, err.Error())
  }

  var games []*pbs.GameMetadata
  for _, game := range spectatorGames {
    games = append(games, &pbs.GameMetadata{
      GameType: util.GetGameType(game.GameType),
      GameKey:  game.GameKey,
      User1:    game.User1,
      User2:    game.User2,
      Moves:    game.Moves,
      Winner:   game.Winner,
    })
  }

  return &pbs.GetSpectatorGamesResponse{
    Games: games,
  }, nil
}

func NewGetSpectatorGamesServer(gameManager *GameManager) *GetSpectatorGamesServer {
  return &GetSpectatorGamesServer{
    gameManager: gameManager,
  }
}
