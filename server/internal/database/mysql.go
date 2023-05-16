package database

import (
  "context"
  "database/sql"
  "fmt"
  "time"

  "github.com/go-sql-driver/mysql"
  "github.com/google/uuid"
  "github.com/patrickkenney9801/librarygames/internal/config"
)

type MysqlBackend struct {
  db *sql.DB
}

func (b *MysqlBackend) Login(ctx context.Context, username string, password string) (string, error) {
  ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
  defer cancel()

  stmt, err := b.db.PrepareContext(ctx, "SELECT user_key FROM users WHERE username = ? AND password = ?")
  if err != nil {
    return "", err
  }
  defer stmt.Close()

  var userKey string
  if err := stmt.QueryRowContext(ctx, username, password).Scan(&userKey); err != nil {
    return "", fmt.Errorf("invalid username or password")
  }
  return userKey, nil
}

func (b *MysqlBackend) CreateAccount(ctx context.Context, username string, password string, email string) (bool, error) {
  ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
  defer cancel()

  stmt, err := b.db.PrepareContext(ctx, "INSERT INTO users (?, ?, ?, ?, NOW())")
  if err != nil {
    return false, err
  }
  defer stmt.Close()

  res, err := stmt.ExecContext(ctx, username, password, email, uuid.NewString())
  if err != nil {
    return false, err
  }
  return verifyRowsInserted(res)
}

func (b *MysqlBackend) GetFriends(ctx context.Context, userKey string) (map[string]bool, error) {
  friends := make(map[string]bool)
  ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
  defer cancel()

  stmt, err := b.db.PrepareContext(ctx, "SELECT users.username FROM users RIGHT JOIN friends ON users.user_key = friends.userkey2 WHERE userkey1 = ? ORDER BY last_action_date DESC")
  if err != nil {
    return nil, err
  }
  defer stmt.Close()

  rows, err := stmt.QueryContext(ctx, userKey)
  if err != nil {
    return nil, err
  }
  defer rows.Close()

  for rows.Next() {
    var friend string
    if err := rows.Scan(&friend); err != nil {
      return nil, err
    }
    friends[friend] = true
  }
  if err = rows.Err(); err != nil {
    return nil, err
  }
  return friends, nil
}

func (b *MysqlBackend) GetUsers(ctx context.Context) ([]string, error) {
  var users []string
  ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
  defer cancel()

  stmt, err := b.db.PrepareContext(ctx, "SELECT username FROM users ORDER BY last_action_date DESC")
  if err != nil {
    return nil, err
  }
  defer stmt.Close()

  rows, err := stmt.QueryContext(ctx)
  if err != nil {
    return nil, err
  }
  defer rows.Close()

  for rows.Next() {
    var user string
    if err := rows.Scan(&user); err != nil {
      return nil, err
    }
    users = append(users, user)
  }
  if err = rows.Err(); err != nil {
    return nil, err
  }
  return users, nil
}

func (b *MysqlBackend) GetUserKeyMap(ctx context.Context) (map[string]string, error) {
  var userKeyMap map[string]string
  ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
  defer cancel()

  stmt, err := b.db.PrepareContext(ctx, "SELECT username, user_key FROM users ORDER BY last_action_date DESC")
  if err != nil {
    return nil, err
  }
  defer stmt.Close()

  rows, err := stmt.QueryContext(ctx)
  if err != nil {
    return nil, err
  }
  defer rows.Close()

  for rows.Next() {
    var user string
    var userKey string
    if err := rows.Scan(&user, &userKey); err != nil {
      return nil, err
    }
    userKeyMap[userKey] = user
  }
  if err = rows.Err(); err != nil {
    return nil, err
  }
  return userKeyMap, nil
}

func (b *MysqlBackend) UsernameExists(ctx context.Context, username string) (bool, error) {
  var users int
  ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
  defer cancel()

  stmt, err := b.db.PrepareContext(ctx, "SELECT COUNT(username) FROM users WHERE username = ?")
  if err != nil {
    return false, err
  }
  defer stmt.Close()

  if err := stmt.QueryRowContext(ctx, username).Scan(&users); err != nil {
    return false, err
  }
  return users != 0, nil
}

func (b *MysqlBackend) GetUserKey(ctx context.Context, username string) (string, error) {
  ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
  defer cancel()

  stmt, err := b.db.PrepareContext(ctx, "SELECT user_key FROM users WHERE username = ?")
  if err != nil {
    return "", err
  }
  defer stmt.Close()

  var userKey string
  if err := stmt.QueryRowContext(ctx, username).Scan(&userKey); err != nil {
    return "", err
  }
  return userKey, nil
}

func (b *MysqlBackend) GetUsername(ctx context.Context, userKey string) (string, error) {
  ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
  defer cancel()

  stmt, err := b.db.PrepareContext(ctx, "SELECT username FROM users WHERE user_key = ?")
  if err != nil {
    return "", err
  }
  defer stmt.Close()

  var username string
  if err := stmt.QueryRowContext(ctx, userKey).Scan(&username); err != nil {
    return "", err
  }
  return username, nil
}

func (b *MysqlBackend) AddFriend(ctx context.Context, userKey string, friendKey string) (bool, error) {
  ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
  defer cancel()

  stmt, err := b.db.PrepareContext(ctx, "INSERT INTO friends VALUES (?, ?, NULL)")
  if err != nil {
    return false, err
  }
  defer stmt.Close()

  res, err := stmt.ExecContext(ctx, userKey, friendKey)
  if err != nil {
    return false, err
  }
  return verifyRowsInserted(res)
}

func (b *MysqlBackend) GetGamePlayerKeys(ctx context.Context, gameKey string) (string, string, error) {
  var userKey1 string
  var userKey2 string
  ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
  defer cancel()

  stmt, err := b.db.PrepareContext(ctx, "SELECT userkey1, userkey2 FROM games WHERE game_key = ?")
  if err != nil {
    return "", "", err
  }
  defer stmt.Close()

  if err := stmt.QueryRowContext(ctx, gameKey).Scan(&userKey1, &userKey2); err != nil {
    return "", "", err
  }
  return userKey1, userKey2, nil
}

func (b *MysqlBackend) CreateGame(ctx context.Context, gameType int32, user1Key string, user2Key string, board string) (string, error) {
  gameKey := uuid.NewString()
  ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
  defer cancel()

  stmt, err := b.db.PrepareContext(ctx, "INSERT INTO games VALUES (?, ?, ?, ?, 0, -5, -5, 0, NOW(), ?)")
  if err != nil {
    return "", err
  }
  defer stmt.Close()

  res, err := stmt.ExecContext(ctx, gameKey, user1Key, user2Key, gameType, board)
  if err != nil {
    return "", err
  }
  ok, err := verifyRowsInserted(res)
  if err != nil {
    return "", err
  }
  if !ok {
    return "", fmt.Errorf("game could not be created in MySQL database")
  }
  return gameKey, nil
}

func (b *MysqlBackend) GetGames(ctx context.Context, userKeyMap map[string]string, userKey string) ([]GameMetadata, error) {
  var games []GameMetadata
  ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
  defer cancel()

  stmt, err := b.db.PrepareContext(ctx, "SELECT game_type, game_key, player1_key, player2_key, moves, winner FROM games WHERE player1_key = ? OR player2_key = ? ORDER BY games.last_action_date DESC")
  if err != nil {
    return nil, err
  }
  defer stmt.Close()

  rows, err := stmt.QueryContext(ctx, userKey)
  if err != nil {
    return nil, err
  }
  defer rows.Close()

  for rows.Next() {
    game := GameMetadata{}
    if err := rows.Scan(&game.GameType, &game.GameKey, &game.User1, &game.User2, &game.Moves, &game.Winner); err != nil {
      return nil, err
    }
    user1, user1Ok := userKeyMap[game.User1]
    user2, user2Ok := userKeyMap[game.User2]
    if user1Ok && user2Ok {
      game.User1 = user1
      game.User2 = user2
      games = append(games, game)
    }
  }
  if err = rows.Err(); err != nil {
    return nil, err
  }
  return games, nil
}

func (b *MysqlBackend) GetSpectatorGames(ctx context.Context, userKeyMap map[string]string, userKey string) ([]GameMetadata, error) {
  var games []GameMetadata
  ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
  defer cancel()

  stmt, err := b.db.PrepareContext(ctx, "SELECT game_type, game_key, player1_key, player2_key, moves FROM games WHERE player1_key != ? AND player2_key != ? AND winner = 0 ORDER BY games.last_action_date DESC")
  if err != nil {
    return nil, err
  }
  defer stmt.Close()

  rows, err := stmt.QueryContext(ctx, userKey, userKey)
  if err != nil {
    return nil, err
  }
  defer rows.Close()

  for rows.Next() {
    game := GameMetadata{
      Winner: 0,
    }
    if err := rows.Scan(&game.GameType, &game.GameKey, &game.User1, &game.User2, &game.Moves); err != nil {
      return nil, err
    }
    user1, user1Ok := userKeyMap[game.User1]
    user2, user2Ok := userKeyMap[game.User2]
    if user1Ok && user2Ok {
      game.User1 = user1
      game.User2 = user2
      games = append(games, game)
    }
  }
  if err = rows.Err(); err != nil {
    return nil, err
  }
  return games, nil
}

func (b *MysqlBackend) CreateGoGame(ctx context.Context, gameKey string) (bool, error) {
  ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
  defer cancel()

  stmt, err := b.db.PrepareContext(ctx, "INSERT INTO go VALUES (?, 0, 0, 0, 0)")
  if err != nil {
    return false, err
  }
  defer stmt.Close()

  res, err := stmt.ExecContext(ctx, gameKey)
  if err != nil {
    return false, err
  }
  ok, err := verifyRowsInserted(res)
  if err != nil {
    return false, err
  }
  if !ok {
    return false, fmt.Errorf("go game could not be created in MySQL database")
  }
  return true, nil
}

func (b *MysqlBackend) GetGoGameState(ctx context.Context, gameKey string) (*GoGameState, error) {
  ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
  defer cancel()

  stmt, err := b.db.PrepareContext(ctx, "SELECT game_type, game_key, player1_key, player2_key, moves, winner, last_move, penult_move, board, p1_stones_captured, p2_stones_captured, p1_score, p2_score FROM games RIGHT JOIN go ON games.game_key = go.game_key WHERE games.game_key = ?")
  if err != nil {
    return nil, err
  }
  defer stmt.Close()

  game := &GoGameState{}
  if err := stmt.QueryRowContext(ctx, gameKey).Scan(
    &game.GeneralState.GameType,
    &game.GeneralState.GameKey,
    &game.GeneralState.User1Key,
    &game.GeneralState.User2Key,
    &game.GeneralState.Moves,
    &game.GeneralState.Winner,
    &game.GeneralState.LastMove,
    &game.GeneralState.PenultMove,
    &game.GeneralState.Board,
    &game.P1StonesCaptured,
    &game.P2StonesCaptured,
    &game.P1Score,
    &game.P2Score,
  ); err != nil {
    return nil, err
  }
  return game, nil
}

func (b *MysqlBackend) UpdateGoGame(ctx context.Context, gameState *GoGameState) (bool, error) {
  ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
  defer cancel()

  stmt, err := b.db.PrepareContext(ctx, "UPDATE games SET moves = ?, penult_move = ?, last_move = ?, winner = ?, last_action_date = NOW(), board = ? WHERE game_key = ?")
  if err != nil {
    return false, err
  }
  defer stmt.Close()

  res, err := stmt.ExecContext(
    ctx,
    gameState.GeneralState.Moves,
    gameState.GeneralState.PenultMove,
    gameState.GeneralState.LastMove,
    gameState.GeneralState.Winner,
    gameState.GeneralState.Board,
    gameState.GeneralState.GameKey,
  )
  if err != nil {
    return false, err
  }
  success, err := verifyRowsInserted(res)
  if err != nil {
    return false, err
  }

  stmt, err = b.db.PrepareContext(ctx, "UPDATE go SET p1_stones_captured = ?, p2_stones_captured = ?, p1_score = ?, p2_score = ? WHERE game_key = ?")
  if err != nil {
    return false, err
  }
  defer stmt.Close()

  res, err = stmt.ExecContext(
    ctx,
    gameState.P1StonesCaptured,
    gameState.P2StonesCaptured,
    gameState.P1Score,
    gameState.P2Score,
    gameState.GeneralState.GameKey,
  )
  if err != nil {
    return false, err
  }
  successGo, err := verifyRowsInserted(res)
  if err != nil {
    return false, err
  }
  return success && successGo, nil
}

func verifyRowsInserted(res sql.Result) (bool, error) {
  count, err := res.RowsAffected()
  if err != nil {
    return false, err
  }
  return count != 0, nil
}

func NewMsqlBackend(cfg *config.Config) (*MysqlBackend, error) {
  mysqlConfig := mysql.Config{
    User:      cfg.Database.Username,
    Passwd:    cfg.Database.Password,
    Net:       "tcp",
    Addr:      cfg.Database.Address,
    DBName:    cfg.Database.Name,
    ParseTime: true,
  }

  db, err := sql.Open("mysql", mysqlConfig.FormatDSN())
  if err != nil {
    return nil, err
  }
  db.SetConnMaxLifetime(time.Minute * 3)
  db.SetMaxOpenConns(100)
  db.SetMaxIdleConns(100)

  return &MysqlBackend{
    db: db,
  }, nil
}