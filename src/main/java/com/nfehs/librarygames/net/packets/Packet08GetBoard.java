package com.nfehs.librarygames.net.packets;

/**
 * Retrieves the data for a game
 * @author Patrick Kenney and Syed Quadri
 * @date 8/14/2018
 */

public class Packet08GetBoard extends Packet {
	// data to send to server
	private String username;
	private String gameKey;
	private int gameType;
	
	// data to send to client
	// gameKey
	// gameType;
	private String player1;
	private String player2;
	private int moves;
	private int penultMove;
	private int lastMove;
	private int winner;
	private boolean player1OnGame;
	private boolean player2OnGame;
	private String board;
	private String extraData;

	/**
	 * Used by client to send data to server
	 * @param senderKey
	 * @param username
	 * @param gameKey
	 * @param gameType
	 */
	public Packet08GetBoard(String senderKey, String username, String gameKey, int gameType) {
		super(8, senderKey);
		setUsername(username);
		setGameKey(gameKey);
		setGameType(gameType);
	}
	
	/**
	 * Used by server to retrieve data from client's packet
	 * @param data
	 */
	public Packet08GetBoard(byte[] data) {
		super(8);
		
		try {
			String[] userdata = readData(data).split(":");
			setUuidKey(userdata[0]);
			setSenderKey(userdata[1]);
			setUsername(userdata[2]);
			setGameKey(userdata[3]);
			setGameType(Integer.parseInt(userdata[4]));
		} catch (Exception e) {
			e.printStackTrace();
			setValid(false);
		}
	}
	
	/**
	 * Used by server to send data to client
	 * @param packetKey
	 * @param senderKey
	 * @param gameKey
	 * @param gameType
	 * @param player1
	 * @param player2
	 * @param moves
	 * @param penultMove
	 * @param lastMove
	 * @param winner
	 * @param player1OnGame
	 * @param player2OnGame
	 * @param board
	 * @param extraData
	 * @param serverUse boolean that serves no purpose other than to distinguish constructors
	 */
	public Packet08GetBoard(String packetKey, String senderKey, String gameKey, int gameType, 
							String player1, String player2, int moves, int penultMove, int lastMove, int winner,
							boolean player1OnGame, boolean player2OnGame, String board, String extraData, boolean serverUse) {
		super(8);
		setUuidKey(packetKey);
		setSenderKey(senderKey);
		setGameKey(gameKey);
		setGameType(gameType);
		setPlayer1(player1);
		setPlayer2(player2);
		setMoves(moves);
		setPenultMove(penultMove);
		setLastMove(lastMove);
		setWinner(winner);
		setPlayer1OnGame(player1OnGame);
		setPlayer2OnGame(player2OnGame);
		setBoard(board);
		setExtraData(extraData);
	}
	
	/**
	 * Used by server to retrieve data from client's data packet
	 * @param data
	 * @param serverUse boolean that serves no purpose other than to distinguish constructors
	 */
	public Packet08GetBoard(byte[] data, boolean serverUse) {
		super(8);
		
		try {
			String[] userdata = readData(data).split(":");
			setUuidKey(userdata[0]);
			setSenderKey(userdata[1]);
			setGameKey(userdata[2]);
			setPlayer1(userdata[4]);
			setPlayer2(userdata[5]);
			setBoard(userdata[12]);
			setExtraData(userdata[13]);
			setGameType(Integer.parseInt(userdata[3]));
			setMoves(Integer.parseInt(userdata[6]));
			setPenultMove(Integer.parseInt(userdata[7]));
			setLastMove(Integer.parseInt(userdata[8]));
			setWinner(Integer.parseInt(userdata[9]));
			setPlayer1OnGame(Boolean.parseBoolean(userdata[10]));
			setPlayer2OnGame(Boolean.parseBoolean(userdata[11]));
		} catch (Exception e) {
			e.printStackTrace();
			setValid(false);
		}
	}

	@Override
	public byte[] getData() {
		return ("08" + getUuidKey() + ":" + getSenderKey() + ":" + getUsername() + ":" + getGameKey() + ":" + getGameType()).getBytes();
	}

	@Override
	public byte[] getDataServer() {
		return ("08" + getUuidKey() + ":" + getSenderKey() + ":" + getGameKey() + ":" + getGameType()
		 		+ ":" + getPlayer1() + ":" + getPlayer2() + ":" + getMoves() + ":" + getPenultMove() + ":" + getLastMove()
		 		+ ":" + getWinner() + ":" + isPlayer1OnGame() + ":" + isPlayer2OnGame() + ":" + getBoard() + ":" + getExtraData()).getBytes();
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

	/**
	 * @return the moves
	 */
	public int getMoves() {
		return moves;
	}

	/**
	 * @param moves the moves to set
	 */
	public void setMoves(int moves) {
		this.moves = moves;
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

	public boolean isPlayer1OnGame() {
		return player1OnGame;
	}

	public void setPlayer1OnGame(boolean player1OnGame) {
		this.player1OnGame = player1OnGame;
	}

	public boolean isPlayer2OnGame() {
		return player2OnGame;
	}

	public void setPlayer2OnGame(boolean player2OnGame) {
		this.player2OnGame = player2OnGame;
	}
}
