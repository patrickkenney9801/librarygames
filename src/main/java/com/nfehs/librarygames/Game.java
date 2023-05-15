package com.nfehs.librarygames;

import java.awt.Container;
import java.awt.Dimension;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.nfehs.librarygames.games.BoardGame;
import com.nfehs.librarygames.net.GameClient;
import com.nfehs.librarygames.net.Security;
import com.nfehs.librarygames.net.packets.*;
import com.nfehs.librarygames.screens.*;

/**
 * This class hosts the game flow
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

  public static final byte[] DEFAULT_SERVER_IP_ADDRESS = {127, 0, 0, 1};
  public static final int DEFAULT_SERVER_PORT = 19602;

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

  public Game() throws UnknownHostException {
    client = new GameClient(getServerAddress(), getServerPort());
    client.start();
  }

  /**
   * This method logs in the user online
   * @param username
   * @param password
   */
  public static void login(String user, char[] pass) {
    // verify that valid data is given
    if (user == null || user.length() < 1) {
      setErrorLoginScreen("ERROR: NO USERNAME PROVIDED");
      return;
    } else if (pass == null || pass.length < 1) {
      setErrorLoginScreen("ERROR: NO PASSWORD PROVIDED");
      return;
    } else if (user.length() > 20) {
      setErrorLoginScreen("ERROR: USERNAMES ARE 20 CHARACTERS OR LESS");
      return;
    } else if (pass.length > 20) {
      setErrorLoginScreen("ERROR: PASSWORDS ARE 20 CHARACTERS OR LESS");
      return;
    }

    // encrypt username and password
    String username = Security.encrypt(user);
    String password = "";
    for (char c : pass)
      password += (c + 15);
    password = Security.encrypt(password);

    // send login packet to server
    Packet packet = new Packet00Login(username, password);
    packet.writeData(client);
    client.getLastPacketKeysSent()[0] = packet.getUuidKey();
  }

  /**
   * This method attempts to create a new account
   * @param username
   * @param email
   * @param password
   * @param password2
   */
  public static void createAccount(String user, String email, char[] pass, char[] pass2) {
    // verify that data given is valid
    if (user == null || user.length() < 1) {
      setErrorCreateAccountScreen("ERROR: PLEASE PROVIDE A USERNAME");
      return;
    } else if (pass == null || pass.length < 1) {
      setErrorCreateAccountScreen("ERROR: PLEASE PROVIDE A PASSWORD");
      return;
    } else if (user.length() > 20) {
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
    for (char c : user.toCharArray())
      if (c == '~' || c == ',' || c == ':') {
        setErrorCreateAccountScreen("ERROR: USERNAMES MAY NOT CONTAIN ,: or ~");
        return;
      }

    // encrypt username and password
    String username = Security.encrypt(user);
    String password = "";
    for (int i = 0; i < pass.length; i++) {
      if (pass[i] != pass2[i]) {
        setErrorCreateAccountScreen("ERROR: PASSWORDS DO NOT MATCH");
        return;
      }
      password += (pass[i] + 15);
    }
    password = Security.encrypt(password);

    // send create account packet to server
    Packet packet = new Packet01CreateAcc(email, username, password);
    packet.writeData(client);
    client.getLastPacketKeysSent()[1] = packet.getUuidKey();
  }

  /**
   * This method attempts to log the user out of the server
   */
  public static void logout() {
    // send logout packet to server
    Packet packet = new Packet03Logout(getPlayer() == null ? null : getPlayer().getUser_key());
    packet.writeData(client);
    client.getLastPacketKeysSent()[3] = packet.getUuidKey();
  }

  /**
   * This method attempts to retrieve friends and other players for CreateGameScreen
   */
  public static void getOtherPlayers() {
    // send get players packet to server
    Packet packet = new Packet04GetPlayers(getPlayer().getUser_key());
    packet.writeData(client);
    client.getLastPacketKeysSent()[4] = packet.getUuidKey();
  }

  /**
   * This method sends a friend request to server
   * @param friend
   */
  public static void addFriend(String friend) {
    // send addFriend packet to server
    Packet packet = new Packet05AddFriend(getPlayer().getUser_key(), friend);
    packet.writeData(client);
    client.getLastPacketKeysSent()[5] = packet.getUuidKey();
  }

  /**
   * This method sends a create game request to server
   * @param otherUser
   * @param creatorGoesFirst
   * @param gameType
   */
  public static void createGame(String otherUser, boolean creatorGoesFirst, int gameType) {
    // send create game packet to server
    Packet packet = new Packet06CreateGame(getPlayer().getUser_key(), getPlayer().getUsername(), otherUser, creatorGoesFirst, gameType);
    packet.writeData(client);
    client.getLastPacketKeysSent()[6] = packet.getUuidKey();
  }

  /**
   * This method attempts to retrieve games for ActiveGamesScreen
   */
  public static void getActiveGames() {
    // sends get games packet to server
    Packet packet = new Packet07GetGames(getPlayer().getUser_key(), getPlayer().getUsername());
    packet.writeData(client);
    client.getLastPacketKeysSent()[7] = packet.getUuidKey();
  }

  /**
   * This method attempts to retrieve a game from the server
   * Ultimately leads to opening GameScreen
   * @param gameKey
   */
  public static void getBoard(String gameKey, int gameType) {
    // sends get board packet to server
    Packet packet = new Packet08GetBoard(getPlayer().getUser_key(), getPlayer().getUsername(), gameKey, gameType);
    packet.writeData(client);
    client.getLastPacketKeysSent()[8] = packet.getUuidKey();
  }

  /**
   * This method attempts to send a move to the server
   * @param movingFrom
   * @param movingTo
   */
  public static void sendMove(int movingFrom, int movingTo) {
    // if the user is online, send move to server, else offline server
    if (isOnline()) {
      // sends send move packet to server
      final Packet packet = new Packet09SendMove(getPlayer().getUser_key(), getBoardGame().getGameKey(),
                movingFrom, movingTo, getPlayer().getUsername(), getBoardGame().getGameType());
      client.getLastPacketKeysSent()[9] = packet.getUuidKey();
      // set last move received false
      client.setLastMoveReceived(false);

      // send move, expect a 09 packet response
      // send up to 5 times, if no response received display disconnect error
      new Thread(new Runnable() {
        public void run() {
          // send packet, wait 500ms before checking for response
          if (sendMove(packet, 500))
            return;
          if (sendMove(packet, 500))
            return;
          if (sendMove(packet, 750))
            return;
          if (sendMove(packet, 1000))
            return;
          if (sendMove(packet, 5000))
            return;
          JOptionPane.showConfirmDialog(null, "Cannot contact the server, the game will quit", "Connection Error", JOptionPane.CANCEL_OPTION);
          System.exit(0);
        }
      }).start();
    } else {
      getBoardGame().makeMoveOffline(movingFrom, movingTo);
      updateGameBoard();
    }
  }

  /**
   * Sends the client's move to server, returns true if the client received a response from server
   * @param packet
   * @param waitTime
   * @return
   */
  private static boolean sendMove(Packet packet, int waitTime) {
    try {
      // send packet and wait for waitTime
      packet.writeData(client);
      Thread.sleep(waitTime);

      // check for receipt, if one, exit and remove packet, if none return false
      if (client.isLastMoveReceived())
        return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * This method attempts to send a line of text to their opponent
   * @param sendToSpectators
   * @param text
   */
  public static void sendChat(boolean sendToSpectators, String text) {
    // sends chat packet to server
    Packet packet = new Packet10SendChat(getPlayer().getUser_key(), getBoardGame().getGameKey(),
        getBoardGame().getPlayer1(), getBoardGame().getPlayer2(), sendToSpectators, getPlayer().getUsername() + ": " + text);
    packet.writeData(client);
    client.getLastPacketKeysSent()[10] = packet.getUuidKey();
  }

  /**
   * This method attempts to update the client's current GameScreen
   */
  public static void sendOnGameUpdate() {
    // sends chat packet to server
    Packet packet = new Packet11OnGame(getPlayer().getUser_key(), getBoardGame().getGameKey());
    packet.writeData(client);
    client.getLastPacketKeysSent()[11] = packet.getUuidKey();
  }

  /**
   * This method attempts to retrieve games for SpectatorGamesScreen
   */
  public static void getSpectatorGames() {
    // sends get games packet to server
    Packet packet = new Packet12GetSpectates(getPlayer().getUser_key());
    packet.writeData(client);
    client.getLastPacketKeysSent()[12] = packet.getUuidKey();
  }

  /**
   * This method closes the screen for the user
   */
  public static void exitCurrentScreen() {
    if (screen != null)
      screen.exit();
  }

  /**
   * This method opens up the login screen for the user
   */
  public static void openLoginScreen() {
    exitCurrentScreen();
    screen = new LoginScreen();
    gameState = LOGIN;
    setOnline(true);
  }

  /**
   * This method opens up the create account screen for the user
   */
  public static void openCreateAccountScreen() {
    exitCurrentScreen();
    screen = new CreateAccountScreen();
    gameState = CREATE_ACCOUNT;
    setOnline(true);
  }

  /**
   * This method opens up the active games screen for the user
   */
  public static void openActiveGamesScreen() {
    exitCurrentScreen();
    screen = new ActiveGamesScreen();
    gameState = ACTIVE_GAMES;
    getActiveGames();
  }

  /**
   * This method opens up the create game screen for the user
   */
  public static void openCreateGameScreen() {
    exitCurrentScreen();
    screen = new CreateGameScreen();
    gameState = CREATE_GAME;
    getOtherPlayers();
  }

  /**
   * This method opens up the game screen for the user
   */
  public static void openGameScreen() {
    exitCurrentScreen();
    screen = new GameScreen();
    gameState = PLAYING_GAME;
  }

  /**
   * This method opens up the spectator games screen for user
   */
  public static void openSpectatorGamesScreen() {
    exitCurrentScreen();
    screen = new SpectatorGamesScreen();
    gameState = SPECTATOR_GAMES;
    getSpectatorGames();
  }

  /**
   * This method opens up the create offline game screen for user
   */
  public static void openCreateOfflineGameScreen() {
    exitCurrentScreen();
    screen = new CreateOfflineGameScreen();
    gameState = CREATE_OFFLINE_GAME;
    setOnline(false);
  }

  /**
   * This method refreshes the current screen, used when screen resized
   */
  public static void refresh() {
    // make sure screen is defined
    if (screen == null)
      return;

    switch (gameState) {
      case LOGIN:          openLoginScreen();
                    break;
      case CREATE_ACCOUNT:    openCreateAccountScreen();
                    break;
      case ACTIVE_GAMES:      openActiveGamesScreen();
                    break;
      case CREATE_GAME:      openCreateGameScreen();
                    break;
      case PLAYING_GAME:      openGameScreen();
                    break;
      case SPECTATOR_GAMES:    openSpectatorGamesScreen();
                    break;
      case CREATE_OFFLINE_GAME:  openCreateOfflineGameScreen();
    }
  }

  /**
   * This method updates the active games list on the ActiveGamesScreen
   */
  public static void updateActiveGamesList() {
    if (!(screen instanceof ActiveGamesScreen)) {
      System.out.println("GAMESTATE ERROR update active games list called on wrong screen");
      return;
    }
    ((ActiveGamesScreen) screen).loadActiveGames();
  }

  /**
   * This method updates the players list on the CreateGameScreen
   */
  public static void updatePlayersList() {
    if (!(screen instanceof CreateGameScreen)) {
      System.out.println("GAMESTATE ERROR update players list called on wrong screen");
      return;
    }
    ((CreateGameScreen) screen).loadPlayers();
  }

  /**
   * This method updates the game board on the GameScreen
   */
  public static void updateGameBoard() {
    if (!(screen instanceof GameScreen)) {
      System.out.println("GAMESTATE ERROR update game board called on wrong screen");
      return;
    }
    ((GameScreen) screen).updateBoard();
  }

  /**
   * This method updates the game chat on the GameScreen
   * @param text
   * @param senderKey
   */
  public static void updateGameChat(String text, String senderKey) {
    if (!(screen instanceof GameScreen)) {
      System.out.println("GAMESTATE ERROR update game chat called on wrong screen");
      return;
    }
    ((GameScreen) screen).updateChat(text, senderKey);
  }

  /**
   * This method updates the color of the opponent on the GameScreen by whether they are on the game or not
   */
  public static void updatePlayersOnGame() {
    if (!(screen instanceof GameScreen)) {
      System.out.println("GAMESTATE ERROR update opponent on game called on wrong screen");
      return;
    }
    ((GameScreen) screen).updatePlayersOnGame();
  }

  /**
   * This method updates the spectator games list on the SpectatorGamesScreen
   */
  public static void updateSpectatorGamesList() {
    if (!(screen instanceof SpectatorGamesScreen)) {
      System.out.println("GAMESTATE ERROR update spectator games list called on wrong screen");
      return;
    }
    ((SpectatorGamesScreen) screen).loadSpectatorGames();
  }

  /**
   * This method notifies the user if a change was made to one of their games
   * @param boardGame
   */
  public static void notifyUser(BoardGame boardGame) {
    screen.notifyUser(boardGame);
  }

  /**
   * Verifies that the client is on LoginScreen and then sets the error to errorMessage
   * Called by Game and GameClient classes
   * @param errorMessage
   */
  public static void setErrorLoginScreen(String errorMessage) {
    if (!(screen instanceof LoginScreen))
      return;
    ((LoginScreen) screen).setError(errorMessage);
  }

  /**
   * Verifies that the client is on CreateAccountScreen and then sets the error to errorMessage
   * Called by Game and GameClient classes
   * @param errorMessage
   */
  public static void setErrorCreateAccountScreen(String errorMessage) {
    if (!(screen instanceof CreateAccountScreen))
      return;
    ((CreateAccountScreen) screen).setError(errorMessage);
  }

  /**
   * Verifies that the client is on CreateGameScreen and then sets the error to errorMessage
   * Called by Game and GameClient classes
   * @param errorMessage
   */
  public static void setErrorCreateGameScreen(String errorMessage) {
    if (!(screen instanceof CreateGameScreen))
      return;
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

  private static byte[] getServerAddress() throws UnknownHostException {
    String serverAddress = System.getenv("LIBRARY_GAMES_SERVER_ADDRESS");
    if (serverAddress == null) {
      return DEFAULT_SERVER_IP_ADDRESS;
    }
    return InetAddress.getByName(serverAddress).getAddress();
  }

  private static int getServerPort() {
    String serverPort = System.getenv("LIBRARY_GAMES_SERVER_PORT");
    if (serverPort == null) {
      return DEFAULT_SERVER_PORT;
    }
    return Integer.parseInt(serverPort);
  }
}
