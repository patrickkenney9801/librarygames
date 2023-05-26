package game

import (
	"context"
	"fmt"
	"sync"

	"github.com/patrickkenney9801/librarygames/internal/database"
	pbs "github.com/patrickkenney9801/librarygames/internal/pbs/v1"
	"github.com/patrickkenney9801/librarygames/internal/util"
	"golang.org/x/exp/slog"
)

const (
	MaxBufferedMoves = 10

	emptyPiece        = '0'
	firstPlayerPiece  = '1'
	secondPlayerPiece = '2'
	blackTerritory    = '3'
	whiteTerritory    = '4'
	neutralTerritory  = '5'
	sentinelPiece     = '$'
)

var (
	ErrMoveViolatesKoRule = fmt.Errorf("invalid move due to Ko Rule")
)

type GoMoveResult struct {
	board          string
	capturedPieces int32
}

type GoGameState struct {
	goState   *database.GoGameState
	liveState *LiveGameState
}

type GoGame struct {
	mu       sync.Mutex
	database database.Database

	gameType int32
	gameKey  string
	player1  string
	player2  string

	watchers map[string]chan *GoGameState

	closed bool
}

func newGoGame(database database.Database, gameMetadata *database.GoGameState, player1 string, player2 string) (*GoGame, error) {
	return &GoGame{
		database: database,

		gameType: gameMetadata.GeneralState.GameType,
		gameKey:  gameMetadata.GeneralState.GameKey,
		player1:  player1,
		player2:  player2,

		watchers: make(map[string]chan *GoGameState, MaxBufferedMoves),

		closed: false,
	}, nil
}

// TODO use this
/*
func (g *GoGame) close() {
	g.mu.Lock()
	defer g.mu.Unlock()
	if g.closed {
		return
	}

	g.closed = true
	for _, channel := range g.watchers {
		close(channel)
	}
}
*/

func (g *GoGame) makeMove(ctx context.Context, username string, moveFrom int32, moveTo int32) error {
	g.mu.Lock()
	defer g.mu.Unlock()
	if moveTo == MoveLoadGame {
		return nil
	}
	if g.closed {
		return ErrGameClosed
	}

	gameState, err := g.database.GetGoGameState(ctx, g.gameKey)
	if err != nil {
		return err
	}
	player1Turn := gameState.GeneralState.Moves%2 == 0
	resigned := moveTo == MoveResignation

	if err = g.checkGoPreconditions(gameState, username, moveTo, player1Turn, resigned); err != nil {
		return err
	}

	moveResult, err := makeGoMove(gameState.GeneralState.GameType, gameState.GeneralState.Board, moveTo, gameState.GeneralState.PenultMove, player1Turn)
	if err != nil {
		return err
	}
	if moveResult != nil {
		gameState.GeneralState.Board = moveResult.board
		if player1Turn {
			gameState.P1StonesCaptured += moveResult.capturedPieces
		} else {
			gameState.P2StonesCaptured += moveResult.capturedPieces
		}
	}

	// handle end of game when both players pass
	if moveTo == MovePass && gameState.GeneralState.LastMove == MovePass {
		winner, p1Score, p2Score := calculateGoScore(gameState.GeneralState.GameType, gameState.GeneralState.Board, gameState.P1StonesCaptured, gameState.P2StonesCaptured)
		gameState.GeneralState.Winner = winner
		gameState.P1Score = p1Score
		gameState.P2Score = p2Score
	}

	// handle resignation
	if resigned {
		gameState.GeneralState.Winner = handleResignation(username, g.player1, g.player2)
	}

	gameState.GeneralState.Moves += 1
	gameState.GeneralState.PenultMove = gameState.GeneralState.LastMove
	gameState.GeneralState.LastMove = moveTo

	success, err := g.database.UpdateGoGame(ctx, gameState)
	if err != nil {
		return err
	}
	if !success {
		return fmt.Errorf("persisting move failed")
	}

	g.forwardStateAll(&GoGameState{
		goState:   gameState,
		liveState: g.getLiveState(),
	})
	return nil
}

func (g *GoGame) registerWatcher(ctx context.Context, username string) chan *GoGameState {
	g.mu.Lock()
	defer g.mu.Unlock()
	if g.closed {
		return nil
	}
	channel := make(chan *GoGameState, MaxBufferedMoves)

	if staleChannel, ok := g.watchers[username]; ok {
		close(staleChannel)
	}
	g.watchers[username] = channel

	gameState, err := g.database.GetGoGameState(ctx, g.gameKey)
	if err != nil {
		slog.Warn("failed to get go game state", slog.String("error", err.Error()))
	}
	message := &GoGameState{
		goState:   gameState,
		liveState: g.getLiveState(),
	}

	if username == g.player1 || username == g.player2 {
		g.forwardStateAll(message)
	} else {
		g.forwardState(channel, message)
	}
	return channel
}

func (g *GoGame) deregisterWatcher(ctx context.Context, username string) {
	g.mu.Lock()
	defer g.mu.Unlock()
	if g.closed {
		return
	}

	if staleChannel, ok := g.watchers[username]; ok {
		close(staleChannel)
	}
	delete(g.watchers, username)

	if username == g.player1 || username == g.player2 {
		gameState, err := g.database.GetGoGameState(ctx, g.gameKey)
		if err != nil {
			slog.Warn("failed to get go game state", slog.String("error", err.Error()))
		}
		message := &GoGameState{
			goState:   gameState,
			liveState: g.getLiveState(),
		}
		g.forwardStateAll(message)
	}
}

func (m *GameManager) CreateGoGame(ctx context.Context, gameType int32, user1 string, user2 string, creatorGoesFirst bool, board string) (string, error) {
	// TODO use database transaction
	if !creatorGoesFirst {
		tmp := user2
		user2 = user1
		user1 = tmp
	}
	gameKey, err := m.createGame(ctx, gameType, user1, user2, board)
	if err != nil {
		return "", err
	}
	ok, err := m.database.CreateGoGame(ctx, gameKey)
	if err != nil {
		return "", err
	}
	if !ok {
		return "", fmt.Errorf("could not create go game")
	}
	return gameKey, nil
}

func (g *GoGame) forwardState(channel chan *GoGameState, message *GoGameState) {
	select {
	case channel <- message:
	default: // TODO add metrics for dropped messages
	}
}

func (g *GoGame) forwardStateAll(message *GoGameState) {
	for _, channel := range g.watchers {
		g.forwardState(channel, message)
	}
}

func (g *GoGame) getLiveState() *LiveGameState {
	_, player1OnGame := g.watchers[g.player1]
	_, player2OnGame := g.watchers[g.player2]
	return &LiveGameState{
		Player1Online: player1OnGame,
		Player2Online: player2OnGame,
	}
}

func (g *GoGame) checkGoPreconditions(gameState *database.GoGameState, username string, moveTo int32, player1Turn bool, resigned bool) error {
	if gameState.GeneralState.Winner != WinUnfinished {
		return ErrGameFinished
	}
	if username != g.player1 && username != g.player2 {
		return ErrUnknownPlayerMove
	}
	if !resigned {
		if player1Turn && username != g.player1 {
			return ErrNotPlayerTurn
		}
		if !player1Turn && username != g.player2 {
			return ErrNotPlayerTurn
		}
	}
	return nil
}

func makeGoMove(gameType int32, board string, moveTo int32, penultMove int32, player1Turn bool) (*GoMoveResult, error) {
	if moveTo < 0 {
		return nil, nil
	}
	if moveTo == penultMove {
		return nil, ErrMoveViolatesKoRule
	}
	var piece byte
	var opposingPiece byte
	if player1Turn {
		piece = firstPlayerPiece
		opposingPiece = secondPlayerPiece
	} else {
		piece = secondPlayerPiece
		opposingPiece = firstPlayerPiece
	}

	board2D := newGoBoard(gameType, board)
	if err := board2D.makeMove(moveTo, piece, opposingPiece); err != nil {
		return nil, err
	}
	newBoard := board2D.get1DBoard()
	capturedPieces := calculateCapturedPieces(board, newBoard)
	return &GoMoveResult{
		board:          newBoard,
		capturedPieces: capturedPieces,
	}, nil
}

func calculateGoScore(gameType int32, board string, p1StonesCaptured int32, p2StonesCaptured int32) (int32, int32, int32) {
	board2D := newGoBoard(gameType, board)
	p1Score, p2Score := board2D.calculateTerritory()

	p1Score += p1StonesCaptured
	p2Score += p2StonesCaptured

	// apply Komi
	switch util.GetGameType(gameType) {
	case pbs.GameType_GO_9X9:
		p2Score += 6
	case pbs.GameType_GO_13X13:
		p2Score += 3
	case pbs.GameType_GO_19X19:
		p2Score += 1
	default:
		p2Score += 1
	}
	winner := WinPlayer2
	if p1Score > p2Score {
		winner = WinPlayer1
	}
	return winner, p1Score, p2Score
}

func calculateCapturedPieces(oldBoard string, newBoard string) int32 {
	capturedPieces := -1
	for i := range oldBoard {
		if oldBoard[i] != newBoard[i] {
			capturedPieces++
		}
	}
	return int32(capturedPieces)
}
