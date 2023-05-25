package chat

import (
	"context"
	"sync"

	"github.com/patrickkenney9801/librarygames/internal/database"
	"github.com/patrickkenney9801/librarygames/metrics"
)

const (
	MaxBufferedMessages = 100
)

type ChatMessage struct {
	sender  string
	message string
}

type Chat struct {
	mu sync.RWMutex

	player1 string
	player2 string

	player1Messages   chan ChatMessage
	player2Messages   chan ChatMessage
	spectatorMessages map[string]chan ChatMessage

	closed  bool
	metrics *metrics.Client
}

type ChatManager struct {
	mu sync.Mutex

	chats map[string]*Chat

	database database.Database
	metrics  *metrics.Client
}

func NewChatManager(db database.Database, metrics *metrics.Client) *ChatManager {
	return &ChatManager{
		chats: make(map[string]*Chat),

		database: db,
		metrics:  metrics,
	}
}

func (m *ChatManager) getChat(ctx context.Context, gameKey string) (*Chat, error) {
	m.mu.Lock()
	defer m.mu.Unlock()

	if chat, ok := m.chats[gameKey]; ok {
		return chat, nil
	}
	playerKey1, playerKey2, err := m.database.GetGamePlayerKeys(ctx, gameKey)
	if err != nil {
		return nil, err
	}
	player1, err := m.database.GetUsername(ctx, playerKey1)
	if err != nil {
		return nil, err
	}
	player2, err := m.database.GetUsername(ctx, playerKey2)
	if err != nil {
		return nil, err
	}

	chat := newChat(m.metrics, player1, player2)
	m.chats[gameKey] = chat
	return chat, nil
}

// TODO cleanup logic

func newChat(metrics *metrics.Client, player1 string, player2 string) *Chat {
	return &Chat{
		player1: player1,
		player2: player2,

		player1Messages:   nil,
		player2Messages:   nil,
		spectatorMessages: make(map[string]chan ChatMessage),

		closed:  false,
		metrics: metrics,
	}
}

func (c *Chat) sendMessage(message ChatMessage, public bool) {
	c.mu.RLock()
	defer c.mu.RUnlock()
	if c.closed {
		return
	}

	if c.player1Messages != nil {
		c.forwardMessage(c.player1Messages, message)
	}
	if c.player2Messages != nil {
		c.forwardMessage(c.player2Messages, message)
	}
	if public {
		for _, spectatorChannel := range c.spectatorMessages {
			c.forwardMessage(spectatorChannel, message)
		}
	} else if channel, ok := c.spectatorMessages[message.sender]; ok {
		c.forwardMessage(channel, message)
	}
}

func (c *Chat) close() {
	c.mu.Lock()
	defer c.mu.Unlock()
	if c.closed {
		return
	}

	c.closed = true
	if c.player1Messages != nil {
		close(c.player1Messages)
	}
	if c.player2Messages != nil {
		close(c.player2Messages)
	}
	for _, spectatorChannel := range c.spectatorMessages {
		close(spectatorChannel)
	}
}

func (c *Chat) registerChatter(username string) chan ChatMessage {
	c.mu.Lock()
	defer c.mu.Unlock()
	if c.closed {
		return nil
	}
	channel := make(chan ChatMessage, MaxBufferedMessages)

	if username == c.player1 {
		if c.player1Messages != nil {
			close(c.player1Messages)
		}
		c.player1Messages = channel
	} else if username == c.player2 {
		if c.player2Messages != nil {
			close(c.player2Messages)
		}
		c.player2Messages = channel
	} else {
		if staleChannel, ok := c.spectatorMessages[username]; ok {
			close(staleChannel)
		}
		c.spectatorMessages[username] = channel
	}
	return channel
}

func (c *Chat) deregisterChatter(username string) {
	c.mu.Lock()
	defer c.mu.Unlock()
	if c.closed {
		return
	}

	if username == c.player1 {
		if c.player1Messages != nil {
			close(c.player1Messages)
		}
		c.player1Messages = nil
	} else if username == c.player2 {
		if c.player2Messages != nil {
			close(c.player2Messages)
		}
		c.player2Messages = nil
	} else {
		if staleChannel, ok := c.spectatorMessages[username]; ok {
			close(staleChannel)
		}
		delete(c.spectatorMessages, username)
	}
}

func (c *Chat) forwardMessage(channel chan ChatMessage, message ChatMessage) {
	select {
	case channel <- message:
	default:
		c.metrics.GetCounter(metrics.ChatMessagesDroppedCounter).Inc()
	}
}
