package com.nfehs.librarygames.games;

import java.awt.image.BufferedImage;

import com.nfehs.librarygames.Game;
import com.nfehs.librarygames.games.go.Go;

/**
 * This is the parent class for board games
 * @author Patrick Kenney and Syed Quadri
 * @date 8/10/2018
 */

public abstract class BoardGame {
  public static enum GameType {
    INVALID(00), GO9x9(01), GO13x13(02), GO19x19(03);

    private int gameType;
    private GameType(int gameType) {
      this.gameType = gameType;
    }

    public int getType() {
      return gameType;
    }
  }

  public static class GameMetadata {
    public GameType gameType;
    public String gameKey;
    public String user1;
    public String user2;
    public int moves;
    public int winner;

    public String title;
    public boolean playing;
    public boolean userTurn;
    public boolean finished;
    public boolean won;

    public GameMetadata(GameType gameType, String gameKey, String user1, String user2, int moves, int winner, String currentUser) {
      this.gameType = gameType;
      this.gameKey = gameKey;
      this.user1 = user1;
      this.user2 = user2;
      this.moves = moves;
      this.winner = winner;

      this.title = lookupGameName(gameType) + ":        " + user1 + "  vs.  " + user2;
      this.playing = currentUser.equals(user1) || currentUser.equals(user2);

      if (playing) {
        // determine whether it is the logged players turn or not
        boolean player1Turn = moves % 2 == 0;
        this.userTurn = (currentUser.equals(user1) && player1Turn) || (currentUser.equals(user2) && !player1Turn);
        this.finished = winner != 0;
        this.won = false;
        if (finished) {
          boolean player1Won = winner == 1 || winner == 3;
          this.won = (currentUser.equals(user1) && player1Won) || (currentUser.equals(user2) && !player1Won);
        }
      }
    }
  }

  // Universal use
  protected GameType gameType;
  private String gameTitle;
  private String gameName;
  private String player1;
  private String player2;
  private boolean isPlayerTurn;
  private String gameKey;
  private boolean isPlayer1;
  private String winner;
  private String scoreInfo;

  // For use only when on GameScreen
  private char[][] board;
  protected Tile[][] tiles;
  protected Piece[][] pieces;
  private int penultMove;
  private int lastMove;
  private int moves;
  private boolean isPlayer1Turn;
  private boolean player1OnGame;
  private boolean player2OnGame;
  private boolean playerIsSpectating;

  /**
   * Sets basic board game information
   * @param gameKey
   * @param gameType
   * @param player1
   * @param player2
   * @param lastMove
   * @param winner
   * @param player1OnGame
   * @param player2OnGame
   * @param board
   */
  public BoardGame(String gameKey, GameType gameType, String player1, String player2, int moves,
      int penultMove, int lastMove, int winner, boolean player1OnGame, boolean player2OnGame, String board) {
    setGameKey(gameKey);
    setGameType(gameType);
    setGameName(lookupGameName(getGameType()));
    setPlayer1(player1);
    setPlayer2(player2);
    setGameTitle(getGameName() + ":        " + getPlayer1() + "  vs.  " + getPlayer2());
    setPenultMove(penultMove);
    setLastMove(lastMove);
    setMoves(moves);
    setPlayer1OnGame(player1OnGame);
    setPlayer2OnGame(player2OnGame);
    setBoard(board);
    setTiles();
    setPieces();

    // if online, update game info
    if (Game.isOnline()) {
      // if the game has a winner, set it to the winner's username, check if opponent resigned
      updateWinner(winner);

      // check if user is spectating
      setPlayerIsSpectating(!getPlayer1().equals(Game.getPlayer().getUsername()) && !getPlayer2().equals(Game.getPlayer().getUsername()));

      // determine whether it is the logged players turn or not
      setPlayer1Turn(moves % 2 == 0);
      setPlayerTurn(false);
      if (!isPlayerIsSpectating() && (getPlayer1().equals(Game.getPlayer().getUsername()) && isPlayer1Turn())
          || (getPlayer2().equals(Game.getPlayer().getUsername()) && !isPlayer1Turn()))
        setPlayerTurn(true);
      setPlayer1((isPlayer1Turn() && isPlayerTurn()) || (!isPlayer1Turn() && !isPlayerTurn()));
    } else {
      // if offline set basic info
      updateWinner(0);
      setPlayerIsSpectating(false);
      setPlayer1Turn(true);
      setPlayerTurn(true);
      setPlayer1(true);
    }
  }

  /**
   * Updates a board game after receiving a 09 packet or 08 if on game screen
   * @param gameKey
   * @param board
   * @param penultMove
   * @param lastMove
   * @param moves
   * @param winner
   * @param player1OnGame
   * @param player2OnGame
   * @return false if not current game
   */
  public boolean update(String gameKey, String board, int penultMove, int lastMove, int moves, int winner, boolean player1OnGame, boolean player2OnGame) {
    if (!getGameKey().equals(gameKey))
      return false;
    setPenultMove(penultMove);
    setLastMove(lastMove);
    setBoard(board);
    setPieces();
    setPlayer1Turn(moves % 2 == 0);
    setPlayerTurn(!isPlayerIsSpectating() && (getPlayer1().equals(Game.getPlayer().getUsername()) && isPlayer1Turn())
        || (getPlayer2().equals(Game.getPlayer().getUsername()) && !isPlayer1Turn()));
    updateWinner(winner);
    setMoves(moves);
    if (winner == 0)
      setMoves(moves + 1);
    setPlayer1OnGame(player1OnGame);
    setPlayer2OnGame(player2OnGame);

    return true;
  }

  /**
   * Updates winner and resigned
   * @param winner
   */
  public void updateWinner(int winner) {
    setWinner(null);
    setScoreInfo(null);
    if (winner == 1 || winner == 3)
      setWinner(getPlayer1());
    else if (winner == 2 || winner == 4)
      setWinner(getPlayer2());
    if (winner == 3)
      setScoreInfo(getPlayer2() + " resigned");
    else if (winner == 4)
      setScoreInfo(getPlayer1() + " resigned");
  }

  public static void printArray(char[][] arr) {
    for (char[] row : arr) {
      for (char c : row)
        System.out.print(c);
      System.out.println();
    }
  }

  // implement in child classes, for use on GameScreen
  protected abstract void setTiles();
  protected abstract void setPieces();
  public abstract BufferedImage getPlayer1Icon();
  public abstract BufferedImage getPlayer2Icon();
  public abstract BufferedImage[][] getCapturablePieces();
  public abstract int[][] getNumberCapturedPieces();
  public abstract void handleMouseEnterTile(int[] coordinates);
  public abstract void handleMouseLeaveTile();
  public abstract void handleMouseClickTile(int[] coordinates);
  public abstract void handleMouseEnterPiece(int[] coordinates);
  public abstract void handleMouseLeavePiece();
  public abstract void handleMouseClickPiece(int[] coordinates);
  public abstract void handleMouseEnterCapturedPiece(int[] coordinates);
  public abstract void handleMouseLeaveCapturedPiece();
  public abstract void handleMouseClickCapturedPiece(int[] coordinates);

  // implement in child classes, for use in logic
  protected abstract boolean validMove(int x, int y);

  /**
   * Handles validating and executing moves
   * @param gameType
   * @param board1D
   * @param isPlayer1Turn
   * @param penultMove1D
   * @param lastMove1D
   * @param moveFrom1D
   * @param moveTo1D
   * @return
   */
  public static String makeMove(GameType gameType, String board1D, boolean isPlayer1Turn, int penultMove1D, int lastMove1D,
                  int moveFrom1D, int moveTo1D) {
    // handle based on game type
    switch (gameType) {
      case GO9x9:
      case GO13x13:   // get result of move for a Go game
      case GO19x19:   return  Go.makeMove(board1D, isPlayer1Turn, penultMove1D, lastMove1D, moveFrom1D, moveTo1D);
      default:        System.out.println("GAME TYPE NOT FOUND");
                      return null;
    }
  }

  /**
   * Handles all offline moves
   * @param movingFrom
   * @param movingTo
   */
  public void makeMoveOffline(int movingFrom, int movingTo) {
    // first handle resignation case
    if (movingTo == -2)
      if (isPlayer1Turn())
        updateWinner(4);
      else
        updateWinner(3);

    // get new move
    String oldBoard = getBoardAsString();
    String newBoard = BoardGame.makeMove(gameType, oldBoard, moves % 2 == 0, penultMove, lastMove, movingFrom, movingTo);
    setBoard(newBoard);

    // if the game is go
    if (getGameType() == GameType.GO9x9 || getGameType() == GameType.GO13x13 || getGameType() == GameType.GO19x19) {
      // if the user did not pass, calculate score
      if (movingTo > -1) {
        int capturedPieces = -1;  // for use in go games
        // calculate number of captured pieces in a go game
        for (int i = 0; i < oldBoard.length(); i++)
          if (oldBoard.charAt(i) != newBoard.charAt(i))
            capturedPieces++;
        if (getMoves() % 2 == 0)
          ((Go) this).setWhiteStonesCaptured(((Go) this).getWhiteStonesCaptured() + capturedPieces);
        else
          ((Go) this).setBlackStonesCaptured(((Go) this).getBlackStonesCaptured() + capturedPieces);
      }

      // if the user passed, it is end of game if last move was a pass too
      if (movingTo == -1 && getLastMove() == -1) {
        // get player scores
        int[] territoryScores = Go.calculateTerritory(oldBoard);
        ((Go) this).setPlayer1Score(territoryScores[0] + ((Go) this).getWhiteStonesCaptured());

        // apply Komi
        switch (getGameType()) {
          case GO19x19: ((Go) this).setPlayer2Score(territoryScores[1] + ((Go) this).getBlackStonesCaptured() + 6);
                        break;
          case GO13x13: ((Go) this).setPlayer2Score(territoryScores[1] + ((Go) this).getBlackStonesCaptured() + 3);
                        break;
          default:      ((Go) this).setPlayer2Score(territoryScores[1] + ((Go) this).getBlackStonesCaptured() + 1);
        }
        updateWinner(1);
        if (((Go) this).getPlayer2Score() >= ((Go) this).getPlayer1Score())
          updateWinner(2);
        setScoreInfo("Score: " + ((Go) this).getPlayer1Score() + " - " + ((Go) this).getPlayer2Score() + ".5");
      }
    }

    setPieces();
    setPlayer1Turn(!isPlayer1Turn());
    setMoves(getMoves() + 1);
    setPlayer1(!isPlayer1());
    setPenultMove(getLastMove());
    setLastMove(movingTo);
  }

  /**
   * Converts 2D coordinates into 1D
   * @param x
   * @param y
   * @return
   */
  protected int getLinearCoordinate(int x, int y) {
    return getBoard().length * x + y;
  }

  /**
   * Converts 1D coordinates into 2D
   * @param coor
   * @param boardLength
   * @return
   */
  protected static int[] get2DCoordinates(int coor, int boardLength) {
    int[] coors2D = new int[2];
    coors2D[0] = coor / boardLength;
    coors2D[1] = coor % boardLength;
    return coors2D;
  }

  /**
   * Returns a copy of the board for testing or other cases
   * @return
   */
  protected char[][] getBoardCopy() {
    char[][] copy = new char[getBoard().length][getBoard().length];
    for (int i = 0; i < copy.length; i++)
      for (int j = 0; j < copy.length; j++)
        copy[i][j] = getBoard()[i][j];
    return copy;
  }

  /**
   * Returns a padded board given a String
   * @param board
   * @return
   */
  protected static char[][] getPaddedBoard(String board) {
    char[][] reqBoard = getBoard(board);
    char[][] padded = new char[reqBoard.length+2][reqBoard.length+2];
    for (int i = 0; i < padded.length-2; i++)
      for (int j = 0; j < padded.length-2; j++)
        padded[i+1][j+1] = reqBoard[i][j];
    return padded;
  }

  /**
   * Returns a padded copy of the board for testing or other cases
   * The board is surrounded by null characters so indexoutofboundexceptions do not occur
   * @return
   */
  protected char[][] getPaddedBoardCopy() {
    char[][] copy = new char[getBoard().length+2][getBoard().length+2];
    for (int i = 0; i < copy.length-2; i++)
      for (int j = 0; j < copy.length-2; j++)
        copy[i+1][j+1] = getBoard()[i][j];
    return copy;
  }

  /**
   * Returns a padded board in String form
   * @param paddedBoard
   * @return
   */
  protected static String buildBoard(char[][] paddedBoard) {
    // set all $ equal to 0, (capture pieces)
    String board = "";
    for (int i = 1; i < paddedBoard.length-1; i++)
      for (int j = 1; j < paddedBoard.length-1; j++)
        board += paddedBoard[i][j] != '$' ? paddedBoard[i][j] : '0';
    return board;
  }

  /**
   * This method WILL OVERWRITE a given board by replacing the given index with replaceChar
   * as well as any other connected characters of the same type
   * @param board
   * @param x
   * @param y
   * @param target
   * @param replace
   * @return
   */
  protected static char[][] paintBoard(char[][] board, int x, int y, char target, char replace) {
    board[x][y] = replace;
    if (board[x][y+1] == target)
      board = paintBoard(board, x, y+1, target, replace);
    if (board[x][y-1] == target)
      board = paintBoard(board, x, y-1, target, replace);
    if (board[x+1][y] == target)
      board = paintBoard(board, x+1, y, target, replace);
    if (board[x-1][y] == target)
      board = paintBoard(board, x-1, y, target, replace);
    return board;
  }

  /**
   * Builds a brand new game for server
   * @param gameType
   * @return
   */
  public static String createNewBoard(GameType gameType) {
    String board = "";
    switch (gameType) {
      case GO9x9:   // handle game type Go 9x9
                    for (int i = 0; i < 9*9; i++)
                      board += "0";
                    break;
      case GO13x13: // handle game type Go 13x13
                    for (int i = 0; i < 13*13; i++)
                      board += "0";
                    break;
      case GO19x19: // handle game type Go 19x19
                    for (int i = 0; i < 19*19; i++)
                      board += "0";
                    break;
      default:      // error no valid game type, return null
                    return null;
    }
    return board;
  }

  /**
   * Returns the name of the game as a string given its integer type
   * @param gameType
   * @return
   */
  public static String lookupGameName(int gameType) {
    return lookupGameName(lookupGame(gameType));
  }

  /**
   * Returns the name of the game given its enum GameType
   * @param lookupGame
   * @return
   */
  private static String lookupGameName(GameType type) {
    switch (type) {
      case INVALID:        return "";
      case GO9x9:          return "Go 9x9";
      case GO13x13:        return "Go 13x13";
      case GO19x19:        return "Go 19x19";
    }
    return "";
  }

  /**
   * This method returns what type of game the game is
   * @param id String
   * @return
   */
  public static GameType lookupGame(String type) {
    try {
      return lookupGame(Integer.parseInt(type));
    } catch (Exception e) {
      return GameType.INVALID;
    }
  }

  /**
   * This method returns what type of game the game is
   * @param id int
   * @return
   */
  public static GameType lookupGame(int type) {
    for (GameType p : GameType.values()) {
      if (p.getType() == type)
        return p;
    }
    return GameType.INVALID;
  }

  /**
   * @return the gameTitle
   */
  public String getGameTitle() {
    return gameTitle;
  }

  /**
   * @param gameTitle the gameTitle to set
   */
  public void setGameTitle(String gameTitle) {
    this.gameTitle = gameTitle;
  }

  /**
   * @return the player1
   */
  public String getPlayer1() {
    return player1;
  }

  /**
   * @param player1 the player1 to set
   */
  public void setPlayer1(String player1) {
    this.player1 = player1;
  }

  /**
   * @return the player2
   */
  public String getPlayer2() {
    return player2;
  }

  /**
   * @param player2 the player2 to set
   */
  public void setPlayer2(String player2) {
    this.player2 = player2;
  }

  /**
   * @return the gameName
   */
  public String getGameName() {
    return gameName;
  }

  /**
   * @param gameName the gameName to set
   */
  public void setGameName(String gameName) {
    this.gameName = gameName;
  }

  /**
   * @return the gameKey
   */
  public String getGameKey() {
    return gameKey;
  }

  /**
   * @param gameKey the gameKey to set
   */
  public void setGameKey(String gameKey) {
    this.gameKey = gameKey;
  }

  /**
   * @return the isPlayerTurn
   */
  public boolean isPlayerTurn() {
    return isPlayerTurn;
  }

  /**
   * @param isPlayerTurn the isPlayerTurn to set
   */
  public void setPlayerTurn(boolean isPlayerTurn) {
    this.isPlayerTurn = isPlayerTurn;
  }

  /**
   * @return the board
   */
  public char[][] getBoard() {
    return board;
  }

  /**
   * Converts a 1D board String into a 2D char array
   * @param board the board to decompress
   */
  public static char[][] getBoard(String board) {
    // get length of sides
    int arrayLength = (int) Math.sqrt(board.length());

    // build 2D board
    char[][] board2D = new char[arrayLength][arrayLength];
    for (int i = 0; i < board2D.length; i++)
      board2D[i] = board.substring(i*arrayLength, (i+1)*arrayLength).toCharArray();
    return board2D;
  }

  /**
   * @param board the board to set
   */
  public void setBoard(String board) {
    // get length of sides
    int arrayLength = (int) Math.sqrt(board.length());

    // build 2D board
    char[][] board2D = new char[arrayLength][arrayLength];
    for (int i = 0; i < board2D.length; i++)
      board2D[i] = board.substring(i*arrayLength, (i+1)*arrayLength).toCharArray();
    this.board = board2D;
  }

  /**
   * @return
   */
  public String getBoardAsString() {
    String board = "";
    for (char[] row : getBoard())
      for (char piece : row)
        board += piece;
    return board;
  }

  /**
   * @return the lastMove
   */
  public int getLastMove() {
    return lastMove;
  }

  /**
   * @param lastMove the lastMove to set
   */
  public void setLastMove(int lastMove) {
    this.lastMove = lastMove;
  }

  public Tile[][] getTiles() {
    return tiles;
  }

  public Piece[][] getPieces() {
    return pieces;
  }

  /**
   * @return the isPlayer1
   */
  public boolean isPlayer1() {
    return isPlayer1;
  }

  /**
   * @param isPlayer1 the isPlayer1 to set
   */
  public void setPlayer1(boolean isPlayer1) {
    this.isPlayer1 = isPlayer1;
  }

  public GameType getGameType() {
    return gameType;
  }

  public void setGameType(GameType gameType) {
    this.gameType = gameType;
  }

  /**
   * @return the winner
   */
  public String getWinner() {
    return winner;
  }

  /**
   * @param winner the winner to set
   */
  public void setWinner(String winner) {
    this.winner = winner;
  }

  public int getPenultMove() {
    return penultMove;
  }

  public void setPenultMove(int penultMove) {
    this.penultMove = penultMove;
  }

  /**
   * @return the moves
   */
  public int getMoves() {
    return moves;
  }

  /**
   * @param moves the moves to set
   */
  public void setMoves(int moves) {
    this.moves = moves;
  }

  public boolean isPlayer1Turn() {
    return isPlayer1Turn;
  }

  public void setPlayer1Turn(boolean isPlayer1Turn) {
    this.isPlayer1Turn = isPlayer1Turn;
  }

  /**
   * @return the scoreInfo
   */
  public String getScoreInfo() {
    return scoreInfo;
  }

  /**
   * @param scoreInfo the scoreInfo to set
   */
  public void setScoreInfo(String scoreInfo) {
    this.scoreInfo = scoreInfo;
  }

  public boolean isPlayer1OnGame() {
    return player1OnGame;
  }

  public void setPlayer1OnGame(boolean player1OnGame) {
    this.player1OnGame = player1OnGame;
  }

  public boolean isPlayer2OnGame() {
    return player2OnGame;
  }

  public void setPlayer2OnGame(boolean player2OnGame) {
    this.player2OnGame = player2OnGame;
  }

  public boolean isPlayerIsSpectating() {
    return playerIsSpectating;
  }

  public void setPlayerIsSpectating(boolean playerIsSpectating) {
    this.playerIsSpectating = playerIsSpectating;
  }
}
