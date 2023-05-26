package database

import (
	"context"
	"database/sql"
	"fmt"
	"time"

	"github.com/go-sql-driver/mysql"
	"github.com/google/uuid"
	"github.com/patrickkenney9801/librarygames/internal/config"
	"github.com/patrickkenney9801/librarygames/internal/util"
	"golang.org/x/crypto/bcrypt"
	"golang.org/x/exp/slog"
)

type MysqlBackend struct {
	db *sql.DB
}

func (b *MysqlBackend) Login(ctx context.Context, username string, password string) (userKey string, err error) {
	ctx, span := util.GetTracer().Start(ctx, "mysql login")
	defer util.EndSpan(span, &err)
	ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
	defer cancel()

	stmt, err := b.db.PrepareContext(ctx, "SELECT user_key, password FROM users WHERE username = ?")
	if err != nil {
		return "", err
	}
	defer checkStatementClose(stmt)

	var storedPassword string
	if err := stmt.QueryRowContext(ctx, username).Scan(&userKey, &storedPassword); err != nil {
		return "", ErrInvalidCredentials
	}
	if err = bcrypt.CompareHashAndPassword([]byte(storedPassword), []byte(password)); err != nil {
		return "", ErrInvalidCredentials
	}
	return userKey, nil
}

func (b *MysqlBackend) CreateAccount(ctx context.Context, username string, password string, email string) (success bool, err error) {
	ctx, span := util.GetTracer().Start(ctx, "mysql create account")
	defer util.EndSpan(span, &err)
	ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
	defer cancel()

	pass, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	if err != nil {
		return false, err
	}

	stmt, err := b.db.PrepareContext(ctx, "INSERT INTO users VALUES (?, ?, ?, ?, NOW())")
	if err != nil {
		return false, err
	}
	defer checkStatementClose(stmt)

	res, err := stmt.ExecContext(ctx, username, string(pass), email, uuid.NewString())
	if err != nil {
		return false, err
	}
	return verifyRowsInserted(res)
}

func (b *MysqlBackend) GetFriends(ctx context.Context, userKey string) (friends map[string]bool, err error) {
	ctx, span := util.GetTracer().Start(ctx, "mysql get friends")
	defer util.EndSpan(span, &err)
	friends = make(map[string]bool)
	ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
	defer cancel()

	stmt, err := b.db.PrepareContext(ctx, "SELECT users.username FROM users RIGHT JOIN friends ON users.user_key = friends.userkey2 WHERE userkey1 = ? ORDER BY last_action_date DESC")
	if err != nil {
		return nil, err
	}
	defer checkStatementClose(stmt)

	rows, err := stmt.QueryContext(ctx, userKey)
	if err != nil {
		return nil, err
	}
	defer checkRowsClose(rows)

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

func (b *MysqlBackend) GetUsers(ctx context.Context) (users []string, err error) {
	ctx, span := util.GetTracer().Start(ctx, "mysql get users")
	defer util.EndSpan(span, &err)
	ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
	defer cancel()

	stmt, err := b.db.PrepareContext(ctx, "SELECT username FROM users ORDER BY last_action_date DESC")
	if err != nil {
		return nil, err
	}
	defer checkStatementClose(stmt)

	rows, err := stmt.QueryContext(ctx)
	if err != nil {
		return nil, err
	}
	defer checkRowsClose(rows)

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

func (b *MysqlBackend) GetUserKeyMap(ctx context.Context) (userKeyMap map[string]string, err error) {
	ctx, span := util.GetTracer().Start(ctx, "mysql get user key map")
	defer util.EndSpan(span, &err)
	userKeyMap = make(map[string]string)
	ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
	defer cancel()

	stmt, err := b.db.PrepareContext(ctx, "SELECT username, user_key FROM users ORDER BY last_action_date DESC")
	if err != nil {
		return nil, err
	}
	defer checkStatementClose(stmt)

	rows, err := stmt.QueryContext(ctx)
	if err != nil {
		return nil, err
	}
	defer checkRowsClose(rows)

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

func (b *MysqlBackend) UsernameExists(ctx context.Context, username string) (exists bool, err error) {
	ctx, span := util.GetTracer().Start(ctx, "mysql username exists")
	defer util.EndSpan(span, &err)
	var users int
	ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
	defer cancel()

	stmt, err := b.db.PrepareContext(ctx, "SELECT COUNT(username) FROM users WHERE username = ?")
	if err != nil {
		return false, err
	}
	defer checkStatementClose(stmt)

	if err := stmt.QueryRowContext(ctx, username).Scan(&users); err != nil {
		return false, err
	}
	return users != 0, nil
}

func (b *MysqlBackend) GetUserKey(ctx context.Context, username string) (userKey string, err error) {
	ctx, span := util.GetTracer().Start(ctx, "mysql get user key")
	defer util.EndSpan(span, &err)
	ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
	defer cancel()

	stmt, err := b.db.PrepareContext(ctx, "SELECT user_key FROM users WHERE username = ?")
	if err != nil {
		return "", err
	}
	defer checkStatementClose(stmt)

	if err := stmt.QueryRowContext(ctx, username).Scan(&userKey); err != nil {
		return "", err
	}
	return userKey, nil
}

func (b *MysqlBackend) GetUsername(ctx context.Context, userKey string) (username string, err error) {
	ctx, span := util.GetTracer().Start(ctx, "mysql get username")
	defer util.EndSpan(span, &err)
	ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
	defer cancel()

	stmt, err := b.db.PrepareContext(ctx, "SELECT username FROM users WHERE user_key = ?")
	if err != nil {
		return "", err
	}
	defer checkStatementClose(stmt)

	if err := stmt.QueryRowContext(ctx, userKey).Scan(&username); err != nil {
		return "", err
	}
	return username, nil
}

func (b *MysqlBackend) AddFriend(ctx context.Context, userKey string, friendKey string) (success bool, err error) {
	ctx, span := util.GetTracer().Start(ctx, "mysql add friend")
	defer util.EndSpan(span, &err)
	ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
	defer cancel()

	stmt, err := b.db.PrepareContext(ctx, "INSERT INTO friends VALUES (?, ?, NULL)")
	if err != nil {
		return false, err
	}
	defer checkStatementClose(stmt)

	res, err := stmt.ExecContext(ctx, userKey, friendKey)
	if err != nil {
		return false, err
	}
	return verifyRowsInserted(res)
}

func (b *MysqlBackend) GetGamePlayerKeys(ctx context.Context, gameKey string) (userKey1 string, userKey2 string, err error) {
	ctx, span := util.GetTracer().Start(ctx, "mysql get game player keys")
	defer util.EndSpan(span, &err)
	ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
	defer cancel()

	stmt, err := b.db.PrepareContext(ctx, "SELECT player1_key, player2_key FROM games WHERE game_key = ?")
	if err != nil {
		return "", "", err
	}
	defer checkStatementClose(stmt)

	if err := stmt.QueryRowContext(ctx, gameKey).Scan(&userKey1, &userKey2); err != nil {
		return "", "", err
	}
	return userKey1, userKey2, nil
}

func (b *MysqlBackend) CreateGame(ctx context.Context, gameType int32, user1Key string, user2Key string, board string) (gameKey string, err error) {
	ctx, span := util.GetTracer().Start(ctx, "mysql create game")
	defer util.EndSpan(span, &err)
	gameKey = uuid.NewString()
	ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
	defer cancel()

	stmt, err := b.db.PrepareContext(ctx, "INSERT INTO games VALUES (?, ?, ?, ?, 0, -5, -5, 0, NOW(), ?)")
	if err != nil {
		return "", err
	}
	defer checkStatementClose(stmt)

	res, err := stmt.ExecContext(ctx, gameKey, user1Key, user2Key, gameType, board)
	if err != nil {
		return "", err
	}
	ok, err := verifyRowsInserted(res)
	if err != nil {
		return "", err
	}
	if !ok {
		err = fmt.Errorf("game could not be created in MySQL database")
		return "", err
	}
	return gameKey, nil
}

func (b *MysqlBackend) GetGames(ctx context.Context, userKeyMap map[string]string, userKey string) (games []GameMetadata, err error) {
	ctx, span := util.GetTracer().Start(ctx, "mysql get games")
	defer util.EndSpan(span, &err)
	ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
	defer cancel()

	stmt, err := b.db.PrepareContext(ctx, "SELECT game_type, game_key, player1_key, player2_key, moves, winner FROM games WHERE player1_key = ? OR player2_key = ? ORDER BY games.last_action_date DESC")
	if err != nil {
		return nil, err
	}
	defer checkStatementClose(stmt)
	games, err = extractGames(ctx, stmt, userKeyMap, userKey)
	return games, err
}

func (b *MysqlBackend) GetSpectatorGames(ctx context.Context, userKeyMap map[string]string, userKey string) (games []GameMetadata, err error) {
	ctx, span := util.GetTracer().Start(ctx, "mysql get spectator games")
	defer util.EndSpan(span, &err)
	ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
	defer cancel()

	stmt, err := b.db.PrepareContext(ctx, "SELECT game_type, game_key, player1_key, player2_key, moves, winner FROM games WHERE player1_key != ? AND player2_key != ? AND winner = 0 ORDER BY games.last_action_date DESC")
	if err != nil {
		return nil, err
	}
	defer checkStatementClose(stmt)
	games, err = extractGames(ctx, stmt, userKeyMap, userKey)
	return games, err
}

func (b *MysqlBackend) CreateGoGame(ctx context.Context, gameKey string) (success bool, err error) {
	ctx, span := util.GetTracer().Start(ctx, "mysql create go game")
	defer util.EndSpan(span, &err)
	ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
	defer cancel()

	stmt, err := b.db.PrepareContext(ctx, "INSERT INTO go VALUES (?, 0, 0, 0, 0)")
	if err != nil {
		return false, err
	}
	defer checkStatementClose(stmt)

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

func (b *MysqlBackend) GetGoGameState(ctx context.Context, gameKey string) (game *GoGameState, err error) {
	ctx, span := util.GetTracer().Start(ctx, "mysql get go game state")
	defer util.EndSpan(span, &err)
	ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
	defer cancel()

	stmt, err := b.db.PrepareContext(ctx, "SELECT game_type, games.game_key, player1_key, player2_key, moves, winner, last_move, penult_move, board, p1_stones_captured, p2_stones_captured, p1_score, p2_score FROM games RIGHT JOIN go ON games.game_key = go.game_key WHERE games.game_key = ?")
	if err != nil {
		return nil, err
	}
	defer checkStatementClose(stmt)

	game = &GoGameState{}
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

func (b *MysqlBackend) UpdateGoGame(ctx context.Context, gameState *GoGameState) (success bool, err error) {
	ctx, span := util.GetTracer().Start(ctx, "mysql update go game")
	defer util.EndSpan(span, &err)
	ctx, cancel := context.WithTimeout(ctx, 15*time.Second)
	defer cancel()

	stmt, err := b.db.PrepareContext(ctx, "UPDATE games SET moves = ?, penult_move = ?, last_move = ?, winner = ?, last_action_date = NOW(), board = ? WHERE game_key = ?")
	if err != nil {
		return false, err
	}
	defer checkStatementClose(stmt)

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
	success, err = verifyRowsInserted(res)
	if err != nil {
		return false, err
	}

	stmt, err = b.db.PrepareContext(ctx, "UPDATE go SET p1_stones_captured = ?, p2_stones_captured = ?, p1_score = ?, p2_score = ? WHERE game_key = ?")
	if err != nil {
		return false, err
	}
	defer checkStatementClose(stmt)

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
	_, err = verifyRowsInserted(res)
	if err != nil {
		return false, err
	}
	return success, nil
}

func extractGames(ctx context.Context, stmt *sql.Stmt, userKeyMap map[string]string, userKey string) (games []GameMetadata, err error) {
	rows, err := stmt.QueryContext(ctx, userKey, userKey)
	if err != nil {
		return nil, err
	}
	defer checkRowsClose(rows)

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

func verifyRowsInserted(res sql.Result) (bool, error) {
	count, err := res.RowsAffected()
	if err != nil {
		return false, err
	}
	return count != 0, nil
}

func checkStatementClose(stmt *sql.Stmt) {
	if err := stmt.Close(); err != nil {
		slog.Warn("closing statement failed", slog.String("err", err.Error()))
	}
}

func checkRowsClose(rows *sql.Rows) {
	if err := rows.Close(); err != nil {
		slog.Warn("closing rows failed", slog.String("err", err.Error()))
	}
}

func NewMsqlBackend(cfg *config.Config) (*MysqlBackend, error) {
	mysqlConfig := mysql.Config{
		User:                 cfg.Database.Username,
		Passwd:               cfg.Database.Password,
		Net:                  "tcp",
		Addr:                 cfg.Database.Address,
		DBName:               cfg.Database.Name,
		AllowNativePasswords: true,
		ParseTime:            true,
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
