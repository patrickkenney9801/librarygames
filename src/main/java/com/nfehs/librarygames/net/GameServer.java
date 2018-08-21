package com.nfehs.librarygames.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

import com.nfehs.librarygames.Player;
import com.nfehs.librarygames.games.BoardGame;
import com.nfehs.librarygames.games.go.Go;
import com.nfehs.librarygames.net.packets.*;
import com.nfehs.librarygames.net.packets.Packet.PacketTypes;

/**
 * This class handles receiving packets from and sending to the client
 * @author Patrick Kenney, Syed Quadri
 * @date 6/13/2018
 */

public class GameServer extends Thread {

	private DatagramSocket socket;
	private Connection database;
	private static final int PORT = 19602;
	private final String DATABASE_USER = "root";
	private final String DATABASE_PASS = "98011089";
	
	private ArrayList<Player> onlinePlayers;
	
	public GameServer() {
		try {
			this.database = getConnection();
			System.out.println("Connected to database");
			this.socket = new DatagramSocket(PORT);
			System.out.println("Connected to UDP");
			this.onlinePlayers = new ArrayList<Player>();
		} catch (SocketException e) {
			e.printStackTrace();
	} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns a database connection to library games server
	 * @return
	 * @throws SQLException
	 */
	private Connection getConnection() throws SQLException {
		try {
			String driver = "com.mysql.cj.jdbc.Driver";
			Class.forName(driver);
			
			return DriverManager.getConnection("jdbc:mysql://localhost:3306/library_games?autoReconnect=true", DATABASE_USER, DATABASE_PASS);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Receives incoming packets from clients
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
			System.out.println();
			System.out.println("CLIENT > " + new String(packet.getData()));
			
			parsePacket(packet.getData(), packet.getAddress(), packet.getPort());
		}
	}
	
	/**
	 * Sends data to server
	 * @param data
	 */
	public void sendData(byte[] data, InetAddress ipAddress, int port) {
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Handles packets sent to the server
	 * @param data
	 * @param address
	 * @param port
	 */
	private void parsePacket(final byte[] data, final InetAddress address, final int port) {
		// start new thread to handle packet
		new Thread (new Runnable () {
			public void run() {
				String message = new String(data).trim();
				PacketTypes type = Packet.lookupPacket(message.substring(0, 2));
				
				switch (type) {
					case INVALID:				break;
					case LOGIN:					loginUser(data, address, port);
												break;
					case CREATEACCOUNT:			createAccount(data, address, port);
												break;
					case ERROR:					// TODO, probably will not be used by server
												break;
					case LOGOUT:				logout(data, address, port);
												break;
					case GETPLAYERS:			getPlayers(data, address, port);
												break;
					case ADDFRIEND:				addFriend(data, address, port);
												break;
					case CREATEGAME:			createGame(data, address, port);
												break;
					case GETGAMES:				getGames(data, address, port);
												break;
					case GETBOARD:				getBoard(data, address, port);
												break;
					case SENDMOVE:				sendMove(data, address, port);
												break;
					case SENDCHAT:				sendChat(data, address, port);
												break;
					case ONGAME:				onGame(data, address, port);
					default:					break;
				}
			}
		}).run();
	}

	/**
	 * Logs the user into the database and returns the user's UUID or error message
	 * @param data
	 * @param address
	 * @param port
	 */
	private void loginUser(byte[] data, InetAddress address, int port) {
		try {
			Packet00Login packet = new Packet00Login(data);
			if (!packet.isValid())
				return;
			
			// search for account with the given username and password
			PreparedStatement statement = database.prepareStatement("SELECT * FROM users WHERE username = '"
																	+ packet.getUsername() + "' AND password = '"
																	+ packet.getPassword() + "';");
			ResultSet result = statement.executeQuery();
			
			// if there is not a result, send error incorrect credentials
			if (!result.next()) {
				// TODO error wrong account credentials
				System.out.println("Incorrect credentials with username: " + Security.decrypt(packet.getUsername()));
				return;
			}

			// add player to online players list
			onlinePlayers.add(new Player(packet.getUsername(), result.getString("user_key"), address, port));
			
			// send login packet back to client (include username and key)
			Packet00Login returnPacket = new Packet00Login(	packet.getUuidKey(), packet.getUsername(), 
																	result.getString("user_key"), true);
			returnPacket.writeData(this, address, port);
			System.out.println("RETURN LOGIN PACKET SENT");
			
			updateSender(result.getString("user_key"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a new account for the user on the database and returns UUID or error message
	 * @param data
	 * @param address
	 * @param port
	 */
	private void createAccount(byte[] data, InetAddress address, int port) {
		try {
			Packet01CreateAcc packet = new Packet01CreateAcc(data);
			if (!packet.isValid())
				return;
			
			// verify that the username is not already in use
			PreparedStatement statement = database.prepareStatement("SELECT * FROM users WHERE username = '" + packet.getUsername() + "';");
			ResultSet result = statement.executeQuery();
			
			// if the username is already in use, do not create new account and send back error package
			if (result.next()) {
				// TODO error username already in use
				System.out.println("user: " + result.getString("username") + " already in use");
				return;
			}
			
			// if it is not, create the account
			String userKey = "" + UUID.randomUUID();
			PreparedStatement createUser = database.prepareStatement("INSERT INTO users VALUES ("
										+ "'" + packet.getUsername() + "', " + "'" +  packet.getPassword() 	+ "', " 
										+ "'" + packet.getEmail()	 + "', " + "'" +  userKey			 	+ "', NOW());");
			createUser.executeUpdate();
			
			System.out.println("ACCOUNT CREATED");
			
			// send login packet back to client (include username and key)
			Packet01CreateAcc returnPacket = new Packet01CreateAcc(packet.getUuidKey(), packet.getUsername(), userKey, true);
			returnPacket.writeData(this, address, port);
			System.out.println("RETURN CREATE ACCOUNT PACKET SENT");
			
			// send 04 getPlayers packets to all logged users in case they are on CreateGameScreen
			Packet04GetPlayers temp;
			for (Player p : onlinePlayers) {
				temp = new Packet04GetPlayers(p.getUser_key());
				temp.setUuidKey(packet.getUuidKey());
				getPlayers(temp.getData(), p.getIpAddress(), p.getPort());
			}
			
			// add player to online players list
			onlinePlayers.add(new Player(packet.getUsername(), userKey, address, port));
			
			updateSender(userKey);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Logs the user out of the server
	 * @param data
	 * @param address
	 * @param port
	 */
	private void logout(byte[] data, InetAddress address, int port) {
		Packet03Logout packet = new Packet03Logout(data);
		if (!packet.isValid())
			return;
		
		for (int i = 0; i < onlinePlayers.size(); i++)
			if (onlinePlayers.get(i).getIpAddress().equals(address)
					&& onlinePlayers.get(i).getUser_key().equals(packet.getSenderKey())) {
				sendOnGame(onlinePlayers.get(i).getGame_key(), onlinePlayers.get(i).getUsername(), false);
				onlinePlayers.remove(i);
			}

	}

	/**
	 * Retrieves list of friends and other players
	 * @param data
	 * @param address
	 * @param port
	 */
	private void getPlayers(byte[] data, InetAddress address, int port) {
		try {
			Packet04GetPlayers packet = new Packet04GetPlayers(data);
			if (!packet.isValid())
				return;
			
			// find friends list
			PreparedStatement statement = database.prepareStatement("SELECT * FROM users RIGHT JOIN friends ON users.user_key = friends.userkey2"
																+ " WHERE userkey1 = '" + packet.getSenderKey() + "' ORDER BY last_action_date DESC;");
			ResultSet result = statement.executeQuery();
			
			// get friends usernames and put into array
			ArrayList<String> friendsList = new ArrayList<String>();
			while (result.next())
				friendsList.add(Security.decrypt(result.getString("username")) + "~" + (userOnline(result.getString("username")) ? true : false));
			
			// find all other users
			statement = database.prepareStatement("SELECT * FROM users WHERE user_key != '" + packet.getSenderKey() + "' ORDER BY last_action_date DESC;");
			result = statement.executeQuery();
			
			// get other usernames and put into array
			ArrayList<String> othersList = new ArrayList<String>();
			while (result.next())
				if (!friendsList.contains(Security.decrypt(result.getString("username"))))
					othersList.add(Security.decrypt(result.getString("username")) + "~" + (userOnline(result.getString("username")) ? true : false));
			
			// build Strings for friends and other users
			// only send 20 usernames at a time, tell the client how many packets to expect
			int packetsToSend = (friendsList.size() + othersList.size()) / 20;
			if ((friendsList.size() + othersList.size()) % 20 != 0 || packetsToSend == 0)
				packetsToSend++;
			int usernamesAdded = 0;
			
			String friends = "";
			
			for (; usernamesAdded < friendsList.size(); usernamesAdded++) {
				friends += friendsList.get(usernamesAdded);
				if (usernamesAdded != friendsList.size()-1 && usernamesAdded % 20 != 0)
					friends += ",";
				// if 20 usernames reached, send packet and clear friends
				if (usernamesAdded != 0 && usernamesAdded % 20 == 0) {
					System.out.println("Friends List: " + friends);
					Packet04GetPlayers returnPacket = new Packet04GetPlayers(packet.getUuidKey(), packetsToSend, usernamesAdded / 20, friends, "", true);
					returnPacket.writeData(this, address, port);
					friends = "";
				}
			}
			String others = "";
			for (int i = 0; i < othersList.size(); i++) {
				usernamesAdded++;
				others += othersList.get(i);
				if (i != othersList.size()-1 && usernamesAdded % 20 != 0)
					others += ",";
				// if 20 usernames reached, send packet, clear friends and other
				if (usernamesAdded != 0 && usernamesAdded % 20 == 0) {
					System.out.println("Friends List: " + friends);
					System.out.println("Others  List: " + others);
					Packet04GetPlayers returnPacket = new Packet04GetPlayers(packet.getUuidKey(), packetsToSend, usernamesAdded / 20, friends, others, true);
					returnPacket.writeData(this, address, port);
					friends = "";
					others = "";
				}
			}
			
			if (usernamesAdded % 20 != 0) {
				Packet04GetPlayers returnPacket = new Packet04GetPlayers(packet.getUuidKey(), packetsToSend, usernamesAdded / 20 + 1, friends, others, true);
				returnPacket.writeData(this, address, port);
			}
			
			System.out.println("Friends List: " + friends);
			System.out.println("Others  List: " + others);
			System.out.println("RETURN GET PLAYERS SENT");
			
			updateSender(packet);
			setSenderGameKey(packet.getSenderKey(), null);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Attempts to add a user as a friend to the client
	 * Refreshes the CreateAccountScreen by returning a 04 packet
	 * @param data
	 * @param address
	 * @param port
	 */
	private void addFriend(byte[] data, InetAddress address, int port) {
		try {
			Packet05AddFriend packet = new Packet05AddFriend(data);
			if (!packet.isValid())
				return;
			
			// get other user's key
			PreparedStatement getKey = database.prepareStatement("SELECT * FROM users WHERE username = '"
																+ Security.encrypt(packet.getFriendName()) + "';");
			ResultSet result = getKey.executeQuery();

			// verify that other user exists
			if (!result.next()) {
				// TODO error other user does not exist
				return;
			}
			String otherKey = result.getString("user_key");
			
			// verify that player is not already friends with other
			PreparedStatement statement = database.prepareStatement("SELECT * FROM friends WHERE userkey1 = '" + packet.getSenderKey()
																	+ "' AND userkey2 = '" + otherKey + "';");
			result = statement.executeQuery();
			
			// if friend set already exists, send error and exit
			if (result.next()) {
				// TODO handles duplicate
				System.out.println("DUPLICATE FRIEND REQUEST ERROR");
				return;
			}

			// add friend set to database
			PreparedStatement add = database.prepareStatement("INSERT INTO friends VALUES ('" + packet.getSenderKey() 
															+ "', '" + otherKey + "' , null);");
			add.executeUpdate();
			
			System.out.println("FRIEND CONNECTION CREATED");
			// refresh players by sending a 04 packet
			Packet04GetPlayers temp = new Packet04GetPlayers(packet.getSenderKey());
			temp.setUuidKey(packet.getUuidKey());
			getPlayers(temp.getData(), address, port);
			
			updateSender(packet);
			setSenderGameKey(packet.getSenderKey(), null);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Attempts to create a new game
	 * If successful returns a 8 packet with new game key
	 * Also sends notification to opponent if online via 8 packet
	 * @param data
	 * @param address
	 * @param port
	 */
	private void createGame(byte[] data, InetAddress address, int port) {
		try {
			Packet06CreateGame packet = new Packet06CreateGame(data);
			if (!packet.isValid())
				return;
			
			// build game board from game type
			String board = BoardGame.createNewBoard(packet.getGameType());
			// if it is null, send error wrong game type and exit
			if (board == null) {
				// TODO send packet
				System.out.println("ERROR gameType: " + packet.getGameType());
				return;
			}
			
			/*
			 *  get other user's key
			 */
			PreparedStatement getKey = database.prepareStatement("SELECT * FROM users WHERE username = '"
																+ Security.encrypt(packet.getOtherUser()) + "';");
			ResultSet result = getKey.executeQuery();

			// verify that other user exists
			if (!result.next()) {
				// TODO error other user does not exist
				return;
			}
			String opponentKey = result.getString("user_key");
			
			// set player1Key and player2Key based on who goes first
			String player1Key = packet.getSenderKey();
			String player2Key = opponentKey;
			
			if (!packet.getCreatorGoesFirst()) {
				String temp = player1Key;
				player1Key = player2Key;
				player2Key = temp;
			}
			
			// create the new game
			String gameKey = "" + UUID.randomUUID();
			PreparedStatement createGame = database.prepareStatement("INSERT INTO games VALUES ('" 
										+ gameKey + "', '" +  player1Key + "', '" + player2Key 
										+ "', " + packet.getGameType() + ", 0, -5, -5, 0, NOW(), '"
										+ board + "');");
			createGame.executeUpdate();
			
			// next create specific game table for extra data
			if(packet.getGameType() < 3) {
				// if go, make a go table as well
				PreparedStatement createGoGame = database.prepareStatement("INSERT INTO go VALUES ('" 
						+ gameKey + "', 0, 0, 0, 0);");
				createGoGame.executeUpdate();
			}
			
			System.out.println("GAME CREATED");
			
			// Send new game data to creator via 8 packet so opens game screen
			Packet08GetBoard temp = new Packet08GetBoard(packet.getSenderKey(), packet.getUsername(), gameKey, packet.getGameType());
			temp.setUuidKey(packet.getUuidKey());		// preserve packet id
			getBoard(temp.getData(), address, port);
			
			// attempt to send data to opponent via 8 packet so sends notification
			for (Player p : onlinePlayers)
				if (p.getUser_key().equals(opponentKey))
					getBoard(temp.getData(), p.getIpAddress(), p.getPort());
			
			updateSender(packet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieves list of game data
	 * @param data
	 * @param address
	 * @param port
	 */
	private void getGames(byte[] data, InetAddress address, int port) {
		try {
			Packet07GetGames packet = new Packet07GetGames(data);
			if (!packet.isValid())
				return;
			
			// find games list
			PreparedStatement statement = database.prepareStatement("SELECT * FROM users RIGHT JOIN games ON users.user_key = games.player2_key"
																+ " WHERE games.player1_key = '" + packet.getSenderKey() + "' ORDER BY games.last_action_date DESC;");
			ResultSet result = statement.executeQuery();
			
			// get game info and put into ArrayList
			ArrayList<String> gameInfo = new ArrayList<String>();
			ArrayList<Timestamp> lastMoved = new ArrayList<Timestamp>();
			while (result.next()) {
				int winner = result.getInt("winner");
				String info = "";
				info += result.getString("game_key") + ",";
				info += result.getString("game_type") + ",";
				info += Security.encrypt(packet.getUsername()) + ",";
				info += result.getString("username") + ",";
				info += result.getInt("moves");
				if (winner != 0)
					info += "," + winner;
				gameInfo.add(info);
				lastMoved.add(result.getTimestamp("last_action_date"));
			}
			
			statement = database.prepareStatement("SELECT * FROM users RIGHT JOIN games ON users.user_key = games.player1_key"
												+ " WHERE games.player2_key = '" + packet.getSenderKey() + "' ORDER BY games.last_action_date DESC;");
			result = statement.executeQuery();

			// get game info and put into ArrayList
			int lastTime = 0;
			while (result.next()) {
				int winner = result.getInt("winner");
				String info = "";
				info += result.getString("game_key") + ",";
				info += result.getString("game_type") + ",";
				info += result.getString("username") + ",";
				info += Security.encrypt(packet.getUsername()) + ",";
				info += result.getInt("moves");
				if (winner != 0)
					info += "," + winner;
				// make sure gameInfo stays ordered by last_action_date
				boolean added = false;
				for (int i = lastTime; i < lastMoved.size() && !(i > lastMoved.size()); i++)
					if (!added && result.getTimestamp("last_action_date").after(lastMoved.get(i))) {
						gameInfo.add(i, info);
						lastMoved.add(i, result.getTimestamp("last_action_date"));
						lastTime = i;
						added = true;
					}
				if (!added) {
					gameInfo.add(info);
					lastMoved.add(result.getTimestamp("last_action_date"));
				}
			}

			// convert ArrayList into array
			int packetsToSend = gameInfo.size() / 10;
			if (packetsToSend % 10 != 0 || packetsToSend == 0)
				packetsToSend++;
			int packetsSent = 0;
			String[] gameInformation = new String[10];
			for (int i = 0; i < gameInfo.size(); i++) {
				gameInformation[i%10] = gameInfo.get(i);
				if ((i % 10 == 0 && i != 0) || i == gameInfo.size()-1) {
					// send games to client, only send 10 games at a time
					Packet07GetGames returnPacket = new Packet07GetGames(packet.getUuidKey(), packetsToSend, ++packetsSent, gameInformation, true);
					returnPacket.writeData(this, address, port);
					gameInformation = new String[10];
				}
			}
			System.out.println("ACTIVE GAMES SENT");
			
			updateSender(packet);
			setSenderGameKey(packet.getSenderKey(), null);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Retrieves a specific game
	 * @param data
	 * @param address
	 * @param port
	 */
	private void getBoard(byte[] data, InetAddress address, int port) {
		try {
			Packet08GetBoard packet = new Packet08GetBoard(data);
			if (!packet.isValid())
				return;
			
			// find String name for gameType so tables can be joined
			String type = getTableName(packet.getGameType());
			
			// if no game name is found
			if (type == null) {
				// TODO send error to client
				System.out.println("ERROR IMPROPER GAME TYPE SENT");
			}
			
			// find game
			PreparedStatement statement = database.prepareStatement("SELECT * FROM games RIGHT JOIN " + type + " ON games.game_key = " + type 
																	+ ".game_key WHERE games.game_key = '" + packet.getGameKey() + "';");
			ResultSet result = statement.executeQuery();
			
			/*
			 *  get game info
			 */
			// if there are no results, exit print error and send error wrong game key
			if (!result.next()) {
				// TODO send error to user
				System.out.println("ERROR IMPROPER GAME KEY SENT");
				return;
			}
			
			int gameType = result.getInt("game_type");
			int moves = result.getInt("moves");
			int penultMove = result.getInt("penult_move");
			int lastMove = result.getInt("last_move");
			String board = result.getString("board");
			int winner = result.getInt("winner");
			
			// get player usernames in proper order
			String player1 = null;
			String player2 = null;
			boolean opponentOnGame = false;
			
			if (packet.getSenderKey().equals(result.getString("player1_key"))) {
				player1 = Security.encrypt(packet.getUsername());
				
				// get opponent's username
				PreparedStatement findUsername = database.prepareStatement("SELECT * FROM users WHERE user_key = '" 
																			+ result.getString("player2_key") + "';");
				ResultSet opponentUser = findUsername.executeQuery();
				opponentUser.next();
				player2 = opponentUser.getString("username");
				opponentOnGame = userOnGame(player2, packet.getGameKey());
			} else  {
				player2 = Security.encrypt(packet.getUsername());
				
				// get opponent's username
				PreparedStatement findUsername = database.prepareStatement("SELECT * FROM users WHERE user_key = '" 
																			+ result.getString("player1_key") + "';");
				ResultSet opponentUser = findUsername.executeQuery();
				opponentUser.next();
				player1 = opponentUser.getString("username");
				opponentOnGame = userOnGame(player1, packet.getGameKey());
			}
			
			// retrieve specific game data
			String extraData = "";
			if (gameType < 3) {
				// handle getting extra data for go games
				extraData += result.getInt("p1_stones_captured");
				extraData += "," + result.getInt("p2_stones_captured");
				extraData += "," + result.getInt("p1_score");
				extraData += "," + result.getInt("p2_score");
			}
			
			// send requested game to client
			Packet08GetBoard returnPacket = new Packet08GetBoard(packet.getUuidKey(), packet.getSenderKey(), packet.getGameKey(), gameType, 
																 player1, player2, moves, penultMove, lastMove, winner, opponentOnGame, board, extraData, true);
			returnPacket.writeData(this, address, port);
			
			System.out.println("GAME SENT");
			
			setSenderGameKey(packet.getSenderKey(), packet.getGameKey());
			updateSender(packet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Verifies a given move, and makes it if valid
	 * @param data
	 * @param address
	 * @param port
	 */
	private void sendMove(byte[] data, InetAddress address, int port) {
		try {
			Packet09SendMove packet = new Packet09SendMove(data);
			if (!packet.isValid())
				return;
			
			// find String name for gameType so tables can be joined
			String type = getTableName(packet.getGameType());
			
			// if no game name is found
			if (type == null) {
				// TODO send error to client
				System.out.println("ERROR IMPROPER GAME TYPE SENT");
			}
			
			// find game
			PreparedStatement statement = database.prepareStatement("SELECT * FROM games RIGHT JOIN " + type + " ON games.game_key = " + type 
																	+ ".game_key WHERE games.game_key = '" + packet.getGameKey() + "';");
			ResultSet result = statement.executeQuery();
			
			/*
			 *  get game info
			 */
			// if there are no results, exit print error and send error wrong game key
			if (!result.next()) {
				// TODO send error to user
				System.out.println("ERROR IMPROPER GAME KEY SENT");
				return;
			}

			// get essential checking information for move
			String player1Key = result.getString("player1_key");
			String player2Key = result.getString("player2_key");
			int gameType = result.getInt("game_type");
			int moves = result.getInt("moves");
			int penultMove = result.getInt("penult_move");
			int lastMove = result.getInt("last_move");
			int winner = result.getInt("winner");
			String oldBoard = result.getString("board");
			boolean resigned = false;
			
			String playerTurnKey;
			String opponentKey;
			if (moves % 2 == 0) {
				playerTurnKey = player1Key;
				opponentKey = player2Key;
			} else {
				playerTurnKey = player2Key;
				opponentKey = player1Key;
			}
			
			// if the game is already over, exit and send error
			if (winner != 0) {
				// TODO send error package
				System.out.println("GAME IS ALREADY OVER");
				return;
			}
			
			// first handle resignation case
			if (packet.getMoveTo() == -2) {
				// if game ends in less than 3 moves, delete the game
				if (moves < 2) {
					deleteGame(packet.getGameKey(), packet.getGameType());
					return;
				}
				if (packet.getSenderKey().equals(player1Key))
					winner = 4;
				else if (packet.getSenderKey().equals(player2Key))
					winner = 3;
				else {
					// TODO send error
					System.out.println("USER KEY NOT IN GAME RESIGNED");
					return;
				}
				resigned = true;
			}
			
			// if it is not the sending player's turn, exit and send error
			if (!resigned && !packet.getSenderKey().equals(playerTurnKey)) {
				// TODO send error package
				System.out.println("NOT SENDING PLAYER'S TURN");
				return;
			}
			
			// make move
			String newBoard = BoardGame.makeMove(gameType, oldBoard, moves % 2 == 0, penultMove, lastMove, packet.getMoveFrom(), packet.getMoveTo());
			
			// if newBoard is null then an improper move was sent, send error and exit
			if (newBoard == null) {
				// TODO send error package
				System.out.println("ILLEGAL MOVE SENT");
				return;
			}
			
			// update game specific info and check for end of game
			String extraInfo = "";
			if (gameType < 3) {
				// if the game is go
				int p1StonesCaptured = result.getInt("p1_stones_captured");
				int p2StonesCaptured = result.getInt("p2_stones_captured");
				int p1Score = 0;
				int p2Score = 0;
				
				// if the user did not pass, calculate score
				if (packet.getMoveTo() > -1) {
					int capturedPieces = -1;	// for use in go games
					// calculate number of captured pieces in a go game
					for (int i = 0; i < oldBoard.length(); i++)
						if (oldBoard.charAt(i) != newBoard.charAt(i))
							capturedPieces++;
					if (moves % 2 == 0)
						p1StonesCaptured += capturedPieces;
					else
						p2StonesCaptured += capturedPieces;
				}
				
				// if the user passed, it is end of game if last move was a pass too
				if (packet.getMoveTo() == -1 && lastMove == -1) {
					// if game ends in less than 3 moves, delete the game
					if (moves < 2) {
						deleteGame(packet.getGameKey(), packet.getGameType());
						return;
					}
					
					// get player scores
					int[] territoryScores = Go.calculateTerritory(oldBoard);
					p1Score = territoryScores[0] + p1StonesCaptured;
					
					// apply Komi
					switch (gameType) {
						case 2:				p2Score += 3;
						case 1:				p2Score += 2;
						default: 			p2Score += territoryScores[1] + 1 + p2StonesCaptured;
					}
					
					winner = 1;
					if (p2Score >= p1Score)
						winner = 2;
				}
				// update go game info
				PreparedStatement updateGoGame = database.prepareStatement("UPDATE go SET " 
						+ "p1_stones_captured = " + p1StonesCaptured + ", p2_stones_captured = " + p2StonesCaptured
						 + ", p1_score = " + p1Score + ", p2_score = " + p2Score + " WHERE game_key = '" + packet.getGameKey() + "';");
				updateGoGame.executeUpdate();
				
				extraInfo += p1StonesCaptured;
				extraInfo += "," + p2StonesCaptured;
				extraInfo += "," + p1Score;
				extraInfo += "," + p2Score;
			}
			
			// update game
			PreparedStatement updateGame = database.prepareStatement("UPDATE games SET " 
					+ "moves = " + ++moves + ", penult_move = " + lastMove + ", last_move = " + packet.getMoveTo()
					 + ", winner = " + winner + ", last_action_date = NOW(), board = '" + newBoard + "' WHERE game_key = '" + packet.getGameKey() + "';");
			updateGame.executeUpdate();
			
			System.out.println("GAME UPDATED");
			
			boolean opponentOnGame = keyOnGame(opponentKey, packet.getGameKey());
			
			// send basic update game info to player via 09 packet
			Packet09SendMove returnPacket = new Packet09SendMove(packet.getUuidKey(), packet.getGameKey(), lastMove,
																packet.getMoveTo(), winner, opponentOnGame, newBoard, extraInfo, true);
			returnPacket.writeData(this, address, port);
			
			/*
			 *  send a 08 packet to opponent to notify or update their client
			 */
			Packet08GetBoard temp = new Packet08GetBoard(packet.getSenderKey(), packet.getUsername(), packet.getGameKey(), gameType);
			temp.setUuidKey(packet.getUuidKey());		// preserve packet id
			
			for (Player p : onlinePlayers)
				if (p.getUser_key().equals(moves % 2 == 0 ? player1Key : player2Key))
					getBoard(temp.getData(), p.getIpAddress(), p.getPort());
			
			updateSender(packet);
			setSenderGameKey(packet.getSenderKey(), packet.getGameKey());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends basic game chat to both players in the game, players must be logged into the game to receive
	 * ***This does not use the database***
	 * @param data
	 * @param address
	 * @param port
	 */
	private void sendChat(byte[] data, InetAddress address, int port) {
		Packet10SendChat packet = new Packet10SendChat(data);
		if (!packet.isValid())
			return;
		
		boolean opponentOnGame = userOnGame(Security.encrypt(packet.getOpponentUsername()), packet.getGameKey());
		
		// create return packet
		Packet10SendChat returnPacket = new Packet10SendChat(packet.getUuidKey(), packet.getSenderKey(), packet.getGameKey(), opponentOnGame, packet.getText(), true);
		
		// send chat back to sender
		returnPacket.writeData(this, address, port);
		
		// send chat to opponent (the opponent must be on the game to send the chat)
		returnPacket.setOpponentOnGame(true);
		for (Player p : onlinePlayers)
			if (p.getUsername().equals(Security.encrypt(packet.getOpponentUsername())))
				returnPacket.writeData(this, p.getIpAddress(), p.getPort());
		
		updateSender(packet);
		setSenderGameKey(packet.getSenderKey(), packet.getGameKey());
	}

	/**
	 * Updates sender's gameKey on server, sends 11 update by effect
	 * ***This does not use database***
	 * @param data
	 * @param address
	 * @param port
	 */
	private void onGame(byte[] data, InetAddress address, int port) {
		Packet11OnGame packet = new Packet11OnGame(data);
		
		updateSender(packet);
		setSenderGameKey(packet.getSenderKey(), packet.getGameKey());
	}

	/**
	 * Returns the table name of a given game type
	 * @param gameType
	 * @return
	 */
	private String getTableName(int gameType) {
		switch (gameType) {
			case 0:
			case 1:
			case 2:						return "go";
			default:					return null;
		}
	}

	/**
	 * Updates the last_action_date of the sender
	 * @param packet
	 */
	private void updateSender(Packet packet) {
		updateSender(packet.getSenderKey());
	}

	/**
	 * Updates the last_action_date of the sender
	 * @param packet
	 */
	private void updateSender(String senderKey) {
		try {
			// update sender
			PreparedStatement updateSender = database.prepareStatement("UPDATE users SET last_action_date = NOW()"
					+ " WHERE user_key = '" + senderKey + "';");
			updateSender.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the sender's current game to gameKey
	 * Sends 11 packet to other clients in past game
	 * @param senderKey
	 * @param gameKey
	 */
	private void setSenderGameKey(String senderKey, String gameKey) {
		for (Player p : onlinePlayers)
			if (p.getUser_key().equals(senderKey)) {
				// send player is leaving GameScreen
				if (p.getGame_key() != null && !p.getGame_key().equals(gameKey))
					sendOnGame(p.getGame_key(), p.getUsername(), false);
				// send player joining GameScreen
				if (gameKey != null && !gameKey.equals(p.getGame_key()))
					sendOnGame(gameKey, p.getUsername(), true);
				p.setGame_key(gameKey);
			}
	}

	/**
	 * Sends 11 packet to clients in given game
	 * @param game_key
	 * @param username
	 * @param onGame
	 */
	private void sendOnGame(String game_key, String username, boolean onGame) {
		if (game_key == null)
			return;
		Packet11OnGame packet = new Packet11OnGame(game_key, username, onGame, true);
		for (Player p : onlinePlayers)
			if (game_key.equals(p.getGame_key()) && !p.getUsername().equals(username))
				packet.writeData(this, p.getIpAddress(), p.getPort());
	}

	/**
	 * Returns true if the given username is on the given game
	 * @param username
	 * @param gameKey
	 * @return
	 */
	private boolean userOnGame(String username, String gameKey) {
		for (Player p : onlinePlayers)
			if (p.getUsername().equals(username) && gameKey.equals(p.getGame_key()))
				return true;
		return false;
	}

	/**
	 * Returns true if the given user key is on the given game
	 * @param userKey
	 * @param gameKey
	 * @return
	 */
	private boolean keyOnGame(String userKey, String gameKey) {
		for (Player p : onlinePlayers)
			if (p.getUser_key().equals(userKey) && p.getGame_key() != null && p.getGame_key().equals(gameKey))
				return true;
		return false;
	}

	/**
	 * Returns true if the given username is logged into the server
	 * @param string
	 * @return
	 */
	private boolean userOnline(String username) {
		for (Player p : onlinePlayers)
			if (p.getUsername().equals(username))
				return true;
		return false;
	}

	/**
	 * Deletes a given game
	 * @param gameKey
	 * @param gameType
	 */
	private void deleteGame(String gameKey, int gameType) {
		try {
			// delete base game data
			PreparedStatement deleteGame = database.prepareStatement("DELETE FROM games "
					+ " WHERE game_key = '" + gameKey + "';");
			deleteGame.executeUpdate();
			
			// delete game specific data
			deleteGame = database.prepareStatement("DELETE FROM " + getTableName(gameType)
					+ " WHERE game_key = '" + gameKey + "';");
			deleteGame.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
