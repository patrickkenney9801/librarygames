package com.nfehs.librarygames.net.packets;

/**
 * Handles sending moves to the server and receiving basic response
 * @author Patrick Kenney and Syed Quadri
 * @date 8/16/2018
 */

public class Packet09SendMove extends Packet {
	// data to send to server
	private String gameKey;
	private int moveFrom;
	private int moveTo;
	private String username;
	private int gameType;
	
	// data to send to client
	// gameKey
	private int penultMove;
	private int lastMove;
	private String board;
	private int winner;
	private boolean opponentOnGame;
	private String extraData;

	/**
	 * Used by client to send data to server
	 * @param senderKey
	 * @param gameKey
	 * @param moveFrom
	 * @param moveTo
	 * @param username
	 * @param gameType
	 */
	public Packet09SendMove(String senderKey, String gameKey, int moveFrom, int moveTo, String username, int gameType) {
		super(9, senderKey);
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
		
		try {
			String[] userdata = readData(data).split(":");
			setUuidKey(userdata[0]);
			setSenderKey(userdata[1]);
			setGameKey(userdata[2]);
			setUsername(userdata[5]);
			setMoveFrom(Integer.parseInt(userdata[3]));
			setMoveTo(Integer.parseInt(userdata[4]));
			setGameType(Integer.parseInt(userdata[6]));
		} catch (Exception e) {
			e.printStackTrace();
			setValid(false);
		}
	}
	
	/**
	 * Used by server to send data to client
	 * @param packetKey
	 * @param gameKey
	 * @param penultMove
	 * @param lastMove
	 * @param winner
	 * @param opponentOnGame
	 * @param board
	 * @param extraData
	 * @param serverUse boolean that serves no purpose other than to distinguish constructors
	 */
	public Packet09SendMove(String packetKey, String gameKey, int penultMove, int lastMove, int winner, 
							boolean opponentOnGame, String board, String extraData, boolean serverUse) {
		super(9);
		setUuidKey(packetKey);
		setGameKey(gameKey);
		setPenultMove(penultMove);
		setLastMove(lastMove);
		setWinner(winner);
		setOpponentOnGame(opponentOnGame);
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
		
		try {
			String[] userdata = readData(data).split(":");
			setUuidKey(userdata[0]);
			setGameKey(userdata[1]);
			setBoard(userdata[6]);
			setExtraData(userdata[7]);
			setPenultMove(Integer.parseInt(userdata[2]));
			setLastMove(Integer.parseInt(userdata[3]));
			setWinner(Integer.parseInt(userdata[4]));
			setOpponentOnGame(Boolean.parseBoolean(userdata[5]));
		} catch (Exception e) {
			e.printStackTrace();
			setValid(false);
		}
	}

	@Override
	public byte[] getData() {
		return ("09" + getUuidKey() + ":" + getSenderKey() + ":" + getGameKey() + ":"
				+ getMoveFrom() + ":" + getMoveTo() + ":" + getUsername() + ":" + getGameType()).getBytes();
	}
	
	@Override
	public byte[] getDataServer() {
		return ("09" + getUuidKey() + ":" + getGameKey() + ":" + getPenultMove() + ":" + getLastMove()
		 		+ ":" + getWinner() + ":" + isOpponentOnGame() + ":" + getBoard() + ":" + getExtraData()).getBytes();
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

	/**
	 * @return the opponentOnGame
	 */
	public boolean isOpponentOnGame() {
		return opponentOnGame;
	}

	/**
	 * @param opponentOnGame the opponentOnGame to set
	 */
	public void setOpponentOnGame(boolean opponentOnGame) {
		this.opponentOnGame = opponentOnGame;
	}
}
