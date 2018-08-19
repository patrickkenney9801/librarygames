package com.nfehs.librarygames;

import java.awt.Container;
import java.awt.Dimension;
import java.net.UnknownHostException;

import javax.swing.JFrame;

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
	
	public static final byte[] SERVER_IP_ADDRESS = {108, (byte) 205, (byte) 143, 97};
	
	public static final int LOGIN = 0;
	public static final int CREATE_ACCOUNT = 1;
	public static final int ACTIVE_GAMES = 2;
	public static final int CREATE_GAME = 3;
	public static final int PLAYING_GAME = 4;
	public static final int OVER = 10;
	
	public static int gameState = Game.LOGIN;
	public static boolean gamePlaying = true;

	public Game() throws UnknownHostException {
		client = new GameClient(SERVER_IP_ADDRESS);
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
			// TODO error user empty
			return;
		} else if (pass == null || pass.length < 1) {
			// TODO error pass empty
			return;
		} else if (user.length() > 20) {
			// TODO error user too long
			return;
		} else if (pass.length > 20) {
			// TODO error pass too long
			return;
		}
		
		// encrypt username and password
		String username = Security.encrypt(user);
		String password = "";
		for (char c : pass)
			password += (c + 15);
		password = Security.encrypt(password);
		
		// send login packet to server
		new Packet00Login(username, password).writeData(client);
	}

	/**
	 * This method attempts to create a new account
	 * @param username
	 * @param email
	 * @param password
	 * @param password2
	 * TODO ban special characters like :,~
	 */
	public static void createAccount(String user, String email, char[] pass, char[] pass2) {
		// verify that data given is valid
		if (user == null || user.length() < 1) {
			// TODO error user empty
			return;
		} else if (pass == null || pass.length < 1) {
			// TODO error pass empty
			return;
		} else if (user.length() > 20) {
			// TODO error user too long
			return;
		} else if (pass.length > 20) {
			// TODO error pass too long
			return;
		} else if (pass.length != pass2.length) {
			// TODO error passwords do not match
			return;
		} else if (email.contains(":")) {
			// TODO error invalid email
			return;
		}
		
		// encrypt username and password
		String username = Security.encrypt(user);
		String password = "";
		for (int i = 0; i < pass.length; i++) {
			if (pass[i] != pass2[i]) {
				// TODO error passwords do not match
				return;
			}
			password += (pass[i] + 15);
		}
		password = Security.encrypt(password);
		
		// send create account packet to server
		new Packet01CreateAcc(email, username, password).writeData(client);
	}
	
	/**
	 * This method attempts to log the user out of the server
	 */
	public static void logout() {
		// send logout packet to server
		new Packet03Logout().writeData(client);
	}
	
	/**
	 * This method attempts to retrieve friends and other players for CreateGameScreen
	 */
	public static void getOtherPlayers() {
		// send get players packet to server
		new Packet04GetPlayers(getPlayer().getUser_key()).writeData(client);
	}

	/**
	 * This method sends a friend request to server
	 * @param friend
	 */
	public static void addFriend(String friend) {
		// send addFriend packet to server
		new Packet05AddFriend(getPlayer().getUser_key(), friend).writeData(client);
	}
	
	/**
	 * This method sends a create game request to server
	 * @param otherUser
	 * @param creatorGoesFirst
	 * @param gameType
	 */
	public static void createGame(String otherUser, boolean creatorGoesFirst, int gameType) {
		// send create game packet to server
		new Packet06CreateGame(getPlayer().getUser_key(), getPlayer().getUsername(), otherUser, creatorGoesFirst, gameType).writeData(client);
	}
	
	/**
	 * This method attempts to retrieve games for ActiveGamesScreen
	 */
	public static void getActiveGames() {
		// sends get games packet to server
		new Packet07GetGames(getPlayer().getUser_key(), getPlayer().getUsername()).writeData(client);
	}
	
	/**
	 * This method attempts to retrieve a game from the server
	 * Ultimately leads to opening GameScreen
	 * @param gameKey
	 */
	public static void getBoard(String gameKey, int gameType) {
		// sends get board packet to server
		new Packet08GetBoard(getPlayer().getUser_key(), getPlayer().getUsername(), gameKey, gameType).writeData(client);
	}
	
	/**
	 * This method attempts to send a move to the server
	 * @param movingFrom
	 * @param movingTo
	 */
	public static void sendMove(int movingFrom, int movingTo) {
		// sends send move packet to server
		new Packet09SendMove(getPlayer().getUser_key(), getBoardGame().getGameKey(), movingFrom, movingTo,
							getPlayer().getUsername(), getBoardGame().getGameType()).writeData(client);
	}
	
	/**
	 * This method attempts to send a line of text to their opponent
	 * @param text
	 */
	public static void sendChat(String text) {
		// sends chat packet to server
		new Packet10SendChat(getPlayer().getUser_key(),
				getBoardGame().getGameKey(),
				getBoardGame().getPlayer1().equals(getPlayer().getUsername()) ? getBoardGame().getPlayer2() : getBoardGame().getPlayer1(),
				getPlayer().getUsername() + ": " + text).writeData(client);
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
	}

	/**
	 * This method opens up the create account screen for the user
	 */
	public static void openCreateAccountScreen() {
		exitCurrentScreen();
		screen = new CreateAccountScreen();
		gameState = CREATE_ACCOUNT;
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
	 * This method refreshes the current screen, used when screen resized
	 */
	public static void refresh() {
		// make sure screen is defined
		if (screen == null)
			return;
		
		switch (gameState) {
			case LOGIN:					openLoginScreen();
										break;
			case CREATE_ACCOUNT:		openCreateAccountScreen();
										break;
			case ACTIVE_GAMES:			openActiveGamesScreen();
										break;
			case CREATE_GAME:			openCreateGameScreen();
										break;
			case PLAYING_GAME:			openGameScreen();
										break;
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
	 * This method notifies the user if a change was made to one of their games
	 * @param boardGame
	 */
	public static void notifyUser(BoardGame boardGame) {
		screen.notifyUser(boardGame);
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
}
