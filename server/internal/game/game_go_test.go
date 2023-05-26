package game

import (
	"fmt"
	"testing"

	pbs "github.com/patrickkenney9801/librarygames/internal/pbs/v1"
	"github.com/patrickkenney9801/librarygames/internal/util"
)

type ScoreGoBoardTest struct {
	board    string
	gameType pbs.GameType

	p1StonesCaptured int
	p2StonesCaptured int

	expectedP1Score int
	expectedP2Score int
	expectedWinner  int32
}

var (
	boards = map[string]ScoreGoBoardTest{
		"board1": {
			board:            "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000100000020000000000000000020200000100000100000200000000000000010000000000000000000000000000000000000000000000020000000200000000000000000000000000000001000000000000000000000000000000000001000000000200000000000000000000000000000000000000000000000000000000000000000000000000000000",
			gameType:         pbs.GameType_GO_19X19,
			p1StonesCaptured: 0,
			p2StonesCaptured: 1,
			expectedP1Score:  0,
			expectedP2Score:  3,
			expectedWinner:   WinPlayer2,
		},
		"board2": {
			board:            "000001000000010100001101000001010020010010000010102000001000022000000020000020022",
			gameType:         pbs.GameType_GO_9X9,
			p1StonesCaptured: 5,
			p2StonesCaptured: 1,
			expectedP1Score:  11,
			expectedP2Score:  8,
			expectedWinner:   WinPlayer1,
		},
	}
)

func TestCalculateGoScore(t *testing.T) {
	for boardName, board := range boards {
		if err := validateGoScoring(boardName, board); err != nil {
			t.Fatal(err)
		}
	}
}

func validateGoScoring(boardName string, board ScoreGoBoardTest) error {
	winner, p1Score, p2Score := calculateGoScore(util.GetIntGameType(board.gameType), board.board, int32(board.p1StonesCaptured), int32(board.p2StonesCaptured))
	if p1Score != int32(board.expectedP1Score) {
		return fmt.Errorf("on board %q expected p1Score %d, got %d", boardName, board.expectedP1Score, p1Score)
	}
	if p2Score != int32(board.expectedP2Score) {
		return fmt.Errorf("on board %q expected p2Score %d, got %d", boardName, board.expectedP2Score, p2Score)
	}
	if winner != int32(board.expectedWinner) {
		return fmt.Errorf("on board %q expected winner %d, got %d", boardName, board.expectedWinner, winner)
	}
	return nil
}
