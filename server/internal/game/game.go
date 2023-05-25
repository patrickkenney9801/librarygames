package game

import (
	"context"
	"fmt"
	"sync"

	"github.com/patrickkenney9801/librarygames/internal/database"
)

const (
	MovePass        = -1
	MoveResignation = -2
	MoveLoadGame    = -3

	WinPlayer1              = 1
	WinPlayer2              = 2
	WinPlayer1ByResignation = 3
	WinPlayer2ByResignation = 4
)

var (
	ErrGameClosed        = fmt.Errorf("game is closed")
	ErrNotPlayerTurn     = fmt.Errorf("player sent move during opponent's turn")
	ErrGameFinished      = fmt.Errorf("game is already finished")
	ErrUnknownPlayerMove = fmt.Errorf("unknown player sent move to game")

	ErrMoveOutOfBounds = fmt.Errorf("move was not within bounds of game board")
	ErrIllegalMove     = fmt.Errorf("illegal move")
)

type GameKey string

type LiveGameState struct {
	Player1Online bool
	Player2Online bool
}

type GameManager struct {
	mu sync.RWMutex

	goGames  map[GameKey]*GoGame
	database database.Database
}

func NewGameManager(database database.Database) (*GameManager, error) {
	return &GameManager{
		database: database,
		goGames:  make(map[GameKey]*GoGame),
	}, nil
}

func (m *GameManager) getGoGame(ctx context.Context, gameKey string) (*GoGame, error) {
	m.mu.Lock()
	defer m.mu.Unlock()

	if game, ok := m.goGames[GameKey(gameKey)]; ok {
		return game, nil
	}
	gameMetadata, err := m.database.GetGoGameState(ctx, gameKey)
	if err != nil {
		return nil, err
	}
	player1, err := m.database.GetUsername(ctx, gameMetadata.GeneralState.User1Key)
	if err != nil {
		return nil, err
	}
	player2, err := m.database.GetUsername(ctx, gameMetadata.GeneralState.User2Key)
	if err != nil {
		return nil, err
	}

	game, err := newGoGame(m.database, gameMetadata, player1, player2)
	if err != nil {
		return nil, err
	}
	m.goGames[GameKey(gameKey)] = game
	return game, nil
}

func (m *GameManager) createGame(ctx context.Context, gameType int32, user1 string, user2 string, board string) (string, error) {
	userKey1, err := m.database.GetUserKey(ctx, user1)
	if err != nil {
		return "", err
	}
	userKey2, err := m.database.GetUserKey(ctx, user2)
	if err != nil {
		return "", err
	}
	gameKey, err := m.database.CreateGame(ctx, gameType, userKey1, userKey2, board)
	if err != nil {
		return "", err
	}
	return gameKey, nil
}

func (m *GameManager) getGames(ctx context.Context, username string) ([]database.GameMetadata, error) {
	userKey, err := m.database.GetUserKey(ctx, username)
	if err != nil {
		return nil, err
	}
	userKeyMap, err := m.database.GetUserKeyMap(ctx)
	if err != nil {
		return nil, err
	}
	return m.database.GetGames(ctx, userKeyMap, userKey)
}

func (m *GameManager) getSpectatorGames(ctx context.Context, username string) ([]database.GameMetadata, error) {
	userKey, err := m.database.GetUserKey(ctx, username)
	if err != nil {
		return nil, err
	}
	userKeyMap, err := m.database.GetUserKeyMap(ctx)
	if err != nil {
		return nil, err
	}
	return m.database.GetSpectatorGames(ctx, userKeyMap, userKey)
}
