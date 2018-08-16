package com.nfehs.librarygames.net.packets;

/**
 * Packets pertaining to creating games, sends game key back from server
 * @author Patrick Kenney and Syed Quadri
 * @date 8/14/2018
 */

public class Packet06CreateGame extends Packet {
	// data to send to server
	private String userKey;
	private String username;
	private String otherUser;
	private boolean creatorGoesFirst;
	private int gameType;
	
	// data to send to client from server
	private String gameKey;

	/**
	 * Used by client to send data to server
	 * @param userKey
	 * @param username
	 * @param otherUser
	 * @param creatorGoesFirst
	 * @param gameType
	 */
	public Packet06CreateGame(String userKey, String username, String otherUser, boolean creatorGoesFirst, int gameType) {
		super(06);
		setUserKey(userKey);
		setUsername(username);
		setOtherUser(otherUser);
		setCreatorGoesFirst(creatorGoesFirst);
		setGameType(gameType);
	}
	
	/**
	 * Used by server to retrieve data from client's data packet
	 * @param data
	 */
	public Packet06CreateGame(byte[] data) {
		super(06);
		String[] userpass = readData(data).split(":");
		setUuidKey(userpass[0]);
		setUserKey(userpass[1]);
		setUsername(userpass[2]);
		setOtherUser(userpass[3]);
		setCreatorGoesFirst(Boolean.parseBoolean(userpass[4]));
		try {
			setGameType(Integer.parseInt(userpass[5]));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Used by server to send data to client
	 * @param packetKey
	 * @param gameKey
	 * @param serverUse boolean that serves no purpose other than to distinguish constructors
	 */
	public Packet06CreateGame(String packetKey, String userKey, String gameKey, boolean serverUse) {
		super(06);
		setUuidKey(packetKey);
		setUserKey(userKey);
		setGameKey(gameKey);
	}
	
	/**
	 * Used by server to retrieve data from client's data packet
	 * @param data
	 * @param serverUse boolean that serves no purpose other than to distinguish constructors
	 */
	public Packet06CreateGame(byte[] data, boolean serverUse) {
		super(06);
		String[] userdata = readData(data).split(":");
		setUuidKey(userdata[0]);
		setUserKey(userdata[1]);
		setGameKey(userdata[2]);
	}

	@Override
	public byte[] getData() {
		return ("06" + getUuidKey() + ":" + getUserKey() + ":" + getUsername() + ":" + getOtherUser()
				+ ":" + getCreatorGoesFirst() + ":" + getGameType()).getBytes();
	}

	@Override
	public byte[] getDataServer() {
		return ("06" + getUuidKey() + ":" + getUserKey() + ":" + getGameKey()).getBytes();
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
	 * @return the otherUser
	 */
	public String getOtherUser() {
		return otherUser;
	}

	/**
	 * @param otherUser the otherUser to set
	 */
	public void setOtherUser(String otherUser) {
		this.otherUser = otherUser;
	}

	/**
	 * @return the creatorGoesFirst
	 */
	public boolean getCreatorGoesFirst() {
		return creatorGoesFirst;
	}

	/**
	 * @param creatorGoesFirst the creatorGoesFirst to set
	 */
	public void setCreatorGoesFirst(boolean creatorGoesFirst) {
		this.creatorGoesFirst = creatorGoesFirst;
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
