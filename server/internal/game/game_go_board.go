package game

import (
	pbs "github.com/patrickkenney9801/librarygames/internal/pbs/v1"
	"github.com/patrickkenney9801/librarygames/internal/util"
)

type GoBoard struct {
	board [][]byte
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
	for i := range board.board {
		board.board[i] = make([]byte, boardSize+2)
	}
	for i := 0; i < boardSize; i++ {
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
			b.paintTerritory(i, j)
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
	b.board[x][y] = piece
	// check if the group of stones created has a liberty
	hasLiberty := b.groupHasLiberty(x, y)
	// reset paddedBoard
	b.paintBoard(x, y, sentinelPiece, piece)

	hasLiberty = hasLiberty || b.capture(x, y+1, opposingPiece)
	hasLiberty = hasLiberty || b.capture(x, y-1, opposingPiece)
	hasLiberty = hasLiberty || b.capture(x+1, y, opposingPiece)
	hasLiberty = hasLiberty || b.capture(x-1, y, opposingPiece)
	if !hasLiberty {
		return ErrIllegalMove
	}
	return nil
}

func (b *GoBoard) capture(x int, y int, opposingPiece byte) bool {
	// test if an opposing stone is captured
	// boards are not unpainted if the opposing group has no liberties
	// unpaint if they do so that they are not removed
	if b.board[x][y+1] == opposingPiece {
		if b.groupHasLiberty(x, y+1) {
			b.paintBoard(x, y+1, sentinelPiece, opposingPiece)
			return false
		} else {
			return true
		}
	}
	return false
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

func (b *GoBoard) paintTerritory(i int, j int) {
	if b.board[i][j] == emptyPiece {
		// when an empty intersection is found, paint its entire group sentinelPiece
		b.paintBoard(i, j, emptyPiece, sentinelPiece)

		// check if the territory touches black stones
		touchesBlack := b.charTouches(sentinelPiece, firstPlayerPiece)
		// check if the territory touched white stones
		touchesWhite := b.charTouches(sentinelPiece, secondPlayerPiece)

		if touchesBlack && !touchesWhite {
			b.paintBoard(i, j, sentinelPiece, blackTerritory)
		} else if !touchesBlack && touchesWhite {
			b.paintBoard(i, j, sentinelPiece, whiteTerritory)
		} else {
			b.paintBoard(i, j, sentinelPiece, neutralTerritory)
		}
	}
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
