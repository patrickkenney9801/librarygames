package chat

import (
  "context"
  "fmt"
  "math/rand"
  "testing"

  "github.com/patrickkenney9801/librarygames/internal/database"
  "github.com/patrickkenney9801/librarygames/metrics"
  "github.com/prometheus/client_golang/prometheus"
)

const (
  letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
)

type MessageChannel struct {
  owner   string
  channel chan ChatMessage
}

func TestSingleChat(t *testing.T) {
  ctx := context.Background()
  db, chatManager, err := getChatManager()
  if err != nil {
    t.Fatal(err)
  }

  errGameDoesNotExist := fmt.Errorf("game does not exist")
  player1 := randomString(15)
  player2 := randomString(15)

  fail := database.TestStringStringError{
    S1: randomString(15),
    S2: randomString(15),
    E:  errGameDoesNotExist,
  }
  db.GetGamePlayerKeysResult <- fail

  if _, err := chatManager.getChat(ctx, randomString(15)); err != errGameDoesNotExist {
    t.Fatal(fmt.Errorf("expected error game does not exist"))
  }

  chat, err := getChat(chatManager, db, player1, player2, randomString(15))
  if err != nil {
    t.Fatal(err)
  }

  player1Channel := chat.registerChatter(player1)
  message := randomString(10)
  chat.sendMessage(ChatMessage{
    sender:  player1,
    message: message,
  }, false)

  recvMsg := <-player1Channel
  if recvMsg.sender != player1 {
    t.Fatal(fmt.Errorf("message had sender %q, expected sender %q", recvMsg.sender, player1))
  }
  if recvMsg.message != message {
    t.Fatal(fmt.Errorf("message has incorrect text, received %q", message))
  }

  chat.deregisterChatter(player1)
  chat.close()
}

func TestGeneralChat(t *testing.T) {
  ctx := context.Background()
  db, chatManager, err := getChatManager()
  if err != nil {
    t.Fatal(err)
  }

  gameKey := randomString(15)
  player1 := randomString(15)
  player2 := randomString(15)
  spectator1 := randomString(15)
  spectator2 := randomString(15)

  chat, err := getChat(chatManager, db, gameKey, player1, player2)
  if err != nil {
    t.Fatal(err)
  }
  player1Channel := chat.registerChatter(player1)

  chat2, err := chatManager.getChat(ctx, gameKey)
  if err != nil {
    t.Fatal(err)
  }
  player2Channel := chat2.registerChatter(player2)

  var spectatorChannels []MessageChannel
  chat3, err := chatManager.getChat(ctx, gameKey)
  if err != nil {
    t.Fatal(err)
  }
  spectatorChannels = append(spectatorChannels, MessageChannel{
    owner:   spectator1,
    channel: chat3.registerChatter(spectator1),
  })
  chat4, err := chatManager.getChat(ctx, gameKey)
  if err != nil {
    t.Fatal(err)
  }
  spectatorChannels = append(spectatorChannels, MessageChannel{
    owner:   spectator2,
    channel: chat4.registerChatter(spectator2),
  })

  if err = sendMessage(chat, player1, false, player1Channel, player2Channel, spectatorChannels); err != nil {
    t.Fatal(err)
  }
  if err = sendMessage(chat, player2, true, player1Channel, player2Channel, spectatorChannels); err != nil {
    t.Fatal(err)
  }
  if err = sendMessage(chat, spectator1, false, player1Channel, player2Channel, spectatorChannels); err != nil {
    t.Fatal(err)
  }
  if err = sendMessage(chat, spectator2, true, player1Channel, player2Channel, spectatorChannels); err != nil {
    t.Fatal(err)
  }

  chat.deregisterChatter(player1)
  chat2.deregisterChatter(player2)
  chat3.deregisterChatter(spectator1)
  chat4.deregisterChatter(spectator2)
  chat.close()
}

func TestStressChat(t *testing.T) {
  ctx := context.Background()
  db, chatManager, err := getChatManager()
  if err != nil {
    t.Fatal(err)
  }

  gameKey := randomString(15)
  player1 := randomString(15)
  player2 := randomString(15)
  spectator1 := randomString(15)
  spectator2 := randomString(15)

  chat, err := getChat(chatManager, db, gameKey, player1, player2)
  if err != nil {
    t.Fatal(err)
  }
  player1Channel := chat.registerChatter(player1)

  chat2, err := chatManager.getChat(ctx, gameKey)
  if err != nil {
    t.Fatal(err)
  }
  player2Channel := chat2.registerChatter(player2)

  var spectatorChannels []MessageChannel
  chat3, err := chatManager.getChat(ctx, gameKey)
  if err != nil {
    t.Fatal(err)
  }
  spectatorChannels = append(spectatorChannels, MessageChannel{
    owner:   spectator1,
    channel: chat3.registerChatter(spectator1),
  })
  chat4, err := chatManager.getChat(ctx, gameKey)
  if err != nil {
    t.Fatal(err)
  }
  spectatorChannels = append(spectatorChannels, MessageChannel{
    owner:   spectator2,
    channel: chat4.registerChatter(spectator2),
  })

  if err = sendMessage(chat, player1, false, player1Channel, player2Channel, spectatorChannels); err != nil {
    t.Fatal(err)
  }
  if err = sendMessage(chat, player2, true, player1Channel, player2Channel, spectatorChannels); err != nil {
    t.Fatal(err)
  }
  if err = sendMessage(chat, spectator1, false, player1Channel, player2Channel, spectatorChannels); err != nil {
    t.Fatal(err)
  }
  if err = sendMessage(chat, spectator2, true, player1Channel, player2Channel, spectatorChannels); err != nil {
    t.Fatal(err)
  }

  chat.deregisterChatter(player1)
  if err = sendMessage(chat, player2, true, nil, player2Channel, spectatorChannels); err != nil {
    t.Fatal(err)
  }
  if err = sendMessage(chat, spectator1, false, nil, player2Channel, spectatorChannels); err != nil {
    t.Fatal(err)
  }
  if err = sendMessage(chat, spectator2, true, nil, player2Channel, spectatorChannels); err != nil {
    t.Fatal(err)
  }

  player1Channel = chat.registerChatter(player1)
  chat2.deregisterChatter(player2)
  if err = sendMessage(chat, player1, false, player1Channel, nil, spectatorChannels); err != nil {
    t.Fatal(err)
  }
  if err = sendMessage(chat, player2, true, player1Channel, nil, spectatorChannels); err != nil {
    t.Fatal(err)
  }
  if err = sendMessage(chat, spectator1, false, player1Channel, nil, spectatorChannels); err != nil {
    t.Fatal(err)
  }

  player2Channel = chat.registerChatter(player2)
  chat4.deregisterChatter(spectator2)
  spectatorChannels = spectatorChannels[0:1]
  if err = sendMessage(chat, player1, false, player1Channel, player2Channel, spectatorChannels); err != nil {
    t.Fatal(err)
  }
  if err = sendMessage(chat, player2, true, player1Channel, player2Channel, spectatorChannels); err != nil {
    t.Fatal(err)
  }
  if err = sendMessage(chat, spectator1, false, player1Channel, player2Channel, spectatorChannels); err != nil {
    t.Fatal(err)
  }

  chat2.deregisterChatter(player2)
  chat3.deregisterChatter(spectator1)
  chat4.deregisterChatter(spectator2)
  chat.close()
}

func sendMessage(chat *Chat, sender string, public bool, player1Channel chan ChatMessage, player2Channel chan ChatMessage, spectatorChannels []MessageChannel) error {
  message := randomString(10)
  chat.sendMessage(ChatMessage{
    sender:  sender,
    message: message,
  }, public)

  if player1Channel != nil {
    recvMsg := <-player1Channel
    if recvMsg.sender != sender {
      return fmt.Errorf("message had sender %q, expected sender %q", recvMsg.sender, sender)
    }
    if recvMsg.message != message {
      return fmt.Errorf("message has incorrect text, received %q", message)
    }
  }
  if player2Channel != nil {
    recvMsg := <-player2Channel
    if recvMsg.sender != sender {
      return fmt.Errorf("message had sender %q, expected sender %q", recvMsg.sender, sender)
    }
    if recvMsg.message != message {
      return fmt.Errorf("message has incorrect text, received %q", message)
    }
  }
  for _, channel := range spectatorChannels {
    if public || sender == channel.owner {
      recvMsg := <-channel.channel
      if recvMsg.sender != sender {
        return fmt.Errorf("message had sender %q, expected sender %q", recvMsg.sender, sender)
      }
      if recvMsg.message != message {
        return fmt.Errorf("message has incorrect text, received %q", message)
      }
    } else {
      select {
      case <-channel.channel:
        return fmt.Errorf("spectator %q unexpectedly received a private message", channel.owner)
      default:
      }
    }
  }
  return nil
}

func getChat(chatManager *ChatManager, db *database.TestDatabase, gameKey string, player1 string, player2 string) (*Chat, error) {
  player1Key := randomString(15)
  player2Key := randomString(15)

  keysSuccess := database.TestStringStringError{
    S1: player1Key,
    S2: player2Key,
    E:  nil,
  }
  player1Success := database.TestStringError{
    S: player1,
    E: nil,
  }
  player2Success := database.TestStringError{
    S: player2,
    E: nil,
  }
  db.GetGamePlayerKeysResult <- keysSuccess
  db.GetUsernameResult <- player1Success
  db.GetUsernameResult <- player2Success

  return chatManager.getChat(context.Background(), gameKey)
}

func getChatManager() (*database.TestDatabase, *ChatManager, error) {
  db := database.NewTestDatabase()
  metrics, err := metrics.New(prometheus.NewRegistry())
  if err != nil {
    return nil, nil, err
  }
  return db, NewChatManager(db, metrics), nil
}

func randomString(length int) string {
  b := make([]byte, length)
  for i := range b {
    b[i] = letters[rand.Intn(len(letters))]
  }
  return string(b)
}
