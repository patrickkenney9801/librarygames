package com.nfehs.librarygames.net.packets;

/**
 * Retrieves the data for a game
 * @author Patrick Kenney and Syed Quadri
 * @date 8/14/2018
 */

public class Packet08GetBoard extends Packet {
	// data to send to server
	private String userKey;
	private String username;
	private String gameKey;
	
	// data to send to client
	// userKey
	// gameKey
	private int gameType;
	private String player1;
	private String player2;
	private boolean player1Turn;
	private int penultMove;
	private int lastMove;
	private int player1Score;
	private int player2Score;
	private int winner;
	private String board;

	/**
	 * Used by client to send data to server
	 * @param userkey
	 * @param username
	 * @param gameKey
	 */
	public Packet08GetBoard(String userkey, String username, String gameKey) {
		super(8);
		setUserKey(userkey);
		setUsername(username);
		setGameKey(gameKey);
	}
	
	/**
	 * Used by server to retrieve data from client's packet
	 * @param data
	 */
	public Packet08GetBoard(byte[] data) {
		super(8);
		String[] userdata = readData(data).split(":");
		setUuidKey(userdata[0]);
		setUserKey(userdata[1]);
		setUsername(userdata[2]);
		setGameKey(userdata[3]);
	}
	
	/**
	 * Used by server to send data to client
	 * @param packetKey
	 * @param userKey
	 * @param gameKey
	 * @param gameType
	 * @param player1
	 * @param player2
	 * @param player1Turn
	 * @param penultMove
	 * @param lastMove
	 * @param player1Score
	 * @param player2Score
	 * @param winner
	 * @param board
	 * @param serverUse boolean that serves no purpose other than to distinguish constructors
	 */
	public Packet08GetBoard(String packetKey, String userKey, String gameKey, int gameType, 
							String player1, String player2, boolean player1Turn, int penultMove, int lastMove,
							int player1Score, int player2Score, int winner, String board, boolean serverUse) {
		super(8);
		setUuidKey(packetKey);
		setUserKey(userKey);
		setGameKey(gameKey);
		setGameType(gameType);
		setPlayer1(player1);
		setPlayer2(player2);
		setPlayer1Turn(player1Turn);
		setPenultMove(penultMove);
		setLastMove(lastMove);
		setPlayer1Score(player1Score);
		setPlayer2Score(player2Score);
		setWinner(winner);
		setBoard(board);
	}
	
	/**
	 * Used by server to retrieve data from client's data packet
	 * @param data
	 * @param serverUse boolean that serves no purpose other than to distinguish constructors
	 */
	public Packet08GetBoard(byte[] data, boolean serverUse) {
		super(8);
		String[] userdata = readData(data).split(":");
		setUuidKey(userdata[0]);
		setUserKey(userdata[1]);
		setGameKey(userdata[2]);
		setPlayer1(userdata[4]);
		setPlayer2(userdata[5]);
		setPlayer1Turn(Boolean.parseBoolean(userdata[6]));
		setBoard(userdata[12]);
		try {
			setGameType(Integer.parseInt(userdata[3]));
			setPenultMove(Integer.parseInt(userdata[7]));
			setLastMove(Integer.parseInt(userdata[8]));
			setPlayer1Score(Integer.parseInt(userdata[9]));
			setPlayer2Score(Integer.parseInt(userdata[10]));
			setWinner(Integer.parseInt(userdata[11]));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	@Override
	public byte[] getData() {
		return ("08" + getUuidKey() + ":" + getUserKey() + ":" + getUsername() + ":" + getGameKey()).getBytes();
	}

	@Override
	public byte[] getDataServer() {
		return ("08" + getUuidKey() + ":" + getUserKey() + ":" + getGameKey() + ":" + getGameType()
		 		+ ":" + getPlayer1() + ":" + getPlayer2() + ":" + isPlayer1Turn() + ":" + getPenultMove() + ":" + getLastMove()
		 		+ ":" + getPlayer1Score() + ":" + getPlayer2Score() + ":" + getWinner() + ":" + getBoard()).getBytes();
	}

	/**
	 * @return the userKey
	 */
	public String getUserKey() {
		return userKey;
	}

	/**
	 * @param userKey the userKey to set
	 */
	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
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
	 * @return the gameType
	 */
	public int getGameType() {
		return gameType;
	}

	/**
	 * @param gameType the gameType to set
	 */
	public void setGameType(int gameType) {
		this.gameType = gameType;
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
	 * @return the player1Turn
	 */
	public boolean isPlayer1Turn() {
		return player1Turn;
	}

	/**
	 * @param player1Turn the player1Turn to set
	 */
	public void setPlayer1Turn(boolean player1Turn) {
		this.player1Turn = player1Turn;
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

	/**
	 * @return the board
	 */
	public String getBoard() {
		return board;
	}

	/**
	 * @param board the board to set
	 */
	public void setBoard(String board) {
		this.board = board;
	}

	public int getPlayer1Score() {
		return player1Score;
	}

	public void setPlayer1Score(int player1Score) {
		this.player1Score = player1Score;
	}

	public int getPlayer2Score() {
		return player2Score;
	}

	public void setPlayer2Score(int player2Score) {
		this.player2Score = player2Score;
	}

	public int getWinner() {
		return winner;
	}

	public void setWinner(int winner) {
		this.winner = winner;
	}

	public int getPenultMove() {
		return penultMove;
	}

	public void setPenultMove(int penultMove) {
		this.penultMove = penultMove;
	}
}
