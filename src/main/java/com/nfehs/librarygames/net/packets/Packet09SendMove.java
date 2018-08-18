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
	private int gameType;
	
	// data to send to client
	// userKey
	// gameKey
	private int penultMove;
	private int lastMove;
	private String board;
	private int winner;
	private String extraData;

	/**
	 * Used by client to send data to server
	 * @param userkey
	 * @param gameKey
	 * @param moveFrom
	 * @param moveTo
	 * @param gameType
	 */
	public Packet09SendMove(String userkey, String gameKey, int moveFrom, int moveTo, String username, int gameType) {
		super(9);
		setUserKey(userkey);
		setGameKey(gameKey);
		setMoveFrom(moveFrom);
		setMoveTo(moveTo);
		setUsername(username);
		setGameType(gameType);
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
			setGameType(Integer.parseInt(userdata[6]));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Used by server to send data to client
	 * @param packetKey
	 * @param userKey
	 * @param gameKey
	 * @param penultMove
	 * @param lastMove
	 * @param winner
	 * @param board
	 * @param extraData
	 * @param serverUse boolean that serves no purpose other than to distinguish constructors
	 */
	public Packet09SendMove(String packetKey, String userKey, String gameKey, int penultMove, int lastMove,
							int winner, String board, String extraData, boolean serverUse) {
		super(9);
		setUuidKey(packetKey);
		setUserKey(userKey);
		setGameKey(gameKey);
		setPenultMove(penultMove);
		setLastMove(lastMove);
		setWinner(winner);
		setBoard(board);
		setExtraData(extraData);
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
		setExtraData(userdata[7]);
		try {
			setPenultMove(Integer.parseInt(userdata[3]));
			setLastMove(Integer.parseInt(userdata[4]));
			setWinner(Integer.parseInt(userdata[5]));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	@Override
	public byte[] getData() {
		return ("09" + getUuidKey() + ":" + getUserKey() + ":" + getGameKey() + ":"
				+ getMoveFrom() + ":" + getMoveTo() + ":" + getUsername() + ":" + getGameType()).getBytes();
	}
	
	@Override
	public byte[] getDataServer() {
		return ("09" + getUuidKey() + ":" + getUserKey() + ":" + getGameKey() + ":" + getPenultMove() + ":" + getLastMove()
		 		+ ":" + getWinner() + ":" + getBoard() + ":" + getExtraData()).getBytes();
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

	public int getPenultMove() {
		return penultMove;
	}

	public void setPenultMove(int penultMove) {
		this.penultMove = penultMove;
	}

	/**
	 * @return the winner
	 */
	public int getWinner() {
		return winner;
	}

	/**
	 * @param winner the winner to set
	 */
	public void setWinner(int winner) {
		this.winner = winner;
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
	 * @return the extraData
	 */
	public String getExtraData() {
		return extraData;
	}

	/**
	 * @param extraData the extraData to set
	 */
	public void setExtraData(String extraData) {
		this.extraData = extraData;
	}
}
