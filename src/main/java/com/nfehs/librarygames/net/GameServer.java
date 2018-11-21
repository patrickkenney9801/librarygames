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
import java.util.HashSet;
import java.util.UUID;

import com.nfehs.librarygames.Player;
import com.nfehs.librarygames.games.BoardGame;
import com.nfehs.librarygames.games.go.Go;
import com.nfehs.librarygames.net.packets.*;
import com.nfehs.librarygames.net.packets.Packet.PacketTypes;
import com.nfehs.librarygames.net.packets.Packet02Error.ErrorType;

/**
 * This class handles receiving packets from and sending to the client
 * @author Patrick Kenney, Syed Quadri
 * @date 6/13/2018
 */

public class GameServer extends Thread {

	private DatagramSocket socket;
	private Connection database;
	private static final int PORT = 19602;
	private final String DATABASE_USER = "pkenney";
	private final String DATABASE_PASS = "9801";
	
	private ArrayList<Player> onlinePlayers;
	private HashSet<String> sentPackets;
	// HashMap <session_keys, player>
	// HashMap <game_key, ArrayList<player>>
	// HashMap <user_keys, session_keys>
	
	public GameServer() {
		try {
			this.database = getConnection();
			System.out.println("Connected to database");
			this.socket = new DatagramSocket(PORT);
			System.out.println("Connected to UDP");
			this.onlinePlayers = new ArrayList<Player>();
			setSentPackets(new HashSet<String>());
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
			// do not output messages
			if (new String(packet.getData()).substring(0, 2).equals("10"))
				System.out.println("CLIENT > PACKET 10 RECEIVED");
			else
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
					case ERROR:					// probably will not be used by server
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
												break;
					case GETSPECTATES:			getSpectates(data, address, port);
												break;
					case RECEIPT:				handleReceipt(data, address, port);
					default:					break;
				}
			}
		}).start();
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
			PreparedStatement statement = database.prepareStatement("SELECT user_key FROM users WHERE username = '"
																	+ packet.getUsername() + "' AND password = '"
																	+ packet.getPassword() + "';");
			ResultSet result = statement.executeQuery();
			
			// if there is not a result, send error incorrect credentials
			if (!result.next()) {
				Packet02Error errorPacket = new Packet02Error(packet.getUuidKey(), ErrorType.PACKET00_INVALID_CREDENTIALS, true);
				errorPacket.writeData(this, address, port);
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
			
			// make sure that username, password, email are the right length, if not exit
			if (packet.getUsername().length() > 100 || packet.getPassword().length() > 100 || packet.getEmail().length() > 30
					|| packet.getUsername().length() == 0 || packet.getPassword().length() == 0)
				return;
			
			try {
				// verify that both username and password are Base64 encrypted
				String rawUsername = Security.decrypt(packet.getUsername());
				Security.decrypt(packet.getPassword());
				
				// if the username contains ~ or , or : it is an invalid username
				for (char c : rawUsername.toCharArray())
					if (c == '~' || c == ',' || c == ':') {
						// send error invalid username back
						Packet02Error errorPacket = new Packet02Error(packet.getUuidKey(), ErrorType.PACKET01_INVALID_USERNAME, true);
						errorPacket.writeData(this, address, port);
						System.out.println("Invalid username: " + rawUsername);
						return;
					}
			} catch (Exception e) {
				// send error invalid encryption back
				Packet02Error errorPacket = new Packet02Error(packet.getUuidKey(), ErrorType.PACKET01_INVALID_ENCRYPTION, true);
				errorPacket.writeData(this, address, port);
				System.out.println("Invalid encryption");
				e.printStackTrace();
				return;
			}
			
			// verify that the username is not already in use
			PreparedStatement statement = database.prepareStatement("SELECT * FROM users WHERE username = '" + packet.getUsername() + "';");
			ResultSet result = statement.executeQuery();
			
			// if the username is already in use, do not create new account and send back error package
			if (result.next()) {
				Packet02Error errorPacket = new Packet02Error(packet.getUuidKey(), ErrorType.PACKET01_USERNAME_IN_USE, true);
				errorPacket.writeData(this, address, port);
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
			PreparedStatement statement = database.prepareStatement("SELECT users.username FROM users RIGHT JOIN friends ON users.user_key = friends.userkey2"
																+ " WHERE userkey1 = '" + packet.getSenderKey() + "' ORDER BY last_action_date DESC;");
			ResultSet result = statement.executeQuery();
			
			// get friends usernames and put into array
			ArrayList<String> friendsList = new ArrayList<String>();
			while (result.next())
				friendsList.add(Security.decrypt(result.getString("username")) + "~" + (userOnline(result.getString("username")) ? true : false));
			
			// find all other users
			statement = database.prepareStatement("SELECT username FROM users WHERE user_key != '" + packet.getSenderKey() + "' ORDER BY last_action_date DESC;");
			result = statement.executeQuery();
			
			// get other usernames and put into array
			ArrayList<String> othersList = new ArrayList<String>();
			while (result.next())
				if (!friendsList.contains(Security.decrypt(result.getString("username")) + "~true")
						&& !friendsList.contains(Security.decrypt(result.getString("username")) + "~false"))
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
				if (usernamesAdded != friendsList.size()-1 && (usernamesAdded % 20 != 0 || usernamesAdded == 0))
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
				if (i != othersList.size()-1 && (usernamesAdded % 20 != 0 || usernamesAdded == 0))
					others += ",";
				// if 20 usernames reached, send packet, clear friends and other
				if (usernamesAdded % 20 == 19) {
					System.out.println("Friends List: " + friends);
					System.out.println("Others  List: " + others);
					Packet04GetPlayers returnPacket = new Packet04GetPlayers(packet.getUuidKey(), packetsToSend, usernamesAdded / 20, friends, others, true);
					returnPacket.writeData(this, address, port);
					friends = "";
					others = "";
				}
			}
			
			if (usernamesAdded % 20 != 19) {
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
			PreparedStatement getKey = database.prepareStatement("SELECT user_key FROM users WHERE username = '"
																+ Security.encrypt(packet.getFriendName()) + "';");
			ResultSet result = getKey.executeQuery();

			// verify that other user exists
			if (!result.next()) {
				// send error other user does not exist back
				Packet02Error errorPacket = new Packet02Error(packet.getUuidKey(), ErrorType.PACKET05_FRIEND_DOES_NOT_EXIST, true);
				errorPacket.writeData(this, address, port);
				System.out.println("Other user does not exist");
				return;
			}
			String otherKey = result.getString("user_key");
			
			// verify that player is not already friends with other
			PreparedStatement statement = database.prepareStatement("SELECT * FROM friends WHERE userkey1 = '" + packet.getSenderKey()
																	+ "' AND userkey2 = '" + otherKey + "';");
			result = statement.executeQuery();
			
			// if friend set already exists, send error and exit
			if (result.next()) {
				// send user is already friends with other user error back
				Packet02Error errorPacket = new Packet02Error(packet.getUuidKey(), ErrorType.PACKET05_ALREADY_FRIENDS, true);
				errorPacket.writeData(this, address, port);
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
				// send invalid gameType error back
				Packet02Error errorPacket = new Packet02Error(packet.getUuidKey(), ErrorType.PACKET06_INVALID_GAMETYPE, true);
				errorPacket.writeData(this, address, port);
				System.out.println("ERROR gameType: " + packet.getGameType());
				return;
			}
			
			/*
			 *  get other user's key
			 */
			PreparedStatement getKey = database.prepareStatement("SELECT user_key FROM users WHERE username = '"
																+ Security.encrypt(packet.getOtherUser()) + "';");
			ResultSet result = getKey.executeQuery();

			// verify that other user exists
			if (!result.next()) {
				// send invalid opponent error back
				Packet02Error errorPacket = new Packet02Error(packet.getUuidKey(), ErrorType.PACKET06_OPPONENT_DOES_NOT_EXIST, true);
				errorPacket.writeData(this, address, port);
				System.out.println("OPPONENT DOES NOT EXIST");
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
			
			/*
			 *  verify that an active game of the same type does not already exist
			 */
			// get all unfinished games between both players of same gameType and order
			PreparedStatement getMatchingGames = database.prepareStatement("SELECT game_key FROM games WHERE winner = 0 AND game_type = "
												+ packet.getGameType() + " AND player1_key = '" + player1Key + "' AND player2_key = '" + player2Key + "';");
			ResultSet matchingGames = getMatchingGames.executeQuery();
			
			// if there is a matching unfinished game, send error to client and exit
			if (matchingGames.next()) {
				// send game already exists error back
				Packet02Error errorPacket = new Packet02Error(packet.getUuidKey(), ErrorType.PACKET06_DUPLICATE_GAME, true);
				errorPacket.writeData(this, address, port);
				System.out.println("DUPLICATE GAME DENIED");
				return;
			}
			
			// create the new game
			String gameKey = "" + UUID.randomUUID();
			PreparedStatement createGame = database.prepareStatement("INSERT INTO games VALUES ('" 
										+ gameKey + "', '" +  player1Key + "', '" + player2Key 
										+ "', " + packet.getGameType() + ", 0, -5, -5, 0, NOW(), '"
										+ board + "');");
			createGame.executeUpdate();
			
			// next create specific game table for extra data
			PreparedStatement createGameSpecificData = database.prepareStatement(
					BoardGame.createExtraGameInfo(packet.getGameType(), gameKey));
			createGameSpecificData.executeUpdate();
			
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
			PreparedStatement statement = database.prepareStatement("SELECT game_key, game_type, moves, winner, games.last_action_date, username FROM users RIGHT JOIN games ON users.user_key = games.player2_key"
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
			
			statement = database.prepareStatement("SELECT game_key, game_type, moves, winner, games.last_action_date, username FROM users RIGHT JOIN games ON users.user_key = games.player1_key"
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
				if (i % 10 == 9) {
					// send games to client, only send 10 games at a time
					Packet07GetGames returnPacket = new Packet07GetGames(packet.getUuidKey(), packetsToSend, ++packetsSent, gameInformation, true);
					returnPacket.writeData(this, address, port);
					gameInformation = new String[10];
				}
			}
			if (packetsToSend % 10 != 9) {
				// send games to client, only send 10 games at a time
				Packet07GetGames returnPacket = new Packet07GetGames(packet.getUuidKey(), packetsToSend, ++packetsSent, gameInformation, true);
				returnPacket.writeData(this, address, port);
				gameInformation = new String[10];
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
				// send invalid gameType error back
				Packet02Error errorPacket = new Packet02Error(packet.getUuidKey(), ErrorType.PACKET08_INVALID_GAME_TYPE, true);
				errorPacket.writeData(this, address, port);
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
				// send invalid game key error back
				Packet02Error errorPacket = new Packet02Error(packet.getUuidKey(), ErrorType.PACKET08_INVALID_GAME_KEY, true);
				errorPacket.writeData(this, address, port);
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
			boolean player1OnGame = false;
			boolean player2OnGame = false;
			
			if (packet.getSenderKey().equals(result.getString("player1_key"))) {
				player1 = Security.encrypt(packet.getUsername());
				
				// get opponent's username
				PreparedStatement findUsername = database.prepareStatement("SELECT username FROM users WHERE user_key = '" 
																			+ result.getString("player2_key") + "';");
				ResultSet opponentUser = findUsername.executeQuery();
				opponentUser.next();
				player2 = opponentUser.getString("username");
			} else if (packet.getSenderKey().equals(result.getString("player2_key"))) {
				player2 = Security.encrypt(packet.getUsername());
				
				// get opponent's username
				PreparedStatement findUsername = database.prepareStatement("SELECT username FROM users WHERE user_key = '" 
																			+ result.getString("player1_key") + "';");
				ResultSet opponentUser = findUsername.executeQuery();
				opponentUser.next();
				player1 = opponentUser.getString("username");
			} else {
				// for spectators
				// get players' usernames
				PreparedStatement findUsername = database.prepareStatement("SELECT username, user_key FROM users WHERE user_key = '" 
										+ result.getString("player1_key") + "' OR user_key = '" + result.getString("player2_key") + "';");
				ResultSet user = findUsername.executeQuery();
				while (user.next()) {
					if (user.getString("user_key").equals(result.getString("player1_key")))
						player1 = user.getString("username");
					else
						player2 = user.getString("username");
				}
			}
			setSenderGameKey(packet.getSenderKey(), packet.getGameKey());
			player1OnGame = userOnGame(player1, packet.getGameKey());
			player2OnGame = userOnGame(player2, packet.getGameKey());
			
			// retrieve specific game data
			String extraData = BoardGame.getExtraGameData(gameType, result);
			
			// send requested game to client, expect receipt
			Packet08GetBoard returnPacket = new Packet08GetBoard(packet.getUuidKey(), packet.getSenderKey(), packet.getGameKey(), gameType, 
							player1, player2, moves, penultMove, lastMove, winner, player1OnGame, player2OnGame, board, extraData, true);
			sendPacket(returnPacket, address, port);
			
			System.out.println("GAME SENT");
			
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
				// send invalid game key error back
				Packet02Error errorPacket = new Packet02Error(packet.getUuidKey(), ErrorType.PACKET09_INVALID_GAME_TYPE, true);
				errorPacket.writeData(this, address, port);
				System.out.println("ERROR IMPROPER GAME TYPE SENT");
				return;
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
				// send invalid game key error back
				Packet02Error errorPacket = new Packet02Error(packet.getUuidKey(), ErrorType.PACKET09_INVALID_GAME_KEY, true);
				errorPacket.writeData(this, address, port);
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
			}
			else {
				playerTurnKey = player2Key;
				opponentKey = player1Key;
			}
			
			// if the game is already over, exit and send error
			if (winner != 0) {
				// send game already over error back
				Packet02Error errorPacket = new Packet02Error(packet.getUuidKey(), ErrorType.PACKET09_GAME_ALREADY_OVER, true);
				errorPacket.writeData(this, address, port);
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
					// send sender not in game error back
					Packet02Error errorPacket = new Packet02Error(packet.getUuidKey(), ErrorType.PACKET09_SENDER_NOT_IN_GAME, true);
					errorPacket.writeData(this, address, port);
					System.out.println("USER KEY NOT IN GAME RESIGNED");
					return;
				}
				resigned = true;
			}
			
			// if it is not the sending player's turn, send 08 packet to update board and exit
			if (!resigned && !packet.getSenderKey().equals(playerTurnKey)) {
				Packet08GetBoard temp = new Packet08GetBoard(packet.getSenderKey(), packet.getUsername(), packet.getGameKey(), gameType);
				temp.setUuidKey(packet.getUuidKey());		// preserve packet id
				getBoard(temp.getData(), address, port);
				System.out.println("NOT SENDING PLAYER'S TURN");
				return;
			}
			
			// make move
			String newBoard = BoardGame.makeMove(gameType, oldBoard, moves % 2 == 0, penultMove, lastMove, packet.getMoveFrom(), packet.getMoveTo());
			
			// if newBoard is null then an improper move was sent, send error and exit
			if (newBoard == null) {
				// send illegal move error back
				Packet02Error errorPacket = new Packet02Error(packet.getUuidKey(), ErrorType.PACKET09_ILLEGAL_MOVE, true);
				errorPacket.writeData(this, address, port);
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
				String olderBoard = Go.retrieveOldBoard(newBoard);
				String newestBoard = Go.retrieveCurrentBoard(newBoard);
				
				// if the user did not pass, calculate score
				if (packet.getMoveTo() > -1) {
					int capturedPieces = -1;	// for use in go games
					// calculate number of captured pieces in a go game
					for (int i = 0; i < olderBoard.length(); i++)
						if (olderBoard.charAt(i) != newestBoard.charAt(i))
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
					int[] territoryScores = Go.calculateTerritory(Go.retrieveCurrentBoard(oldBoard));
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
			// update specific game info and handle end of game
			// if (gameType == 3) { TODO
			
			// update game and move count
			PreparedStatement updateGame = database.prepareStatement("UPDATE games SET " 
					+ "moves = " + ++moves + ", penult_move = " + lastMove + ", last_move = " + packet.getMoveTo()
					 + ", winner = " + winner + ", last_action_date = NOW(), board = '" + newBoard + "' WHERE game_key = '" + packet.getGameKey() + "';");
			updateGame.executeUpdate();
			
			System.out.println("GAME UPDATED");

			setSenderGameKey(packet.getSenderKey(), packet.getGameKey());
			boolean player1OnGame = keyOnGame(player1Key, packet.getGameKey());
			boolean player2OnGame = keyOnGame(player2Key, packet.getGameKey());
			
			// send basic update game info to player via 09 packet, expect receipt response
			Packet09SendMove returnPacket = new Packet09SendMove(packet.getUuidKey(), packet.getGameKey(), lastMove, packet.getMoveTo(),
																	moves, winner, player1OnGame, player2OnGame, newBoard, extraInfo, true);
			sendPacket(returnPacket, address, port);
			
			/*
			 *  send a 08 packet to opponent and spectators to notify or update their client
			 */
			Packet08GetBoard temp = new Packet08GetBoard(packet.getSenderKey(), packet.getUsername(), packet.getGameKey(), gameType);
			temp.setUuidKey(packet.getUuidKey());		// preserve packet id
			
			for (Player p : onlinePlayers)
				// send to opponent if online and to spectators
				if (opponentKey.equals(p.getUser_key()) 
						|| packet.getGameKey().equals(p.getGame_key()) && !p.getUser_key().equals(packet.getSenderKey()))
					getBoard(temp.getData(), p.getIpAddress(), p.getPort());
			
			updateSender(packet);
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
		String senderKey = packet.getSenderKey();
		
		setSenderGameKey(senderKey, packet.getGameKey());
		boolean player1OnGame = userOnGame(Security.encrypt(packet.getPlayer1Username()), packet.getGameKey());
		boolean player2OnGame = userOnGame(Security.encrypt(packet.getPlayer2Username()), packet.getGameKey());
		
		// create return packet
		Packet10SendChat returnPacket = new Packet10SendChat(packet.getUuidKey(), senderKey, packet.getGameKey(), player1OnGame, player2OnGame, packet.getText(), true);
		sendPacket(returnPacket, address, port);
		// set senderKey for others null to increase security
		returnPacket = new Packet10SendChat(packet.getUuidKey(), null, packet.getGameKey(), player1OnGame, player2OnGame, packet.getText(), true);
		// send chat to players (the opponent must be on the game to send the chat), expect receipts
		// also send chat to spectators if option is on
		boolean sendToSpectators = packet.isSendToSpectators();
		for (Player p : onlinePlayers)
			if (!packet.getSenderKey().equals(p.getUser_key()) && packet.getGameKey().equals(p.getGame_key()))
				if (sendToSpectators)
					sendPacket(returnPacket, p.getIpAddress(), p.getPort());
				else if (p.getUsername().equals(Security.encrypt(packet.getPlayer1Username()))
						|| p.getUsername().equals(Security.encrypt(packet.getPlayer2Username())))
					sendPacket(returnPacket, p.getIpAddress(), p.getPort());
		
		updateSender(packet);
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
			case 3:						return "xiangqi";
			default:					return null;
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
	 * Retrieves list of game data for spectators, does not include sender's own games or finished games
	 * @param data
	 * @param address
	 * @param port
	 */
	private void getSpectates(byte[] data, InetAddress address, int port) {
		try {
			Packet12GetSpectates packet = new Packet12GetSpectates(data);
			if (!packet.isValid())
				return;
			
			// find players list
			PreparedStatement players = database.prepareStatement("SELECT username, user_key FROM users;");
			ResultSet playersResult = players.executeQuery();
			
			// get user info and put into ArrayList
			ArrayList<ArrayList<String>> usersInfo = new ArrayList<ArrayList<String>>();
			while (playersResult.next()) {
				ArrayList<String> temp = new ArrayList<String>(2);
				temp.add(playersResult.getString("user_key"));
				temp.add(playersResult.getString("username"));
				usersInfo.add(temp);
			}
			
			// find spectator games list
			PreparedStatement games = database.prepareStatement("SELECT game_key, game_type, player1_key, player2_key, moves FROM games WHERE player1_key != '" + packet.getSenderKey()
							+ "' AND player2_key != '" + packet.getSenderKey() + "' AND winner = 0 ORDER BY games.last_action_date DESC;");
			ResultSet gamesResult = games.executeQuery();
			
			// get game info and put into ArrayList
			ArrayList<String> gameInfo = new ArrayList<String>();
			while (gamesResult.next()) {
				String info = "";
				info += gamesResult.getString("game_key") + ",";
				info += gamesResult.getString("game_type") + ",";
				
				String user1 = null;
				String user2 = null;
				for (int i = 0; i < usersInfo.size(); i++) {
					if (usersInfo.get(i).get(0).equals(gamesResult.getString("player1_key")))
						user1 = usersInfo.get(i).get(1);
					else if (usersInfo.get(i).get(0).equals(gamesResult.getString("player2_key")))
						user2 = usersInfo.get(i).get(1);
				}
				info += user1 + ",";
				info += user2 + ",";
				info += gamesResult.getInt("moves");
				gameInfo.add(info);
			}

			// convert ArrayList into array
			int packetsToSend = gameInfo.size() / 10;
			if (packetsToSend % 10 != 0 || packetsToSend == 0)
				packetsToSend++;
			int packetsSent = 0;
			String[] gameInformation = new String[10];
			for (int i = 0; i < gameInfo.size(); i++) {
				gameInformation[i%10] = gameInfo.get(i);
				if (i % 10 == 9) {
					// send games to client, only send 10 games at a time
					Packet12GetSpectates returnPacket = new Packet12GetSpectates(packet.getUuidKey(), packetsToSend, ++packetsSent, gameInformation, true);
					returnPacket.writeData(this, address, port);
					gameInformation = new String[10];
				}
			}
			if (packetsToSend % 10 != 0) {
				// send games to client, only send 10 games at a time
				Packet12GetSpectates returnPacket = new Packet12GetSpectates(packet.getUuidKey(), packetsToSend, ++packetsSent, gameInformation, true);
				returnPacket.writeData(this, address, port);
				gameInformation = new String[10];
			}
			
			System.out.println("SPECTATE GAMES SENT");
			
			updateSender(packet);
			setSenderGameKey(packet.getSenderKey(), null);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles a receipt by setting the HashMap value with the packet identifier true
	 * @param data
	 * @param address
	 * @param port
	 */
	private void handleReceipt(byte[] data, InetAddress address, int port) {
		Packet13Receipt packet = new Packet13Receipt(data);
		if (!packet.isValid())
			return;
		// remove the packetIdentifier from HashSet
		String packetIdentifier = packet.getUuidKey() + address.toString();
		getSentPackets().remove(packetIdentifier);
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
	 * Sends a packet to the given address and port, expecting a response
	 * Attempts up to 5 times, if failure removes the address from onlinePlayers
	 * @param packet
	 * @param address
	 * @param port
	 */
	private void sendPacket(final Packet packet, final InetAddress address, final int port) {
		// add packet identifier to HashMap
		final String packetIdentifier = packet.getUuidKey() + address.toString();
		getSentPackets().add(packetIdentifier);
		new Thread(new Runnable() {
			public void run() {
				// send packet and then check for receipt 500ms later, if receipt received exit
				if (sendPacket(packet, address, port, packetIdentifier, 500))
					return;
				if (sendPacket(packet, address, port, packetIdentifier, 500))
					return;
				if (sendPacket(packet, address, port, packetIdentifier, 750))
					return;
				if (sendPacket(packet, address, port, packetIdentifier, 1000))
					return;
				if (sendPacket(packet, address, port, packetIdentifier, 5000))
					return;
				// if a response is not received after 5 tries, remove players with address from onlinePlayers
				for (int i = 0; i < onlinePlayers.size(); i++)
					if (onlinePlayers.get(i).getIpAddress().equals(address)) {
						sendOnGame(onlinePlayers.get(i).getGame_key(), onlinePlayers.get(i).getUsername(), false);
						onlinePlayers.remove(i);
					}
				getSentPackets().remove(packetIdentifier);
			}
		}).start();
	}
	
	/**
	 * Actually sends packet to address, waits waitTime to check for receipt, handles removal of packetIdentifier
	 * Returns true if the packet got a receipt
	 * @param packet
	 * @param address
	 * @param port
	 * @param packetIdentifier
	 * @param waitTime
	 */
	private boolean sendPacket(Packet packet, InetAddress address, int port, String packetIdentifier, int waitTime) {
		try {
			// send packet and wait for waitTime
			packet.writeData(this, address, port);
			sleep(waitTime);
			
			// return if sent packet still exists
			return !getSentPackets().contains(packetIdentifier);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
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
			
			System.out.println("GAME DELETED");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public HashSet<String> getSentPackets() {
		return sentPackets;
	}

	public void setSentPackets(HashSet<String> sentPackets) {
		this.sentPackets = sentPackets;
	}
}
