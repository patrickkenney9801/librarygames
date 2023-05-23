package database

import (
  "context"
  "fmt"
)

var (
  ErrInvalidCredentials = fmt.Errorf("invalid username or password")
)

type GameMetadata struct {
  GameType int32
  GameKey  string
  User1    string
  User2    string
  Moves    int32
  Winner   int32
}

type GameState struct {
  GameType int32
  GameKey  string
  User1Key string
  User2Key string

  Board  string
  Moves  int32
  Winner int32

  LastMove   int32
  PenultMove int32
}

type GoGameState struct {
  GeneralState GameState

  P1StonesCaptured int32
  P2StonesCaptured int32
  P1Score          int32
  P2Score          int32
}

type Database interface {
  Login(ctx context.Context, username string, password string) (string, error)
  CreateAccount(ctx context.Context, username string, password string, email string) (bool, error)
  GetFriends(ctx context.Context, userKey string) (map[string]bool, error)
  GetUsers(ctx context.Context) ([]string, error)
  GetUserKeyMap(ctx context.Context) (map[string]string, error)
  GetUserKey(ctx context.Context, username string) (string, error)
  GetUsername(ctx context.Context, userKey string) (string, error)
  AddFriend(ctx context.Context, userKey string, friendKey string) (bool, error)
  UsernameExists(ctx context.Context, username string) (bool, error)

  GetGamePlayerKeys(ctx context.Context, gameKey string) (string, string, error)

  CreateGame(ctx context.Context, gameType int32, user1Key string, user2Key string, board string) (string, error)
  GetGames(ctx context.Context, userKeyMap map[string]string, userKey string) ([]GameMetadata, error)
  GetSpectatorGames(ctx context.Context, userKeyMap map[string]string, userKey string) ([]GameMetadata, error)

  CreateGoGame(ctx context.Context, gameKey string) (bool, error)
  GetGoGameState(ctx context.Context, gameKey string) (*GoGameState, error)
  UpdateGoGame(ctx context.Context, gameState *GoGameState) (bool, error)
}
