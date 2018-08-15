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
	 * Handles a successful create game and opens the new game
	 * @param data
	 * TODO decide whether or not to keep
	private void getNewGame(byte[] data) {
		// verify that user is still on the create game screen, if not exit
		if (Game.gameState != Game.CREATE_GAME)
			return;
		
		Packet06CreateGame packet = new Packet06CreateGame(data, true);
		
		// open game screen if the user created the game
		if (packet.getUserKey().equals(Game.getPlayer().getUser_key()))
			Game.openGameScreen(packet.getGameKey());
	}*/

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
			
			/*
			 *  determine type of GameBoard to make
			 */
			
			// if Go create Go board
			if (packet.getGameType() < 3 && packet.getGameType() > -1) {
				// TODO create game board
			}
			
			// open GameScreen and exit
			Game.openGameScreen();
			return;
		}
	}
}
