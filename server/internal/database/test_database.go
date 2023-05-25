package database

import "context"

const (
	TestChannelBufferSize = 100
)

type TestStringError struct {
	S string
	E error
}

type TestStringStringError struct {
	S1 string
	S2 string
	E  error
}

type TestBoolError struct {
	B bool
	E error
}

type TestGoGameStateError struct {
	G *GoGameState
	E error
}

type TestDatabase struct {
	LoginResult              chan TestStringError
	CreateAccountResult      chan TestBoolError
	GetFriendsResultMap      map[string]bool
	GetFriendsResult         chan error
	GetUsersResultArray      []string
	GetUsersResult           chan error
	GetUserKeyMapResultArray map[string]string
	GetUserKeyMapResult      chan error
	GetUserKeyResult         chan TestStringError
	GetUsernameResult        chan TestStringError
	AddFriendResult          chan TestBoolError
	UsernameExistsResult     chan TestBoolError

	GetGamePlayerKeysResult chan TestStringStringError

	CreateGameResult             chan TestStringError
	GetGamesResultArray          []GameMetadata
	GetGamesResult               chan error
	GetSpectatorGamesResultArray []GameMetadata
	GetSpectatorGamesResult      chan error

	CreateGoGameResult   chan TestBoolError
	GetGoGameStateResult chan TestGoGameStateError
	UpdateGoGameResult   chan TestBoolError
}

func NewTestDatabase() *TestDatabase {
	db := &TestDatabase{
		LoginResult:              make(chan TestStringError, TestChannelBufferSize),
		CreateAccountResult:      make(chan TestBoolError, TestChannelBufferSize),
		GetFriendsResultMap:      make(map[string]bool),
		GetFriendsResult:         make(chan error, TestChannelBufferSize),
		GetUsersResultArray:      make([]string, 0),
		GetUsersResult:           make(chan error, TestChannelBufferSize),
		GetUserKeyMapResultArray: make(map[string]string),
		GetUserKeyMapResult:      make(chan error, TestChannelBufferSize),
		GetUserKeyResult:         make(chan TestStringError, TestChannelBufferSize),
		GetUsernameResult:        make(chan TestStringError, TestChannelBufferSize),
		AddFriendResult:          make(chan TestBoolError, TestChannelBufferSize),
		UsernameExistsResult:     make(chan TestBoolError, TestChannelBufferSize),

		GetGamePlayerKeysResult: make(chan TestStringStringError, TestChannelBufferSize),

		CreateGameResult:             make(chan TestStringError, TestChannelBufferSize),
		GetGamesResultArray:          make([]GameMetadata, 0),
		GetGamesResult:               make(chan error, TestChannelBufferSize),
		GetSpectatorGamesResultArray: make([]GameMetadata, 0),
		GetSpectatorGamesResult:      make(chan error, TestChannelBufferSize),

		CreateGoGameResult:   make(chan TestBoolError, TestChannelBufferSize),
		GetGoGameStateResult: make(chan TestGoGameStateError, TestChannelBufferSize),
		UpdateGoGameResult:   make(chan TestBoolError, TestChannelBufferSize),
	}
	return db
}

func (t *TestDatabase) Login(ctx context.Context, username string, password string) (string, error) {
	res := <-t.LoginResult
	return res.S, res.E
}

func (t *TestDatabase) CreateAccount(ctx context.Context, username string, password string, email string) (bool, error) {
	res := <-t.CreateAccountResult
	return res.B, res.E
}

func (t *TestDatabase) GetFriends(ctx context.Context, userKey string) (map[string]bool, error) {
	res := <-t.GetFriendsResult
	return t.GetFriendsResultMap, res
}

func (t *TestDatabase) GetUsers(ctx context.Context) ([]string, error) {
	res := <-t.GetUsersResult
	return t.GetUsersResultArray, res
}

func (t *TestDatabase) GetUserKeyMap(ctx context.Context) (map[string]string, error) {
	res := <-t.GetUserKeyMapResult
	return t.GetUserKeyMapResultArray, res
}

func (t *TestDatabase) GetUserKey(ctx context.Context, username string) (string, error) {
	res := <-t.GetUserKeyResult
	return res.S, res.E
}

func (t *TestDatabase) GetUsername(ctx context.Context, userKey string) (string, error) {
	res := <-t.GetUsernameResult
	return res.S, res.E
}

func (t *TestDatabase) AddFriend(ctx context.Context, userKey string, friendKey string) (bool, error) {
	res := <-t.AddFriendResult
	return res.B, res.E
}

func (t *TestDatabase) UsernameExists(ctx context.Context, username string) (bool, error) {
	res := <-t.UsernameExistsResult
	return res.B, res.E
}

func (t *TestDatabase) CreateGame(ctx context.Context, gameType int32, user1Key string, user2Key string, board string) (string, error) {
	res := <-t.CreateGameResult
	return res.S, res.E
}

func (t *TestDatabase) GetGames(ctx context.Context, userKeyMap map[string]string, userKey string) ([]GameMetadata, error) {
	res := <-t.GetGamesResult
	return t.GetGamesResultArray, res
}

func (t *TestDatabase) GetSpectatorGames(ctx context.Context, userKeyMap map[string]string, userKey string) ([]GameMetadata, error) {
	res := <-t.GetSpectatorGamesResult
	return t.GetSpectatorGamesResultArray, res
}

func (t *TestDatabase) GetGamePlayerKeys(ctx context.Context, gameKey string) (string, string, error) {
	res := <-t.GetGamePlayerKeysResult
	return res.S1, res.S2, res.E
}

func (t *TestDatabase) CreateGoGame(ctx context.Context, gameKey string) (bool, error) {
	res := <-t.CreateGoGameResult
	return res.B, res.E
}

func (t *TestDatabase) GetGoGameState(ctx context.Context, gameKey string) (*GoGameState, error) {
	res := <-t.GetGoGameStateResult
	return res.G, res.E
}

func (t *TestDatabase) UpdateGoGame(ctx context.Context, gameState *GoGameState) (bool, error) {
	res := <-t.UpdateGoGameResult
	return res.B, res.E
}
