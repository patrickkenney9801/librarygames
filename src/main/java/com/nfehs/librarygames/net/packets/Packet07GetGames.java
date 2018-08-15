package com.nfehs.librarygames.net.packets;

/**
 * Packets pertaining to ActiveGamesScreen, gets active games
 * @author Patrick Kenney and Syed Quadri
 * @date 8/14/2018
 */

public class Packet07GetGames extends Packet {
	// data to send to server
	private String userKey;
	private String username;
	
	// data to send to client
	// userKey
	private String[] gameInfo;

	/**
	 * Used by client to send data to server
	 * @param userkey
	 */
	public Packet07GetGames(String userkey, String username) {
		super(07);
		setUserKey(userkey);
		setUsername(username);
	}
	
	/**
	 * Used by server to retrieve data from client's packet
	 * @param data
	 */
	public Packet07GetGames(byte[] data) {
		super(07);
		String[] userdata = readData(data).split(":");
		setUuidKey(userdata[0]);
		setUserKey(userdata[1]);
		setUsername(userdata[2]);
	}
	
	/**
	 * 
	 * Used by server to send data to client
	 * @param packetKey
	 * @param userKey
	 * @param gameKey
	 * @param gameType
	 * @param player1
	 * @param player2
	 * @param player1Turn
	 * @param serverUse boolean that serves no purpose other than to distinguish constructors
	 */
	public Packet07GetGames(String packetKey, String userKey, String[] gameInfo, boolean serverUse) {
		super(07);
		setUuidKey(packetKey);
		setUserKey(userKey);
		setGameInfo(gameInfo);
	}
	
	/**
	 * Used by server to retrieve data from client's data packet
	 * @param data
	 * @param serverUse boolean that serves no purpose other than to distinguish constructors
	 */
	public Packet07GetGames(byte[] data, boolean serverUse) {
		super(07);
		String[] userdata = readData(data).split(":");
		setUuidKey(userdata[0]);
		setUserKey(userdata[1]);
		
		String[] gameInfo = new String[userdata.length-2];
		for (int i = 2; i < userdata.length; i++)
			gameInfo[i-2] = userdata[i];
		setGameInfo(gameInfo);
	}

	@Override
	public byte[] getData() {
		return ("07" + getUuidKey() + ":" + getUserKey() + ":" + getUsername()).getBytes();
	}

	@Override
	public byte[] getDataServer() {
		String data = "07" + getUuidKey() + ":" + getUserKey();
		for (String info : getGameInfo())
			data += ":" + info;
		return (data).getBytes();
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
	 * @return the gameInfo
	 */
	public String[] getGameInfo() {
		return gameInfo;
	}

	/**
	 * @param gameInfo the gameInfo to set
	 */
	public void setGameInfo(String[] gameInfo) {
		this.gameInfo = gameInfo;
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
