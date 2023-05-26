package chat

import (
	"io"

	pbs "github.com/patrickkenney9801/librarygames/internal/pbs/v1"
	"github.com/patrickkenney9801/librarygames/internal/util"
	"golang.org/x/exp/slog"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

type ChatServer struct {
	pbs.UnimplementedChatServer

	chatManager *ChatManager
}

func (s *ChatServer) Chat(stream pbs.Chat_ChatServer) error {
	var chat *Chat
	var receiver chan ChatMessage

	chat = nil
	username, err := util.GetGRPCUsername(stream.Context())
	if err != nil {
		return status.Error(codes.Unauthenticated, err.Error())
	}
	defer func() {
		if chat != nil {
			chat.deregisterChatter(username)
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

		if chat == nil {
			chat, err = s.chatManager.getChat(stream.Context(), message.GameKey)
			if err != nil {
				return err
			}
			receiver = chat.registerChatter(username)
			go streamMessages(stream, receiver, done)
		}

		chat.sendMessage(
			ChatMessage{
				sender:  username,
				message: message.Message,
			},
			message.Public,
		)
	}
	return nil
}

func streamMessages(stream pbs.Chat_ChatServer, receiver chan ChatMessage, done chan bool) {
	for msg := range receiver {
		if err := stream.Send(&pbs.ChatResponse{
			Sender:  msg.sender,
			Message: msg.message,
		}); err != nil {
			slog.Warn("sending chat message failed", slog.String("err", err.Error()))
			break
		}
	}
	done <- true
}

func NewChatServer(chatManager *ChatManager) *ChatServer {
	return &ChatServer{
		chatManager: chatManager,
	}
}
