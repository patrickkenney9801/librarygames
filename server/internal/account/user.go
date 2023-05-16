package account

import (
  "context"
  "fmt"
  "sync"
  "time"

  "github.com/google/uuid"
  "github.com/patrickkenney9801/librarygames/internal/database"
  "github.com/patrickkenney9801/librarygames/internal/util"
  "golang.org/x/crypto/bcrypt"
  "google.golang.org/grpc"
  "google.golang.org/grpc/codes"
  "google.golang.org/grpc/status"
)

type Username string

type AccessToken struct {
  token          string
  expirationDate time.Time
}

type User struct {
  username Username
  userKey  string
  token    AccessToken
}

type PeerUser struct {
  username string
  online   bool
  friend   bool
}

func (u *User) logout() {}

type UserManager struct {
  mu sync.RWMutex

  users    map[Username]User
  database database.Database
}

func NewUserManager(database database.Database) (*UserManager, error) {
  return &UserManager{
    database: database,
    users:    make(map[Username]User),
  }, nil
}

func (m *UserManager) login(ctx context.Context, username string, password string) (*User, error) {
  m.mu.Lock()
  defer m.mu.Unlock()

  pass, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
  if err != nil {
    return nil, err
  }

  userKey, err := m.database.Login(ctx, username, string(pass))
  if err != nil {
    return nil, err
  }

  var name Username
  name = Username(username)
  user := User{
    username: name,
    userKey:  userKey,
    token: AccessToken{
      token:          uuid.NewString(),
      expirationDate: time.Now().Add(time.Minute * 5),
    },
  }
  m.users[name] = user

  return &user, nil
}

func (m *UserManager) createAccount(ctx context.Context, username string, password string, email string) error {
  if len(username) > 100 {
    return fmt.Errorf("username too long")
  }
  if len(password) > 100 {
    return fmt.Errorf("password too long")
  }
  if len(email) > 30 {
    return fmt.Errorf("email too long")
  }
  if len(username) == 0 {
    return fmt.Errorf("no username provided")
  }
  if len(password) == 0 {
    return fmt.Errorf("no password provided")
  }

  userExists, err := m.database.UsernameExists(ctx, username)
  if err != nil {
    return err
  }
  if userExists {
    return fmt.Errorf("user %q already exists", username)
  }

  pass, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
  if err != nil {
    return err
  }

  ok, err := m.database.CreateAccount(ctx, username, string(pass), email)
  if err != nil {
    return err
  }
  if !ok {
    return fmt.Errorf("could not create user %q", username)
  }
  return nil
}

func (m *UserManager) logout(username string) error {
  m.mu.Lock()
  defer m.mu.Unlock()

  var name Username
  name = Username(username)

  if staleUser, ok := m.users[name]; ok {
    staleUser.logout()
    delete(m.users, name)
  }
  return nil
}

func (m *UserManager) getUsers(ctx context.Context, username string) ([]PeerUser, error) {
  m.mu.RLock()
  defer m.mu.RUnlock()

  var peerUsers []PeerUser
  var name Username
  name = Username(username)

  user, ok := m.users[name]
  if !ok {
    return nil, fmt.Errorf("user %q is not currently logged in", username)
  }

  friends, err := m.database.GetFriends(ctx, user.userKey)
  if err != nil {
    return nil, err
  }

  users, err := m.database.GetUsers(ctx)
  if err != nil {
    return nil, err
  }

  for _, peerUser := range users {
    if peerUser != username {
      _, online := m.users[Username(peerUser)]
      _, friend := friends[peerUser]
      peerUsers = append(peerUsers, PeerUser{
        username: peerUser,
        online:   online,
        friend:   friend,
      })
    }
  }
  return peerUsers, nil
}

func (m *UserManager) addFriend(ctx context.Context, username string, friend string) error {
  m.mu.RLock()
  defer m.mu.RUnlock()

  user, ok := m.users[Username(username)]
  if !ok {
    return fmt.Errorf("user %q is not currently logged in", username)
  }

  friendKey, err := m.database.GetUserKey(ctx, friend)
  if err != nil {
    return err
  }

  ok, err = m.database.AddFriend(ctx, user.userKey, friendKey)
  if err != nil {
    return err
  }
  if !ok {
    return fmt.Errorf("could not add friend %q", friend)
  }
  return nil
}

func (m *UserManager) authorize(ctx context.Context, method string) error {
  m.mu.RLock()
  defer m.mu.RUnlock()

  if _, ok := util.PublicGRPCMethods[method]; ok {
    return nil
  }

  userValue, err := util.GetGRPCUsername(ctx)
  if err != nil {
    return status.Errorf(codes.Unauthenticated, err.Error())
  }
  username := Username(userValue)

  accessToken, err := util.GetGRPCAccessToken(ctx)
  if err != nil {
    return status.Errorf(codes.Unauthenticated, err.Error())
  }

  user, ok := m.users[username]
  if !ok {
    return status.Errorf(codes.Unauthenticated, "user is not logged in")
  }
  if user.token.token != accessToken {
    return status.Errorf(codes.Unauthenticated, "invalid authorization token")
  }
  if time.Now().After(user.token.expirationDate) {
    return status.Errorf(codes.Unauthenticated, "expired authorization token")
  }
  return nil
}

func (m *UserManager) UnaryAuthIntercepter() grpc.UnaryServerInterceptor {
  return func(
    ctx context.Context,
    req interface{},
    info *grpc.UnaryServerInfo,
    handler grpc.UnaryHandler,
  ) (interface{}, error) {

    err := m.authorize(ctx, info.FullMethod)
    if err != nil {
      return nil, err
    }

    return handler(ctx, req)
  }
}

func (m *UserManager) StreamAuthIntercepter() grpc.StreamServerInterceptor {
  return func(
    srv interface{},
    stream grpc.ServerStream,
    info *grpc.StreamServerInfo,
    handler grpc.StreamHandler,
  ) error {
    err := m.authorize(stream.Context(), info.FullMethod)
    if err != nil {
      return err
    }

    return handler(srv, stream)
  }
}
