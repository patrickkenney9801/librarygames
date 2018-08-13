package com.nfehs.librarygames.net.packets;

/**
 * Packets pertaining to CreateGameScreen, adds a friend to player
 * Server return packet is sent via Packet04GetPlayers, not here
 * @author Patrick Kenney and Syed Quadri
 * @date 8/12/2018
 */

public class Packet05AddFriend extends Packet {
	// data to send to server
	private String userKey;
	private String friendName;

	/**
	 * Used by client to send data to server
	 * @param userkey
	 * @param friendName
	 */
	public Packet05AddFriend(String userkey, String friendName) {
		super(05);
		setUserKey(userkey);
		setFriendName(friendName);
	}
	
	/**
	 * Used by server to retrieve data from client's packet
	 * @param data
	 */
	public Packet05AddFriend(byte[] data) {
		super(05);
		String[] userdata = readData(data).split(":");
		setUuidKey(userdata[0]);
		setUserKey(userdata[1]);
		setFriendName(userdata[1]);
	}

	@Override
	public byte[] getData() {
		return ("05" + getUuidKey() + ":" + getUserKey() + ":" + getFriendName()).getBytes();
	}

	@Override
	public byte[] getDataServer() {
		return ("05" + getUuidKey()).getBytes();
	}

	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}

	public String getFriendName() {
		return friendName;
	}

	public void setFriendName(String friendName) {
		this.friendName = friendName;
	}
}
