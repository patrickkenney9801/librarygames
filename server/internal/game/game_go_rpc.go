package game

import (
	"io"

	pbs "github.com/patrickkenney9801/librarygames/internal/pbs/v1"
	"github.com/patrickkenney9801/librarygames/internal/util"
	"golang.org/x/exp/slog"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

type GameGoServer struct {
	pbs.UnimplementedGoServer

	gameManager *GameManager
}

func (s *GameGoServer) PlayGo(stream pbs.Go_PlayGoServer) error {
	var game *GoGame
	var receiver chan *GoGameState

	game = nil
	username, err := util.GetGRPCUsername(stream.Context())
	if err != nil {
		return status.Error(codes.Unauthenticated, err.Error())
	}
	defer func() {
		if game != nil {
			game.deregisterWatcher(stream.Context(), username)
		}
	}()

	done := make(chan bool)

	for {
		message, err := stream.Recv()
		if err == io.EOF {
			break
		}
		if err != nil {
			return err
		}
		select {
		case <-done:
			return nil
		default:
		}

		if game == nil {
			game, err = s.gameManager.getGoGame(stream.Context(), message.GameKey)
			if err != nil {
				return err
			}
			receiver = game.registerWatcher(stream.Context(), username)
			go streamGameStates(stream, receiver, done)
		}

		if err = game.makeMove(stream.Context(), username, message.MoveFrom, message.MoveTo); err != nil {
			slog.Warn("go game move failed from %d to %d", message.MoveFrom, message.MoveTo, slog.String("error", err.Error()))
			return err
		}
	}
	return nil
}

func streamGameStates(stream pbs.Go_PlayGoServer, receiver chan *GoGameState, done chan bool) {
	for state := range receiver {
		if err := stream.Send(translateGoGameState(state)); err != nil {
			slog.Warn("sending go game state failed", slog.String("err", err.Error()))
			break
		}
	}
	done <- true
}

func NewGameGoServer(gameManager *GameManager) *GameGoServer {
	return &GameGoServer{
		gameManager: gameManager,
	}
}

func translateGoGameState(state *GoGameState) *pbs.StateGoResponse {
	return &pbs.StateGoResponse{
		GameState: &pbs.GameState{
			Board:  state.goState.GeneralState.Board,
			Moves:  state.goState.GeneralState.Moves,
			Winner: state.goState.GeneralState.Winner,

			LastMove:   state.goState.GeneralState.LastMove,
			PenultMove: state.goState.GeneralState.PenultMove,

			Player1Online: state.liveState.Player1Online,
			Player2Online: state.liveState.Player2Online,
		},

		P1StonesCaptured: state.goState.P1StonesCaptured,
		P2StonesCaptured: state.goState.P2StonesCaptured,

		P1Score: state.goState.P1Score,
		P2Score: state.goState.P2Score,
	}
}
