package account

import (
  "context"
  "fmt"
  "math/rand"
  "testing"
  "time"

  "github.com/patrickkenney9801/librarygames/internal/database"
)

const (
  letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
)

func TestLogin(t *testing.T) {
  var err error
  ctx := context.Background()
  db := database.NewTestDatabase()
  userManager, err := NewUserManager(db)
  if err != nil {
    t.Fatal(err)
  }
  username := "hi"
  password := "hihi"
  userKey := "9801"
  badCredentials := fmt.Errorf("bad credentials")
  success := database.TestStringError{
    S: userKey,
    E: nil,
  }
  fail := database.TestStringError{
    S: "",
    E: badCredentials,
  }
  db.LoginResult <- success
  db.LoginResult <- success
  db.LoginResult <- fail

  if err = expectUsers(userManager, 0); err != nil {
    t.Fatal(err)
  }

  user, err := userManager.login(ctx, username, password)
  if err != nil {
    t.Fatal(err)
  }
  if user.userKey != userKey {
    t.Fatal(fmt.Errorf("expected user key %q, got %q", userKey, user.userKey))
  }
  if user.username != Username(username) {
    t.Fatal(fmt.Errorf("expected username %q, got %q", username, user.username))
  }
  if time.Now().After(user.token.expirationDate) {
    t.Fatal(fmt.Errorf("user token has already expired"))
  }
  if err = expectUsers(userManager, 1); err != nil {
    t.Fatal(err)
  }

  userRelogged, err := userManager.login(ctx, username, password)
  if err != nil {
    t.Fatal(err)
  }
  if userRelogged.userKey != userKey {
    t.Fatal(fmt.Errorf("expected relogged user key %q, got %q", userKey, user.userKey))
  }
  if userRelogged.username != Username(username) {
    t.Fatal(fmt.Errorf("expected relogged username %q, got %q", username, user.username))
  }
  if userRelogged.token.token == user.token.token {
    t.Fatal(fmt.Errorf("relogged token unexpectedly matches stale user token"))
  }
  if time.Now().After(user.token.expirationDate) {
    t.Fatal(fmt.Errorf("relogged user token has already expired"))
  }
  if err = expectUsers(userManager, 1); err != nil {
    t.Fatal(err)
  }

  _, err = userManager.login(ctx, username, password)
  if err != badCredentials {
    t.Fatal(fmt.Errorf("failed login attempt succeeded or returned wrong error %v", err))
  }
  if err = expectUsers(userManager, 1); err != nil {
    t.Fatal(err)
  }
}

func TestCreateAccount(t *testing.T) {
  var err error
  ctx := context.Background()
  db := database.NewTestDatabase()
  userManager, err := NewUserManager(db)
  if err != nil {
    t.Fatal(err)
  }
  username := "hi"
  password := "hihi"
  email := "hi@hi"
  usernameExists := database.TestBoolError{
    B: true,
    E: nil,
  }
  usernameDoesNotExist := database.TestBoolError{
    B: false,
    E: nil,
  }
  createdAccount := database.TestBoolError{
    B: true,
    E: nil,
  }
  db.UsernameExistsResult <- usernameExists
  db.UsernameExistsResult <- usernameDoesNotExist
  db.CreateAccountResult <- createdAccount

  err = userManager.createAccount(ctx, "", password, email)
  if err == nil {
    t.Fatal(fmt.Errorf("created account with empty username"))
  }
  err = userManager.createAccount(ctx, username, "", email)
  if err == nil {
    t.Fatal(fmt.Errorf("created account with empty password"))
  }
  err = userManager.createAccount(ctx, randomString(101), password, email)
  if err == nil {
    t.Fatal(fmt.Errorf("created account with too long username"))
  }
  err = userManager.createAccount(ctx, username, randomString(101), email)
  if err == nil {
    t.Fatal(fmt.Errorf("created account with too long password"))
  }

  err = userManager.createAccount(ctx, username, password, email)
  if err == nil {
    t.Fatal(fmt.Errorf("created account with non-unique username"))
  }
  err = userManager.createAccount(ctx, username, password, "")
  if err != nil {
    t.Fatal(err)
  }
  if err = expectUsers(userManager, 0); err != nil {
    t.Fatal(err)
  }
}

func TestLogout(t *testing.T) {
  var err error
  ctx := context.Background()
  db := database.NewTestDatabase()
  userManager, err := NewUserManager(db)
  if err != nil {
    t.Fatal(err)
  }
  username := "hi"
  password := "hihi"
  userKey := "9801"
  res := database.TestStringError{
    S: userKey,
    E: nil,
  }
  username2 := "bye"
  password2 := "byebye"
  userKey2 := "19602"
  res2 := database.TestStringError{
    S: userKey2,
    E: nil,
  }
  db.LoginResult <- res
  db.LoginResult <- res
  db.LoginResult <- res2
  db.LoginResult <- res

  if err = expectUsers(userManager, 0); err != nil {
    t.Fatal(err)
  }
  _ = userManager.logout(username)
  if err = expectUsers(userManager, 0); err != nil {
    t.Fatal(err)
  }

  _, err = userManager.login(ctx, username, password)
  if err != nil {
    t.Fatal(err)
  }
  if err = expectUsers(userManager, 1); err != nil {
    t.Fatal(err)
  }

  _, err = userManager.login(ctx, username, password)
  if err != nil {
    t.Fatal(err)
  }
  if err = expectUsers(userManager, 1); err != nil {
    t.Fatal(err)
  }
  _, err = userManager.login(ctx, username2, password2)
  if err != nil {
    t.Fatal(err)
  }
  if err = expectUsers(userManager, 2); err != nil {
    t.Fatal(err)
  }

  _ = userManager.logout(username)
  if err = expectUsers(userManager, 1); err != nil {
    t.Fatal(err)
  }
  if err = expectUsernames(userManager, username2); err != nil {
    t.Fatal(err)
  }

  _, err = userManager.login(ctx, username, password)
  if err != nil {
    t.Fatal(err)
  }
  if err = expectUsers(userManager, 2); err != nil {
    t.Fatal(err)
  }
  if err = expectUsernames(userManager, username, username2); err != nil {
    t.Fatal(err)
  }
  _ = userManager.logout(username)
  if err = expectUsers(userManager, 1); err != nil {
    t.Fatal(err)
  }
  if err = expectUsernames(userManager, username2); err != nil {
    t.Fatal(err)
  }
  _ = userManager.logout(username2)
  if err = expectUsers(userManager, 0); err != nil {
    t.Fatal(err)
  }
}

func TestGetUsers(t *testing.T) {
  var err error
  ctx := context.Background()
  db := database.NewTestDatabase()
  userManager, err := NewUserManager(db)
  if err != nil {
    t.Fatal(err)
  }

  activeUsers, err := loginUsers(db, userManager, 1)
  if err != nil {
    t.Fatal(err)
  }
  activeUser := string(activeUsers[0].username)
  users, err := loginUsers(db, userManager, 5)
  if err != nil {
    t.Fatal(err)
  }
  for _, user := range users {
    db.GetFriendsResultMap[string(user.username)] = true
    db.GetUsersResultArray = append(db.GetUsersResultArray, string(user.username))
  }
  offlineUser := randomString(15)
  db.GetUsersResultArray = append(db.GetUsersResultArray, offlineUser)
  offlineFriend := randomString(15)
  db.GetFriendsResultMap[offlineFriend] = true
  db.GetUsersResultArray = append(db.GetUsersResultArray, offlineFriend)

  db.GetFriendsResult <- nil
  db.GetUsersResult <- nil
  db.GetFriendsResult <- nil
  db.GetUsersResult <- nil

  _, err = userManager.getUsers(ctx, randomString(15))
  if err == nil {
    t.Fatal(fmt.Errorf("got users with account not signed in"))
  }

  others, err := userManager.getUsers(ctx, activeUser)
  if err != nil {
    t.Fatal(err)
  }
  for _, other := range others {
    friend := other.username != offlineUser
    online := other.username != offlineUser && other.username != offlineFriend
    if other.friend != friend {
      t.Fatal(fmt.Errorf("%q friendship incorrect, expected %v", other.username, friend))
    }
    if other.online != online {
      t.Fatal(fmt.Errorf("%q online status incorrect, expected %v", other.username, online))
    }
  }

  loggedOutUser := string(users[0].username)
  _ = userManager.logout(loggedOutUser)
  others, err = userManager.getUsers(ctx, activeUser)
  if err != nil {
    t.Fatal(err)
  }
  for _, other := range others {
    friend := other.username != offlineUser
    online := other.username != offlineUser && other.username != offlineFriend && other.username != loggedOutUser
    if other.friend != friend {
      t.Fatal(fmt.Errorf("%q friendship incorrect, expected %v", other.username, friend))
    }
    if other.online != online {
      t.Fatal(fmt.Errorf("%q online status incorrect, expected %v", other.username, online))
    }
  }
}

func TestAddFriends(t *testing.T) {
  var err error
  ctx := context.Background()
  db := database.NewTestDatabase()
  userManager, err := NewUserManager(db)
  if err != nil {
    t.Fatal(err)
  }

  activeUsers, err := loginUsers(db, userManager, 1)
  if err != nil {
    t.Fatal(err)
  }
  activeUser := string(activeUsers[0].username)

  addFriendKeyError := fmt.Errorf("get user key errored")
  addFriendError := fmt.Errorf("add friend errored")
  failAtKey := database.TestStringError{
    S: "",
    E: addFriendKeyError,
  }
  successKey := database.TestStringError{
    S: randomString(15),
    E: nil,
  }
  success := database.TestBoolError{
    B: true,
    E: nil,
  }
  failKey := database.TestStringError{
    S: randomString(15),
    E: nil,
  }
  fail := database.TestBoolError{
    B: false,
    E: nil,
  }
  erroredKey := database.TestStringError{
    S: randomString(15),
    E: nil,
  }
  errored := database.TestBoolError{
    B: false,
    E: addFriendError,
  }

  db.GetUserKeyResult <- failAtKey

  db.GetUserKeyResult <- successKey
  db.AddFriendResult <- success
  db.GetUserKeyResult <- failKey
  db.AddFriendResult <- fail
  db.GetUserKeyResult <- erroredKey
  db.AddFriendResult <- errored

  if err = userManager.addFriend(ctx, activeUser, randomString(15)); err != addFriendKeyError {
    t.Fatal(fmt.Errorf("incorrect or no error %q returned when getting user key errored", err))
  }
  if err = userManager.addFriend(ctx, randomString(15), randomString(15)); err == nil {
    t.Fatal(fmt.Errorf("added friend with account not signed in"))
  }
  if err = userManager.addFriend(ctx, activeUser, randomString(15)); err != nil {
    t.Fatal(err)
  }
  if err = userManager.addFriend(ctx, activeUser, randomString(15)); err == nil {
    t.Fatal(fmt.Errorf("no error returned when adding friend failed"))
  }
  if err = userManager.addFriend(ctx, activeUser, randomString(15)); err != addFriendError {
    t.Fatal(fmt.Errorf("incorrect or no error %q returned when adding friend errored", err))
  }
}

func expectUsernames(userManager *UserManager, users ...string) error {
  for _, user := range users {
    if _, ok := userManager.users[Username(user)]; !ok {
      return fmt.Errorf("%q is not logged in", user)
    }
  }
  return nil
}

func expectUsers(userManager *UserManager, count int) error {
  if len(userManager.users) != count {
    return fmt.Errorf("user manager has %q users logged in, expected %q", len(userManager.users), count)
  }
  return nil
}

func loginUsers(db *database.TestDatabase, userManager *UserManager, count int) ([]*User, error) {
  users := make([]*User, count)
  for i := range users {
    success := database.TestStringError{
      S: randomString(15),
      E: nil,
    }
    db.LoginResult <- success
    user, err := userManager.login(context.Background(), randomString(15), randomString(15))
    if err != nil {
      return nil, err
    }
    users[i] = user
  }
  return users, nil
}

func randomString(length int) string {
  b := make([]byte, length)
  for i := range b {
    b[i] = letters[rand.Intn(len(letters))]
  }
  return string(b)
}
