package chat

import (
  "io"

  pbs "github.com/patrickkenney9801/librarygames/internal/pbs/v1"
  "github.com/patrickkenney9801/librarygames/internal/util"
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
  receiver = nil
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
      go func() {
        for msg := range receiver {
          stream.Send(&pbs.ChatResponse{
            Sender:  msg.sender,
            Message: msg.message,
          })
        }
        done <- true
      }()
    }

    if len(message.Message) > 0 {
      chat.sendMessage(
        ChatMessage{
          sender:  username,
          message: message.Message,
        },
        message.Public,
      )
    }
  }
  return nil
}

func NewChatServer(chatManager *ChatManager) *ChatServer {
  return &ChatServer{
    chatManager: chatManager,
  }
}
