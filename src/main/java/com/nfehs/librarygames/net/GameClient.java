package com.nfehs.librarygames.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.nfehs.librarygames.Game;
import com.nfehs.librarygames.Player;
import com.nfehs.librarygames.games.BoardGame;
import com.nfehs.librarygames.games.go.Go;
import com.nfehs.librarygames.net.packets.*;

/**
 * This class handles receiving packets from and sending to the server
 * @author Patrick Kenney, Syed Quadri
 * @date 6/13/2018
 */

public class GameClient extends Thread {

	private InetAddress ipAddress;
	private DatagramSocket socket;
	private Game game;
	public static final int PORT = 19602;
	
	public GameClient(Game game, byte[] ipAddress) {
		this.game = game;
		try {
			this.socket = new DatagramSocket();
			this.ipAddress = InetAddress.getByAddress(ipAddress);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Receives incoming packets from server
	 */
	public void run() {
		while (true) {
			byte[] data = new byte[1024];
			DatagramPacket packet = new DatagramPacket(data, data.length);
			
			try {
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("SERVER > " + new String(packet.getData()));
			handle(packet);
		}
	}
	
	/**
	 * Determines what to do with incoming packets
	 * @param packet
	 */
	private void handle(DatagramPacket packet) {
		switch (Packet.lookupPacket(new String(packet.getData()).trim().substring(0, 2))) {
			case INVALID:				break;
			case LOGIN:					loginUser(packet.getData());
										break;
			case CREATEACCOUNT:			createAccountLogin(packet.getData());
										break;
			case ERROR:					// TODO will handle all errors
										break;
			case LOGOUT:				// TODO probably will not be used
										break;
			case GETPLAYERS:			getPlayers(packet.getData());
										break;
			case CREATEGAME:			//getNewGame(packet.getData());
										break;
			case GETGAMES:				getGames(packet.getData());
										break;
			case GETBOARD:				getBoard(packet.getData());
										break;
			case SENDMOVE:				updateBoard(packet.getData());
										break;
			case SENDCHAT:				updateChat(packet.getData());
			default:					break;
		}
	}

	/**
	 * Sends data to server
	 * @param data
	 */
	public void sendData(byte[] data) {
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, PORT);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// TODO use uuid keys for packages to keep things organized
	
	/**
	 * Handles a user successfully logging to server
	 * @param data
	 */
	private void loginUser(byte[] data) {
		// verify that user is still on the login screen, if not exit
		if (Game.gameState != Game.LOGIN)
			return;
		Packet00Login packet = new Packet00Login(data, true);
		
		// create player from packet
		Game.setPlayer(new Player(Security.decrypt(packet.getUsername()), packet.getUserKey()));
		
		// open active games screen
		Game.openActiveGamesScreen();
	}

	/**
	 * Handles a user successfully creating an account and then logging into server
	 * @param data
	 */
	private void createAccountLogin(byte[] data) {
		// verify that user is still on the create account screen, if not exit
		if (Game.gameState != Game.CREATE_ACCOUNT)
			return;
		
		Packet01CreateAcc packet = new Packet01CreateAcc(data, true);
		
		// create player from packet
		Game.setPlayer(new Player(Security.decrypt(packet.getUsername()), packet.getUserKey()));
		
		// open active games screen
		Game.openActiveGamesScreen();
	}

	/**
	 * Handles a successful request for players
	 * @param data
	 */
	private void getPlayers(byte[] data) {
		// verify that user is still on the create game screen, if not exit
		if (Game.gameState != Game.CREATE_GAME)
			return;
		
		Packet04GetPlayers packet = new Packet04GetPlayers(data, true);
		
		// set friends and other players lists in Player
		Game.getPlayer().setFriends(packet.getFriends());
		Game.getPlayer().setOtherPlayers(packet.getOtherPlayers());
		
		// refresh friends and players on create game screen
		Game.updatePlayersList();
	}

	/**
	 * Handles a successful request for games
	 * @param data
	 */
	private void getGames(byte[] data) {
		// verify that user is still on the active games screen, if not exit
		if (Game.gameState != Game.ACTIVE_GAMES)
			return;
		
		Packet07GetGames packet = new Packet07GetGames(data, true);
		
		// set your and opponent turn board games
		String[] gameInfo = packet.getGameInfo();
		ArrayList<String> userTurn = new ArrayList<String>();
		ArrayList<String> opponentTurn = new ArrayList<String>();
		
		for (String info : gameInfo) {
			String gameData = BoardGame.getGameInfo(info.split(","));
			
			if (Boolean.parseBoolean(gameData.split("~")[2]))
				userTurn.add(gameData);
			else
				opponentTurn.add(gameData);
		}
		
		// set board game Strings in Player
		Game.getPlayer().setYourTurnBoardGames(userTurn);
		Game.getPlayer().setOpponentTurnBoardGames(opponentTurn);
		
		// refresh games list on ActiveGamesScreen
		Game.updateActiveGamesList();
	}

	/**
	 * Handles a successful request for a game
	 * Also handles server sent games when an action is made
	 * Also handles server sent new games
	 * @param data
	 */
	private void getBoard(byte[] data) {
		Packet08GetBoard packet = new Packet08GetBoard(data, true);
		
		// check to see if user is trying to access game from ActiveGamesScreen or CreateGameScreen
		if ((Game.gameState == Game.ACTIVE_GAMES || Game.gameState == Game.CREATE_GAME) 
					&& packet.getUserKey().equals(Game.getPlayer().getUser_key())) {
			
			// if so, set GameBoard and open GameScreen
			Game.setBoardGame(BoardGame.createGame(packet.getGameKey(), packet.getGameType(), packet.getPlayer1(), packet.getPlayer2(), packet.getMoves(),
					packet.getPenultMove(), packet.getLastMove(), packet.getWinner(), packet.getBoard(), packet.getExtraData()));
			
			// open GameScreen and exit
			Game.openGameScreen();
			return;
		}
		// check to see if user is receiving packet while on GameScreen, if it is the same game, update screen
		if (Game.gameState == Game.PLAYING_GAME && Game.getBoardGame().update(packet.getGameKey(), packet.getBoard(),
				packet.getPenultMove(), packet.getLastMove(), packet.getWinner(), packet.getExtraData()))
			Game.updateGameBoard();
	}

	/**
	 * Handles a successful move, updates the game board
	 * @param data
	 */
	private void updateBoard(byte[] data) {
		// verify that user is still on game screen, if not exit
		if (Game.gameState != Game.PLAYING_GAME)
			return;
		
		Packet09SendMove packet = new Packet09SendMove(data, true);
		
		// update current board game, returns false if wrong game
		// if successful update, update the game board
		if (Game.getBoardGame().update(	packet.getGameKey(), packet.getBoard(), packet.getPenultMove(), packet.getLastMove(),
										packet.getWinner(), packet.getExtraData()))
			Game.updateGameBoard();
	}

	/**
	 * Handles a server sent chat, updates the game chat
	 * @param data
	 */
	private void updateChat(byte[] data) {
		// verify that user is on game screen, if not exit
		if (Game.gameState != Game.PLAYING_GAME)
			return;
		
		Packet10SendChat packet = new Packet10SendChat(data, true);
		
		// check that client is on correct game, if so update the chat
		if (packet.getGameKey().equals(Game.getBoardGame().getGameKey()))
			Game.updateGameChat(packet.getText(), packet.getUserKey());
	}
}
