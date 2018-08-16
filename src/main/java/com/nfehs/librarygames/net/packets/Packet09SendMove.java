package com.nfehs.librarygames.net.packets;

/**
 * Handles sending moves to the server and receiving basic response
 * @author Patrick Kenney and Syed Quadri
 * @date 8/16/2018
 */

public class Packet09SendMove extends Packet {
	// data to send to server
	private String userKey;
	private String gameKey;
	private int moveFrom;
	private int moveTo;
	private String username;
	
	// data to send to client
	// userKey
	// gameKey
	private int lastMove;
	private int player1Score;
	private int player2Score;
	private String board;

	/**
	 * Used by client to send data to server
	 * @param userkey
	 * @param gameKey
	 * @param moveFrom
	 * @param moveTo
	 */
	public Packet09SendMove(String userkey, String gameKey, int moveFrom, int moveTo, String username) {
		super(9);
		setUserKey(userkey);
		setGameKey(gameKey);
		setMoveFrom(moveFrom);
		setMoveTo(moveTo);
		setUsername(username);
	}
	
	/**
	 * Used by server to retrieve data from client's packet
	 * @param data
	 */
	public Packet09SendMove(byte[] data) {
		super(9);
		String[] userdata = readData(data).split(":");
		setUuidKey(userdata[0]);
		setUserKey(userdata[1]);
		setGameKey(userdata[2]);
		setUsername(userdata[5]);
		try {
			setMoveFrom(Integer.parseInt(userdata[3]));
			setMoveTo(Integer.parseInt(userdata[4]));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Used by server to send data to client
	 * @param packetKey
	 * @param userKey
	 * @param gameKey
	 * @param lastMove
	 * @param player1Score
	 * @param player2Score
	 * @param board
	 * @param serverUse boolean that serves no purpose other than to distinguish constructors
	 */
	public Packet09SendMove(String packetKey, String userKey, String gameKey, int lastMove,
							int player1Score, int player2Score, String board, boolean serverUse) {
		super(9);
		setUuidKey(packetKey);
		setUserKey(userKey);
		setGameKey(gameKey);
		setLastMove(lastMove);
		setPlayer1Score(player1Score);
		setPlayer2Score(player2Score);
		setBoard(board);
	}
	
	/**
	 * Used by server to retrieve data from client's data packet
	 * @param data
	 * @param serverUse boolean that serves no purpose other than to distinguish constructors
	 */
	public Packet09SendMove(byte[] data, boolean serverUse) {
		super(9);
		String[] userdata = readData(data).split(":");
		setUuidKey(userdata[0]);
		setUserKey(userdata[1]);
		setGameKey(userdata[2]);
		setBoard(userdata[6]);
		try {
			setLastMove(Integer.parseInt(userdata[3]));
			setPlayer1Score(Integer.parseInt(userdata[4]));
			setPlayer2Score(Integer.parseInt(userdata[5]));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	@Override
	public byte[] getData() {
		return ("09" + getUuidKey() + ":" + getUserKey() + ":" + getGameKey() + ":"
				+ getMoveFrom() + ":" + getMoveTo() + ":" + getUsername()).getBytes();
	}
	
	@Override
	public byte[] getDataServer() {
		return ("09" + getUuidKey() + ":" + getUserKey() + ":" + getGameKey() + ":" + getLastMove()
		 		+ ":" + getPlayer1Score() + ":" + getPlayer2Score() + ":" + getBoard()).getBytes();
	}

	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}

	public String getGameKey() {
		return gameKey;
	}

	public void setGameKey(String gameKey) {
		this.gameKey = gameKey;
	}

	public int getMoveFrom() {
		return moveFrom;
	}

	public void setMoveFrom(int moveFrom) {
		this.moveFrom = moveFrom;
	}

	public int getMoveTo() {
		return moveTo;
	}

	public void setMoveTo(int moveTo) {
		this.moveTo = moveTo;
	}

	public int getLastMove() {
		return lastMove;
	}

	public void setLastMove(int lastMove) {
		this.lastMove = lastMove;
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

	public String getBoard() {
		return board;
	}

	public void setBoard(String board) {
		this.board = board;
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
}
