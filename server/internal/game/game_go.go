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

type GoBoard struct {
  board [][]byte
}

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

func (g *GoGame) makeMove(ctx context.Context, username string, moveFrom int32, moveTo int32) error {
  g.mu.Lock()
  defer g.mu.Unlock()
  if g.closed {
    return ErrGameClosed
  }

  gameState, err := g.database.GetGoGameState(ctx, g.gameKey)
  if err != nil {
    return err
  }
  if gameState.GeneralState.Winner != 0 {
    return ErrGameFinished
  }
  if username != g.player1 && username != g.player2 {
    return ErrUnknownPlayerMove
  }

  // handle resignation
  resigned := moveTo == MoveResignation
  if resigned {
    if username == g.player1 {
      gameState.GeneralState.Winner = WinPlayer2ByResignation
    } else {
      gameState.GeneralState.Winner = WinPlayer1ByResignation
    }
  }

  player1Turn := gameState.GeneralState.Moves%2 == 0

  if !resigned {
    if player1Turn && username != g.player1 {
      return ErrNotPlayerTurn
    }
    if !player1Turn && username != g.player2 {
      return ErrNotPlayerTurn
    }
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

  gameState.GeneralState.Moves += 1
  gameState.GeneralState.PenultMove = gameState.GeneralState.LastMove
  gameState.GeneralState.LastMove = moveTo

  // handle end of game when both players pass
  if moveTo == MovePass && gameState.GeneralState.LastMove == MovePass {
    p1Score, p2Score := calculateGoScore(gameState.GeneralState.GameType, gameState.GeneralState.Board, gameState.P1StonesCaptured, gameState.P2StonesCaptured)
    if p1Score > p2Score {
      gameState.GeneralState.Winner = WinPlayer1
    } else {
      gameState.GeneralState.Winner = WinPlayer2
    }
    gameState.P1Score = p1Score
    gameState.P2Score = p2Score
  }

  success, err := g.database.UpdateGoGame(ctx, gameState)
  if err != nil {
    return err
  }
  if !success {
    return fmt.Errorf("persisting move failed")
  }

  for _, channel := range g.watchers {
    g.forwardState(channel, &GoGameState{
      goState:   gameState,
      liveState: g.getLiveState(),
    })
  }
  return nil
}

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
  g.forwardState(channel, &GoGameState{
    goState:   gameState,
    liveState: g.getLiveState(),
  })
  return channel
}

func (g *GoGame) deregisterWatcher(username string) {
  g.mu.Lock()
  defer g.mu.Unlock()
  if g.closed {
    return
  }

  if staleChannel, ok := g.watchers[username]; ok {
    close(staleChannel)
  }
  delete(g.watchers, username)
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

func (g *GoGame) getLiveState() *LiveGameState {
  _, player1OnGame := g.watchers[g.player1]
  _, player2OnGame := g.watchers[g.player2]
  return &LiveGameState{
    Player1Online: player1OnGame,
    Player2Online: player2OnGame,
  }
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

func calculateGoScore(gameType int32, board string, p1StonesCaptured int32, p2StonesCaptured int32) (int32, int32) {
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
  return p1Score, p2Score
}

func newGoBoard(gameType int32, board1D string) GoBoard {
  var boardSize int
  switch util.GetGameType(gameType) {
  case pbs.GameType_GO_9X9:
    boardSize = 9
  case pbs.GameType_GO_13X13:
    boardSize = 13
  case pbs.GameType_GO_19X19:
    boardSize = 19
  default:
    boardSize = 19
  }

  // pad board to avoid out of bounds logic
  board := GoBoard{
    board: make([][]byte, boardSize+2),
  }
  for i := 0; i < boardSize; i++ {
    board.board[i] = make([]byte, boardSize+2)
    for j := 0; j < boardSize; j++ {
      board.board[i+1][j+1] = board1D[i*boardSize+j]
    }
  }
  return board
}

func (b *GoBoard) calculateTerritory() (int32, int32) {
  p1Score := 0
  p2Score := 0

  // cycle through board to determine who controls empty territory or if it is neutral
  for i := 1; i < len(b.board)-1; i++ {
    for j := 1; j < len(b.board)-1; j++ {
      if b.board[i][j] == emptyPiece {
        // when an empty intersection is found, paint its entire group sentinelPiece
        b.paintBoard(i, j, emptyPiece, sentinelPiece)

        // check if the territory touches black stones
        touchesBlack := b.charTouches(sentinelPiece, firstPlayerPiece)
        // check if the territory touched white stones
        touchesWhite := b.charTouches(sentinelPiece, secondPlayerPiece)

        // if only touches black stones, set territory black territory
        if touchesBlack && !touchesWhite {
          b.paintBoard(i, j, sentinelPiece, blackTerritory)
        } else if !touchesBlack && touchesWhite {
          b.paintBoard(i, j, sentinelPiece, whiteTerritory)
        } else {
          b.paintBoard(i, j, sentinelPiece, neutralTerritory)
        }
      }
    }
  }
  // count territories each player holds
  for i := 1; i < len(b.board)-1; i++ {
    for j := 1; j < len(b.board)-1; j++ {
      if b.board[i][j] == blackTerritory {
        p1Score++
      } else if b.board[i][j] == whiteTerritory {
        p2Score++
      }
    }
  }
  return int32(p1Score), int32(p2Score)
}

func (b *GoBoard) makeMove(move1D int32, piece byte, opposingPiece byte) error {
  x, y, err := get2DCoordinates(len(b.board)-2, move1D)
  if err != nil {
    return err
  }
  // check if the group of stones created has a liberty
  hasLiberty := b.groupHasLiberty(x, y)
  // reset paddedBoard
  b.paintBoard(x, y, sentinelPiece, piece)

  // finally test if a surrounding opposing stone is captured, if so the group has a liberty
  // boards are not unpainted if the group has no liberties
  // unpaint if they do so that they are not removed
  if b.board[x][y+1] == opposingPiece {
    if b.groupHasLiberty(x, y+1) {
      b.paintBoard(x, y+1, sentinelPiece, opposingPiece)
    } else {
      hasLiberty = true
    }
  }
  if b.board[x][y-1] == opposingPiece {
    if b.groupHasLiberty(x, y-1) {
      b.paintBoard(x, y-1, sentinelPiece, opposingPiece)
    } else {
      hasLiberty = true
    }
  }
  if b.board[x+1][y] == opposingPiece {
    if b.groupHasLiberty(x+1, y) {
      b.paintBoard(x+1, y, sentinelPiece, opposingPiece)
    } else {
      hasLiberty = true
    }
  }
  if b.board[x-1][y] == opposingPiece {
    if b.groupHasLiberty(x-1, y) {
      b.paintBoard(x-1, y, sentinelPiece, opposingPiece)
    } else {
      hasLiberty = true
    }
  }
  if !hasLiberty {
    return ErrIllegalMove
  }
  return nil
}

func (b *GoBoard) groupHasLiberty(x int, y int) bool {
  b.paintBoard(x, y, b.board[x][y], sentinelPiece)
  for i := 1; i < len(b.board)-1; i++ {
    for j := 1; j < len(b.board)-1; j++ {
      if b.board[i][j] == sentinelPiece {
        if b.board[i][j+1] == emptyPiece || b.board[i][j-1] == emptyPiece || b.board[i+1][j] == emptyPiece || b.board[i-1][j] == emptyPiece {
          return true
        }
      }
    }
  }
  return false
}

func (b *GoBoard) paintBoard(x int, y int, target byte, replace byte) {
  b.board[x][y] = replace
  if b.board[x][y+1] == target {
    b.paintBoard(x, y+1, target, replace)
  }
  if b.board[x][y-1] == target {
    b.paintBoard(x, y-1, target, replace)
  }
  if b.board[x+1][y] == target {
    b.paintBoard(x+1, y, target, replace)
  }
  if b.board[x-1][y] == target {
    b.paintBoard(x-1, y, target, replace)
  }
}

func (b *GoBoard) charTouches(base byte, target byte) bool {
  for i := 1; i < len(b.board)-1; i++ {
    for j := 1; j < len(b.board)-1; j++ {
      if b.board[i][j] == base {
        if b.board[i][j+1] == target || b.board[i][j-1] == target || b.board[i+1][j] == target || b.board[i-1][j] == target {
          return true
        }
      }
    }
  }
  return false
}

func (b *GoBoard) get1DBoard() string {
  board := make([]byte, (len(b.board)-2)*(len(b.board)-2))
  for i := 1; i < len(b.board)-1; i++ {
    for j := 1; j < len(b.board)-1; j++ {
      piece := b.board[i][j]
      if piece == sentinelPiece {
        piece = emptyPiece
      }
      board[get1DCoordinates(len(b.board)-2, i, j)] = piece
    }
  }
  return string(board)
}

func get1DCoordinates(boardSize, x int, y int) int {
  return boardSize*(x-1) + y - 1
}

func get2DCoordinates(boardSize int, move1D int32) (int, int, error) {
  if move1D < 0 || move1D >= int32(boardSize*boardSize) {
    return 0, 0, ErrMoveOutOfBounds
  }
  // adjust cpprdinates for padding
  return (int(move1D) / boardSize) + 1, (int(move1D) % boardSize) + 1, nil
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
