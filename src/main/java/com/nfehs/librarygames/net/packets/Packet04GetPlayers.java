package com.nfehs.librarygames.net.packets;

/**
 * Packets pertaining to CreateGameScreen, gets players from server
 * Retrieves friends and other users separately
 * @author Patrick Kenney and Syed Quadri
 * @date 8/12/2018
 */

public class Packet04GetPlayers extends Packet {
	// data to send to server
	private String userKey;
	
	// data to send to client
	private String friends;
	private String otherPlayers;

	/**
	 * Used by client to send data to server
	 * @param userkey
	 */
	public Packet04GetPlayers(String userkey) {
		super(04);
		setUserKey(userkey);
	}
	
	/**
	 * Used by server to retrieve data from client's packet
	 * @param data
	 */
	public Packet04GetPlayers(byte[] data) {
		super(04);
		String[] userdata = readData(data).split(":");
		setUuidKey(userdata[0]);
		setUserKey(userdata[1]);
	}
	
	/**
	 * Used by server to send data to client
	 * @param packetKey
	 * @param friends
	 * @param otherPlayers
	 * @param serverUse boolean that serves no purpose other than to distinguish constructors
	 */
	public Packet04GetPlayers(String packetKey, String friends, String otherPlayers, boolean serverUse) {
		super(04);
		setUuidKey(packetKey);
		setFriends(friends);
		setOtherPlayers(otherPlayers);
	}
	
	/**
	 * Used by server to retrieve data from client's data packet
	 * @param data
	 * @param serverUse boolean that serves no purpose other than to distinguish constructors
	 */
	public Packet04GetPlayers(byte[] data, boolean serverUse) {
		super(04);
		String[] userdata = readData(data).split(":");
		setUuidKey(userdata[0]);
		setFriends(userdata[1]);
		setOtherPlayers(userdata[2]);
	}

	@Override
	public byte[] getData() {
		return ("04" + getUuidKey() + ":" + getUserKey()).getBytes();
	}

	@Override
	public byte[] getDataServer() {
		return ("04" + getUuidKey() + ":" + getFriends() + ":" + getOtherPlayers()).getBytes();
	}

	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}

	public String getFriends() {
		return friends;
	}

	public void setFriends(String friends) {
		this.friends = friends;
	}

	public String getOtherPlayers() {
		return otherPlayers;
	}

	public void setOtherPlayers(String otherPlayers) {
		this.otherPlayers = otherPlayers;
	}
}