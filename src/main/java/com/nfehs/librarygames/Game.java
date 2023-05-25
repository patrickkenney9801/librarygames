package com.nfehs.librarygames;

import com.nfehs.librarygames.games.BoardGame;
import com.nfehs.librarygames.net.GameClient;
import com.nfehs.librarygames.screens.*;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.JFrame;

/**
 * This class hosts the game flow
 *
 * @author Patrick Kenney, Syed Quadri
 * @date 6/13/2018
 */
public class Game {
  public static GameFrame gameFrame;
  public static JFrame window;
  public static Container mainWindow;
  public static Dimension screenSize;

  public static Screen screen;

  private static GameClient client;
  private static Player player;
  private static BoardGame boardGame;

  public static final String DEFAULT_SERVER_ADDRESS = "127.0.0.1:19602";

  public static final int LOGIN = 0;
  public static final int CREATE_ACCOUNT = 1;
  public static final int ACTIVE_GAMES = 2;
  public static final int CREATE_GAME = 3;
  public static final int PLAYING_GAME = 4;
  public static final int SPECTATOR_GAMES = 5;
  public static final int CREATE_OFFLINE_GAME = 6;
  public static final int OVER = 10;

  public static int gameState = Game.LOGIN;
  public static boolean gamePlaying = true;
  public static boolean online = false;

  public Game() {
    client = new GameClient(getServerAddress());
  }

  /**
   * This method logs in the user online
   *
   * @param username
   * @param password
   */
  public static void login(String username, char[] pass) {
    // verify that valid data is given
    if (username == null || username.length() < 1) {
      setErrorLoginScreen("ERROR: NO USERNAME PROVIDED");
      return;
    } else if (pass == null || pass.length < 1) {
      setErrorLoginScreen("ERROR: NO PASSWORD PROVIDED");
      return;
    } else if (username.length() > 20) {
      setErrorLoginScreen("ERROR: USERNAMES ARE 20 CHARACTERS OR LESS");
      return;
    } else if (pass.length > 20) {
      setErrorLoginScreen("ERROR: PASSWORDS ARE 20 CHARACTERS OR LESS");
      return;
    }

    String password = new String(pass);
    client.login(username, password);
  }

  /**
   * This method attempts to create a new account
   *
   * @param username
   * @param email
   * @param password
   * @param password2
   */
  public static void createAccount(String username, String email, char[] pass, char[] pass2) {
    // verify that data given is valid
    if (username == null || username.length() < 1) {
      setErrorCreateAccountScreen("ERROR: PLEASE PROVIDE A USERNAME");
      return;
    } else if (pass == null || pass.length < 1) {
      setErrorCreateAccountScreen("ERROR: PLEASE PROVIDE A PASSWORD");
      return;
    } else if (username.length() > 20) {
      setErrorCreateAccountScreen("ERROR: USERNAMES CANNOT EXCEED 20 CHARACTERS");
      return;
    } else if (pass.length > 20) {
      setErrorCreateAccountScreen("ERROR: PASSWORDS CANNOT EXCEED 20 CHARACTERS");
      return;
    } else if (pass.length != pass2.length) {
      setErrorCreateAccountScreen("ERROR: PASSWORDS DO NOT MATCH");
      return;
    } else if (email.contains(":")) {
      setErrorCreateAccountScreen("ERROR: EMAILS MAY NOT CONTAIN ':'");
      return;
    }
    // if the user contains a ,: or ~ set error and exit
    for (char c : username.toCharArray())
      if (c == '~' || c == ',' || c == ':') {
        setErrorCreateAccountScreen("ERROR: USERNAMES MAY NOT CONTAIN ,: or ~");
        return;
      }

    for (int i = 0; i < pass.length; i++) {
      if (pass[i] != pass2[i]) {
        setErrorCreateAccountScreen("ERROR: PASSWORDS DO NOT MATCH");
        return;
      }
    }
    String password = new String(pass);
    client.createAccount(username, password, email);
  }

  /** This method attempts to log the user out of the server */
  public static void logout() {
    client.logout();
  }

  /** This method attempts to retrieve friends and other players for CreateGameScreen */
  public static void getOtherPlayers() {
    client.getUsers();
  }

  /**
   * This method sends a friend request to server
   *
   * @param friend
   */
  public static void addFriend(String friend) {
    client.addFriend(friend);
  }

  /**
   * This method sends a create game request to server
   *
   * @param otherUser
   * @param creatorGoesFirst
   * @param gameType
   */
  public static void createGame(
      String otherUser, boolean creatorGoesFirst, BoardGame.GameType gameType) {
    client.createGame(otherUser, creatorGoesFirst, gameType);
  }

  /** This method attempts to retrieve games for ActiveGamesScreen */
  public static void getActiveGames() {
    client.getGames();
  }

  /**
   * This method attempts to play a game Ultimately leads to opening GameScreen
   *
   * @param gameKey
   */
  public static void startPlaying(BoardGame.GameMetadata gameMetadata) {
    switch (gameMetadata.gameType) {
      case GO9x9:
      case GO13x13:
      case GO19x19:
        client.startGo(gameMetadata);
        break;
      default: // handle invalid game type
        System.out.println("ERROR INVALID GAME TYPE");
        return;
    }
    client.chat(gameMetadata);
  }

  public static void stopPlaying() {
    client.stopChatStream();
    switch (getBoardGame().getGameType()) {
      case GO9x9:
      case GO13x13:
      case GO19x19:
        client.stopGoStream();
        break;
      default: // handle invalid game type
        return;
    }
  }

  /**
   * This method attempts to send a move to the server
   *
   * @param movingFrom
   * @param movingTo
   */
  public static void sendMove(int movingFrom, int movingTo) {
    // if the user is online, send move to server, else offline server
    if (isOnline()) {
      switch (getBoardGame().getGameType()) {
        case GO9x9:
        case GO13x13:
        case GO19x19:
          client.sendGoMove(getBoardGame().getGameKey(), movingFrom, movingTo);
          break;
        default: // handle invalid game type
          System.out.println("ERROR INVALID GAME TYPE");
          return;
      }
    } else {
      getBoardGame().makeMoveOffline(movingFrom, movingTo);
      updateGameBoard();
    }
  }

  /**
   * This method attempts to send a line of text to their opponent
   *
   * @param sendToSpectators
   * @param text
   */
  public static void sendChat(boolean sendToSpectators, String text) {
    client.sendChat(getBoardGame().getGameKey(), text, sendToSpectators);
  }

  /** This method attempts to retrieve games for SpectatorGamesScreen */
  public static void getSpectatorGames() {
    client.getSpectatorGames();
  }

  /** This method closes the screen for the user */
  public static void exitCurrentScreen() {
    if (screen != null) screen.exit();
  }

  /** This method opens up the login screen for the user */
  public static void openLoginScreen() {
    exitCurrentScreen();
    screen = new LoginScreen();
    gameState = LOGIN;
    setOnline(true);
  }

  /** This method opens up the create account screen for the user */
  public static void openCreateAccountScreen() {
    exitCurrentScreen();
    screen = new CreateAccountScreen();
    gameState = CREATE_ACCOUNT;
    setOnline(true);
  }

  /** This method opens up the active games screen for the user */
  public static void openActiveGamesScreen() {
    exitCurrentScreen();
    screen = new ActiveGamesScreen();
    gameState = ACTIVE_GAMES;
    getActiveGames();
  }

  /** This method opens up the create game screen for the user */
  public static void openCreateGameScreen() {
    exitCurrentScreen();
    screen = new CreateGameScreen();
    gameState = CREATE_GAME;
    getOtherPlayers();
  }

  /** This method opens up the game screen for the user */
  public static void openGameScreen() {
    exitCurrentScreen();
    screen = new GameScreen();
    gameState = PLAYING_GAME;
  }

  /** This method opens up the spectator games screen for user */
  public static void openSpectatorGamesScreen() {
    exitCurrentScreen();
    screen = new SpectatorGamesScreen();
    gameState = SPECTATOR_GAMES;
    getSpectatorGames();
  }

  /** This method opens up the create offline game screen for user */
  public static void openCreateOfflineGameScreen() {
    exitCurrentScreen();
    screen = new CreateOfflineGameScreen();
    gameState = CREATE_OFFLINE_GAME;
    setOnline(false);
  }

  /** This method refreshes the current screen, used when screen resized */
  public static void refresh() {
    // make sure screen is defined
    if (screen == null) return;

    switch (gameState) {
      case LOGIN:
        openLoginScreen();
        break;
      case CREATE_ACCOUNT:
        openCreateAccountScreen();
        break;
      case ACTIVE_GAMES:
        openActiveGamesScreen();
        break;
      case CREATE_GAME:
        openCreateGameScreen();
        break;
      case PLAYING_GAME:
        openGameScreen();
        break;
      case SPECTATOR_GAMES:
        openSpectatorGamesScreen();
        break;
      case CREATE_OFFLINE_GAME:
        openCreateOfflineGameScreen();
        break;
    }
  }

  /** This method updates the active games list on the ActiveGamesScreen */
  public static void updateActiveGamesList() {
    if (!(screen instanceof ActiveGamesScreen)) {
      System.out.println("GAMESTATE ERROR update active games list called on wrong screen");
      return;
    }
    ((ActiveGamesScreen) screen).loadActiveGames();
  }

  /** This method updates the players list on the CreateGameScreen */
  public static void updatePlayersList() {
    if (!(screen instanceof CreateGameScreen)) {
      System.out.println("GAMESTATE ERROR update players list called on wrong screen");
      return;
    }
    ((CreateGameScreen) screen).loadPlayers();
  }

  /** This method updates the game board on the GameScreen */
  public static void updateGameBoard() {
    if (!(screen instanceof GameScreen)) {
      System.out.println("GAMESTATE ERROR update game board called on wrong screen");
      return;
    }
    ((GameScreen) screen).updateBoard();
  }

  /**
   * This method updates the game chat on the GameScreen
   *
   * @param sender
   * @param message
   */
  public static void updateGameChat(String sender, String message) {
    if (!(screen instanceof GameScreen)) {
      System.out.println("GAMESTATE ERROR update game chat called on wrong screen");
      return;
    }
    ((GameScreen) screen).updateChat(sender, message);
  }

  /** This method updates the spectator games list on the SpectatorGamesScreen */
  public static void updateSpectatorGamesList() {
    if (!(screen instanceof SpectatorGamesScreen)) {
      System.out.println("GAMESTATE ERROR update spectator games list called on wrong screen");
      return;
    }
    ((SpectatorGamesScreen) screen).loadSpectatorGames();
  }

  /**
   * Verifies that the client is on LoginScreen and then sets the error to errorMessage Called by
   * Game and GameClient classes
   *
   * @param errorMessage
   */
  public static void setErrorLoginScreen(String errorMessage) {
    if (!(screen instanceof LoginScreen)) return;
    ((LoginScreen) screen).setError(errorMessage);
  }

  /**
   * Verifies that the client is on CreateAccountScreen and then sets the error to errorMessage
   * Called by Game and GameClient classes
   *
   * @param errorMessage
   */
  public static void setErrorCreateAccountScreen(String errorMessage) {
    if (!(screen instanceof CreateAccountScreen)) return;
    ((CreateAccountScreen) screen).setError(errorMessage);
  }

  /**
   * Verifies that the client is on CreateGameScreen and then sets the error to errorMessage Called
   * by Game and GameClient classes
   *
   * @param errorMessage
   */
  public static void setErrorCreateGameScreen(String errorMessage) {
    if (!(screen instanceof CreateGameScreen)) return;
    ((CreateGameScreen) screen).setError(errorMessage);
  }

  /**
   * @return the player
   */
  public static Player getPlayer() {
    return player;
  }

  /**
   * @param player the player to set
   */
  public static void setPlayer(Player player) {
    Game.player = player;
  }

  /**
   * @return the boardGame
   */
  public static BoardGame getBoardGame() {
    return boardGame;
  }

  /**
   * @param boardGame the boardGame to set
   */
  public static void setBoardGame(BoardGame boardGame) {
    Game.boardGame = boardGame;
  }

  public static boolean isOnline() {
    return online;
  }

  public static void setOnline(boolean online) {
    Game.online = online;
  }

  private static String getServerAddress() {
    String serverAddress = System.getenv("LIBRARY_GAMES_SERVER_ADDRESS");
    if (serverAddress == null) {
      return DEFAULT_SERVER_ADDRESS;
    }
    return serverAddress;
  }
}
