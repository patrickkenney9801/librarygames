package com.nfehs.librarygames.games;

import com.nfehs.librarygames.net.Security;

/**
 * This is the parent class for board games
 * @author Patrick Kenney and Syed Quadri
 * @date 8/10/2018
 */

public abstract class BoardGame {
	public static enum GameTypes {
		INVALID(-1), GO(00);
		
		private int gameType;
		private GameTypes(int gameType) {
			this.gameType = gameType;
		}
		
		public int getType() {
			return gameType;
		}
	}
	
	private byte gameType;
	private String gameTitle;
	private String gameName;
	private String player1;
	private String player2;
	private int gameId;
	
	private String gameKey;
	
	/**
	 * @param gameType
	 * @param player1
	 * @param player2
	 * @param gameId
	 */
	public BoardGame(int gameType, String gameKey, String player1, String player2, int gameId) {
		this.gameType = (byte) gameType;
		setGameKey(gameKey);
		setGameName(lookupGameName(gameType));
		setPlayer1(Security.decrypt(player1));
		setPlayer2(Security.decrypt(player2));
		setGameId(gameId);
		
		setGameTitle(getGameId() + "\t" + getGameName() + ": " + getPlayer1() + " VS. " + getPlayer2());
	}
	
	/**
	 * Returns the name of the game as a string given its integer type
	 * @param gameType
	 * @return
	 */
	public String lookupGameName(int gameType) {
		return lookupGameName(lookupGame(gameType));
	}
	
	/**
	 * Returns the name of the game given its enum GameTypes
	 * @param lookupGame
	 * @return
	 */
	private String lookupGameName(GameTypes type) {
		switch (type) {
			case INVALID:				return "";
			case GO:					return "Go";
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
	 * @return the gameId
	 */
	public int getGameId() {
		return gameId;
	}

	/**
	 * @param gameId the gameId to set
	 */
	public void setGameId(int gameId) {
		this.gameId = gameId;
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
}
