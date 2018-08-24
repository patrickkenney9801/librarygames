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
	public static final int PORT = 19602;
	
	private String[] lastPacketKeysSent;
	
	private int packetsToReceiveGetPlayers;
	private String[] friends;
	private String[] others;
	
	private int packetsToReceiveGetGames;
	private String[][] games;
	
	private int packetsToReceiveGetSpectates;
	private String[][] spectates;
	
	private boolean lastMoveReceived;
	
	public GameClient(byte[] ipAddress) {
		setLastPacketKeysSent(new String[13]);
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
	private void handle(final DatagramPacket packet) {
		// create new thread to handle packet
		new Thread (new Runnable () {
			public void run() {
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
					case CREATEGAME:			break;
					case GETGAMES:				getGames(packet.getData());
												break;
					case GETBOARD:				getBoard(packet.getData());
												break;
					case SENDMOVE:				updateBoard(packet.getData());
												break;
					case SENDCHAT:				updateChat(packet.getData());
												break;
					case ONGAME:				updateOnGame(packet.getData());
												break;
					case GETSPECTATES:			getSpectates(packet.getData());
					default:					break;
				}
			}
		}).run();
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
	
	/**
	 * Handles a user successfully logging to server
	 * @param data
	 */
	private void loginUser(byte[] data) {
		// verify that user is still on the login screen, if not exit
		if (Game.gameState != Game.LOGIN)
			return;
		Packet00Login packet = new Packet00Login(data, true);
		if (!packet.isValid())
			return;
		// verify that this packet is responding to the last one sent
		if (!packet.getUuidKey().equals(getLastPacketKeysSent()[0]))
			return;
		
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
		if (!packet.isValid())
			return;
		// verify that this packet is responding to the last one sent
		if (!packet.getUuidKey().equals(getLastPacketKeysSent()[1]))
			return;
		
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
		if (!packet.isValid())
			return;
		// verify that this packet is responding to the last one sent
		if (!packet.getUuidKey().equals(getLastPacketKeysSent()[4]))
			return;
		
		if (getFriends() == null || getFriends().length != packet.getPacketsSent()) {
			setPacketsToReceiveGetPlayers(packet.getPacketsSent());
			setFriends(new String[packet.getPacketsSent()]);
			setOthers(new String[packet.getPacketsSent()]);
		}
		
		// set friends and other players lists in class in right row
		getFriends()[packet.getPacketNumber()-1] = packet.getFriends();
		getOthers()[packet.getPacketNumber()-1] = packet.getOtherPlayers();
		
		// when all packets have been received, 
		if (listIsFull(getFriends())) {
			// build strings for friends and others
			String friends = "";
			String others = "";
			
			for (int i = 0; i < getPacketsToReceiveGetPlayers(); i++) {
				friends += getFriends()[i];
				others += getOthers()[i];
			}
			
			if (friends.length() < 1)
				friends = null;
			if (others.length() < 1)
				others = null;
			Game.getPlayer().setFriends(friends);
			Game.getPlayer().setOtherPlayers(others);
			
			// refresh friends and players on create game screen
			Game.updatePlayersList();
			
			setFriends(null);
		}
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
		if (!packet.isValid())
			return;
		// verify that this packet is responding to the last one sent
		if (!packet.getUuidKey().equals(getLastPacketKeysSent()[7]))
			return;
		
		if (getGames() == null || getGames().length != packet.getPacketsSent()) {
			setPacketsToReceiveGetGames(packet.getPacketsSent());
			setGames(new String[packet.getPacketsSent()][]);
		}
		
		// set games lists in class in right row
		getGames()[packet.getPacketNumber()-1] = packet.getGameInfo();
		
		// when all packets have been received, 
		if (listIsFull(getGames())) {
			ArrayList<String> finished = new ArrayList<String>();
			ArrayList<String> userTurn = new ArrayList<String>();
			ArrayList<String> opponentTurn = new ArrayList<String>();
			
			for (String[] packetData : getGames())
				for (String info : packetData) {
					if (info != null) {
						String gameData = BoardGame.getGameInfo(info.split(","));
						
						if (info.split(",").length == 6)
							finished.add(gameData);
						else if (Boolean.parseBoolean(gameData.split("~")[2]))
							userTurn.add(gameData);
						else
							opponentTurn.add(gameData);
					}
				}
			
			// set board game Strings in Player
			Game.getPlayer().setFinishedBoardGames(finished);
			Game.getPlayer().setYourTurnBoardGames(userTurn);
			Game.getPlayer().setOpponentTurnBoardGames(opponentTurn);
			
			// refresh games list on ActiveGamesScreen
			Game.updateActiveGamesList();
			
			setGames(null);
		}
	}

	/**
	 * Handles a successful request for a game
	 * Also handles server sent games when an action is made
	 * Also handles server sent new games
	 * @param data
	 */
	private void getBoard(byte[] data) {
		Packet08GetBoard packet = new Packet08GetBoard(data, true);
		if (!packet.isValid())
			return;
		// send receipt
		Packet13Receipt receipt = new Packet13Receipt(packet.getUuidKey());
		receipt.writeData(this);
		
		// check to see if user is trying to access game from ActiveGamesScreen or CreateGameScreen or SpectatorGamesScreen
		if ((Game.gameState == Game.ACTIVE_GAMES || Game.gameState == Game.CREATE_GAME || Game.gameState == Game.SPECTATOR_GAMES) 
					&& (packet.getUuidKey().equals(getLastPacketKeysSent()[8]) || packet.getUuidKey().equals(getLastPacketKeysSent()[6]))
						|| packet.getUuidKey().equals(getLastPacketKeysSent()[12])) {
			
			// if so, set GameBoard and open GameScreen
			Game.setBoardGame(BoardGame.createGame(packet.getGameKey(), packet.getGameType(), packet.getPlayer1(), packet.getPlayer2(), packet.getMoves(),
					packet.getPenultMove(), packet.getLastMove(), packet.getWinner(), packet.isPlayer1OnGame(), packet.isPlayer2OnGame(), packet.getBoard(), packet.getExtraData()));
			
			// open GameScreen
			Game.openGameScreen();
		}
		// check to see if user is receiving packet while on GameScreen, if it is the same game, update screen
		else if (Game.gameState == Game.PLAYING_GAME && Game.getBoardGame().update(packet.getGameKey(), packet.getBoard(),
				packet.getPenultMove(), packet.getLastMove(), packet.getWinner(), packet.isPlayer1OnGame(), packet.isPlayer2OnGame(), packet.getExtraData()))
			Game.updateGameBoard();
		// if none of the above, notify the client
		else {
			// if the user is on the active games screen, update it
			if (Game.gameState == Game.ACTIVE_GAMES)
				Game.getActiveGames();
			// notify the user
			Game.notifyUser(BoardGame.createGame(packet.getGameKey(), packet.getGameType(), packet.getPlayer1(), packet.getPlayer2(), packet.getMoves(),
					packet.getPenultMove(), packet.getLastMove(), packet.getWinner(), true, true, packet.getBoard(), packet.getExtraData()));
		}
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
		if (!packet.isValid())
			return;
		// send receipt
		Packet13Receipt receipt = new Packet13Receipt(packet.getUuidKey());
		receipt.writeData(this);
		// verify that this packet is responding to the last one sent
		if (!packet.getUuidKey().equals(getLastPacketKeysSent()[9]))
			return;
		
		// set last move receipt true
		setLastMoveReceived(true);
		
		// update current board game, returns false if wrong game
		// if successful update, update the game board
		if (Game.getBoardGame().update(	packet.getGameKey(), packet.getBoard(), packet.getPenultMove(), packet.getLastMove(),
										packet.getWinner(), packet.isPlayer1OnGame(), packet.isPlayer2OnGame(), packet.getExtraData()))
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
		if (!packet.isValid())
			return;
		// send receipt
		Packet13Receipt receipt = new Packet13Receipt(packet.getUuidKey());
		receipt.writeData(this);
		
		// check that client is on correct game, if so update the chat
		if (packet.getGameKey().equals(Game.getBoardGame().getGameKey())) {
			Game.getBoardGame().setPlayer1OnGame(packet.isPlayer1OnGame());
			Game.getBoardGame().setPlayer2OnGame(packet.isPlayer2OnGame());
			Game.updateGameChat(packet.getText(), packet.getSenderKey());
		}
	}

	/**
	 * Handles a server onGame packet, updates opponent is on same game
	 * @param data
	 */
	private void updateOnGame(byte[] data) {
		// verify that user is on game screen, if not exit
		if (Game.gameState != Game.PLAYING_GAME)
			return;
		
		Packet11OnGame packet = new Packet11OnGame(data, true);
		if (!packet.isValid())
			return;
		
		// check that client is on correct game, if so update the opponent is on game
		if (packet.getGameKey().equals(Game.getBoardGame().getGameKey())) {
			if (Game.getBoardGame().getPlayer1().equals(Security.decrypt(packet.getPlayer())))
				Game.getBoardGame().setPlayer1OnGame(packet.isOnGame());
			else if (Game.getBoardGame().getPlayer2().equals(Security.decrypt(packet.getPlayer())))
				Game.getBoardGame().setPlayer2OnGame(packet.isOnGame());
		}
		Game.updatePlayersOnGame();
	}

	/**
	 * Handles a successful request for spectator games
	 * @param data
	 */
	private void getSpectates(byte[] data) {
		// verify that user is still on the spectator games screen, if not exit
		if (Game.gameState != Game.SPECTATOR_GAMES)
			return;
		
		Packet12GetSpectates packet = new Packet12GetSpectates(data, true);
		if (!packet.isValid())
			return;
		// verify that this packet is responding to the last one sent
		if (!packet.getUuidKey().equals(getLastPacketKeysSent()[12]))
			return;
		
		if (getSpectates() == null || getSpectates().length != packet.getPacketsSent()) {
			setPacketsToReceiveGetSpectates(packet.getPacketsSent());
			setSpectates(new String[packet.getPacketsSent()][]);
		}
		
		// set spectates lists in class in right row
		getSpectates()[packet.getPacketNumber()-1] = packet.getGameInfo();
		
		// when all packets have been received, 
		if (listIsFull(getSpectates())) {
			ArrayList<String> spectates = new ArrayList<String>();
			
			for (String[] packetData : getSpectates())
				for (String info : packetData) {
					if (info != null) {
						String gameData = BoardGame.getGameInfo(info.split(","));
						spectates.add(gameData);
					}
				}
			
			// set board game Strings in Player
			Game.getPlayer().setSpectatorBoardGames(spectates);
			
			// refresh games list on SpectatorGamesScreen
			Game.updateSpectatorGamesList();
			
			setSpectates(null);
		}
	}

	/**
	 * Returns true if a given string array has no null values
	 * @param array
	 * @return
	 */
	private boolean listIsFull(String[] array) {
		for (String s : array)
			if (s == null)
				return false;
		return true;
	}

	/**
	 * Returns true if a given 2d string array has no null values
	 * @param array
	 * @return
	 */
	private boolean listIsFull(String[][] array) {
		for (String[] s : array)
			if (s == null)
				return false;
		return true;
	}

	/**
	 * @return the lastPacketKeysSent
	 */
	public String[] getLastPacketKeysSent() {
		return lastPacketKeysSent;
	}

	/**
	 * @param lastPacketKeysSent the lastPacketKeysSent to set
	 */
	public void setLastPacketKeysSent(String[] lastPacketKeysSent) {
		this.lastPacketKeysSent = lastPacketKeysSent;
	}

	public int getPacketsToReceiveGetPlayers() {
		return packetsToReceiveGetPlayers;
	}

	public void setPacketsToReceiveGetPlayers(int packetsToReceiveGetPlayers) {
		this.packetsToReceiveGetPlayers = packetsToReceiveGetPlayers;
	}

	public String[] getFriends() {
		return friends;
	}

	public void setFriends(String[] friends) {
		this.friends = friends;
	}

	public String[] getOthers() {
		return others;
	}

	public void setOthers(String[] others) {
		this.others = others;
	}

	public int getPacketsToReceiveGetGames() {
		return packetsToReceiveGetGames;
	}

	public void setPacketsToReceiveGetGames(int packetsToReceiveGetGames) {
		this.packetsToReceiveGetGames = packetsToReceiveGetGames;
	}

	public String[][] getGames() {
		return games;
	}

	public void setGames(String[][] games) {
		this.games = games;
	}

	public int getPacketsToReceiveGetSpectates() {
		return packetsToReceiveGetSpectates;
	}

	public void setPacketsToReceiveGetSpectates(int packetsToReceiveGetSpectates) {
		this.packetsToReceiveGetSpectates = packetsToReceiveGetSpectates;
	}

	public String[][] getSpectates() {
		return spectates;
	}

	public void setSpectates(String[][] spectates) {
		this.spectates = spectates;
	}

	public boolean isLastMoveReceived() {
		return lastMoveReceived;
	}

	public void setLastMoveReceived(boolean lastMoveReceived) {
		this.lastMoveReceived = lastMoveReceived;
	}
}
