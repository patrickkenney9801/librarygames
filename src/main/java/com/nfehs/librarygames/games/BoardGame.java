package com.nfehs.librarygames.games;

import com.nfehs.librarygames.Game;
import com.nfehs.librarygames.net.Security;

/**
 * This is the parent class for board games
 * @author Patrick Kenney and Syed Quadri
 * @date 8/10/2018
 */

public abstract class BoardGame {
	public static enum GameTypes {
		INVALID(-1), GO9x9(00), GO13x13(01), GO19x19(02);
		
		private int gameType;
		private GameTypes(int gameType) {
			this.gameType = gameType;
		}
		
		public int getType() {
			return gameType;
		}
	}
	
	// Universal use
	private byte gameType;
	private String gameTitle;
	private String gameName;
	private String player1;
	private String player2;
	private boolean isPlayerTurn;
	private String gameKey;
	
	// For use only when on GameScreen
	private char[][] board;
	protected Tile[][] tiles;
	protected Piece[][] pieces;
	private int lastMove;
	
	/**
	 * Sets basic board game information
	 * @param gameKey
	 * @param gameType
	 * @param player1
	 * @param player2
	 * @param player1Turn
	 * @param lastMove
	 * @param board
	 */
	public BoardGame(String gameKey, int gameType, String player1, String player2, boolean player1Turn,
			int lastMove, String board) {
		setGameKey(gameKey);
		this.gameType = (byte) gameType;
		setGameName(lookupGameName(this.gameType));
		setPlayer1(Security.decrypt(player1));
		setPlayer2(Security.decrypt(player2));
		setGameTitle(getGameName() + ":        " + getPlayer1() + "  vs.  " + getPlayer2());
		setLastMove(lastMove);
		setBoard(board);
		setTiles();
		setPieces();
		
		// determine whether it is the logged players turn or not
		setPlayerTurn(false);
		if ((getPlayer1().equals(Game.getPlayer().getUsername()) && player1Turn) 
				|| (getPlayer2().equals(Game.getPlayer().getUsername()) && !player1Turn))
			setPlayerTurn(true);
	}

	// implement in child classes, for use on GameScreen
	protected abstract void setTiles();
	protected abstract void setPieces();
	public abstract void handleMouseEnterTile(int[] coordinates);
	public abstract void handleMouseLeaveTile(int[] coordinates);
	public abstract void handleMouseClickTile(int[] coordinates);
	
	// implement in child classes, for use in logic TODO add more logic methods
	protected abstract boolean validMove(int x, int y);

	/**
	 * Returns a String with 3 parts delimited by ~
	 * Part 1 is the game key, 2 is game title, 3 is bool for user goes first
	 * @param gameInfo
	 * @return
	 */
	public static String getGameInfo(String[] gameInfo) {
		String gameInformation = gameInfo[0] + "~";
		byte gameType = Byte.parseByte(gameInfo[1]);
		gameInformation += lookupGameName(gameType) + ":        ";
		gameInformation += Security.decrypt(gameInfo[2]) + "  vs.  ";
		gameInformation += Security.decrypt(gameInfo[3]) + "~";
		
		// determine whether it is the logged players turn or not
		boolean playerTurn = false;
		if ((Security.decrypt(gameInfo[2]).equals(Game.getPlayer().getUsername()) && gameInfo[4].charAt(0) == '1') 
				|| (Security.decrypt(gameInfo[3]).equals(Game.getPlayer().getUsername()) && gameInfo[4].charAt(0) != '1'))
			playerTurn = true;
		gameInformation += playerTurn;
		return gameInformation;
	}
	
	/**
	 * Returns the name of the game as a string given its integer type
	 * @param gameType
	 * @return
	 */
	public static String lookupGameName(int gameType) {
		return lookupGameName(lookupGame(gameType));
	}
	
	/**
	 * Returns the name of the game given its enum GameTypes
	 * @param lookupGame
	 * @return
	 */
	private static String lookupGameName(GameTypes type) {
		switch (type) {
			case INVALID:				return "";
			case GO9x9:					return "Go 9x9";
			case GO13x13:				return "Go 13x13";
			case GO19x19:				return "Go 19x19";
		}
		return "";
	}

	/**
	 * This method returns what type of game the game is
	 * @param id String
	 * @return
	 */
	public static GameTypes lookupGame(String type) {
		try {
			return lookupGame(Integer.parseInt(type));
		} catch (Exception e) {
			return GameTypes.INVALID;
		}
	}
	
	/**
	 * This method returns what type of game the game is
	 * @param id int
	 * @return
	 */
	public static GameTypes lookupGame(int type) {
		for (GameTypes p : GameTypes.values()) {
			if (p.getType() == type)
				return p;
		}
		return GameTypes.INVALID;
	}

	/**
	 * @return the gameTitle
	 */
	public String getGameTitle() {
		return gameTitle;
	}

	/**
	 * @param gameTitle the gameTitle to set
	 */
	public void setGameTitle(String gameTitle) {
		this.gameTitle = gameTitle;
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
	 * @return the gameName
	 */
	public String getGameName() {
		return gameName;
	}

	/**
	 * @param gameName the gameName to set
	 */
	public void setGameName(String gameName) {
		this.gameName = gameName;
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
	 * @return the isPlayerTurn
	 */
	public boolean isPlayerTurn() {
		return isPlayerTurn;
	}

	/**
	 * @param isPlayerTurn the isPlayerTurn to set
	 */
	public void setPlayerTurn(boolean isPlayerTurn) {
		this.isPlayerTurn = isPlayerTurn;
	}

	/**
	 * @return the board
	 */
	public char[][] getBoard() {
		return board;
	}

	/**
	 * @param board the board to set
	 */
	public void setBoard(String board) {
		// get length of sides
		int arrayLength = (int) Math.sqrt(board.length());
		
		// build 2D board
		char[][] board2D = new char[arrayLength][arrayLength];
		for (int i = 0; i < board2D.length; i++)
			board2D[i] = board.substring(i*arrayLength, (i+1)*arrayLength).toCharArray();
		this.board = board2D;
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

	public Tile[][] getTiles() {
		return tiles;
	}

	public Piece[][] getPieces() {
		return pieces;
	}
}
