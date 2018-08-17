package com.nfehs.librarygames.net.packets;

/**
 * Handles sending chat to the server and receiving response
 * @author Patrick Kenney and Syed Quadri
 * @date 8/17/2018
 */

public class Packet10SendChat extends Packet {
	// data to send to server
	private String userKey;
	private String gameKey;
	private String opponentUsername;
	private String text;
	
	// data to send to client
	// same as above

	/**
	 * Used by client to send data to server
	 * @param userkey
	 * @param gameKey
	 * @param username
	 * @param opponentUsername
	 * @param text
	 */
	public Packet10SendChat(String userkey, String gameKey, String opponentUsername, String text) {
		super(10);
		setUserKey(userkey);
		setGameKey(gameKey);
		setOpponentUsername(opponentUsername);
		text = text.replace(':', '|');
		if (text.length() > 500)
			setText(text.substring(0, 500));
		else
			setText(text);
	}
	
	/**
	 * Used by server to retrieve data from client's packet
	 * @param data
	 */
	public Packet10SendChat(byte[] data) {
		super(10);
		String[] userdata = readData(data).split(":");
		setUuidKey(userdata[0]);
		setUserKey(userdata[1]);
		setGameKey(userdata[2]);
		setOpponentUsername(userdata[3]);
		setText(userdata[4]);
	}
	
	/**
	 * Used by server to send data to client
	 * @param packetKey
	 * @param userkey
	 * @param gameKey
	 * @param text
	 * @param serverUse boolean that serves no purpose other than to distinguish constructors
	 */
	public Packet10SendChat(String packetKey, String userKey, String gameKey, String text, boolean serverUse) {
		super(10);
		setUuidKey(packetKey);
		setUserKey(userKey);
		setGameKey(gameKey);
		text = text.replace(':', '|');
		if (text.length() > 500)
			setText(text.substring(0, 500));
		else
			setText(text);
	}
	
	/**
	 * Used by server to retrieve data from client's data packet
	 * @param data
	 * @param serverUse boolean that serves no purpose other than to distinguish constructors
	 */
	public Packet10SendChat(byte[] data, boolean serverUse) {
		super(10);
		String[] userdata = readData(data).split(":");
		setUuidKey(userdata[0]);
		setUserKey(userdata[1]);
		setGameKey(userdata[2]);
		setText(userdata[3].replace('|', ':'));
	}

	@Override
	public byte[] getData() {
		return ("10" + getUuidKey() + ":" + getUserKey() + ":" + getGameKey() + ":" + getOpponentUsername() + ":" + getText()).getBytes();
	}
	
	@Override
	public byte[] getDataServer() {
		return ("10" + getUuidKey() + ":" + getUserKey() + ":" + getGameKey() + ":" + getText()).getBytes();
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

	public String getOpponentUsername() {
		return opponentUsername;
	}

	public void setOpponentUsername(String opponentUsername) {
		this.opponentUsername = opponentUsername;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
