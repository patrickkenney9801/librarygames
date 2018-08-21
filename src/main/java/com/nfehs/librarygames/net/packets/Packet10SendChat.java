package com.nfehs.librarygames.net.packets;

/**
 * Handles sending chat to the server and receiving response
 * @author Patrick Kenney and Syed Quadri
 * @date 8/17/2018
 */

public class Packet10SendChat extends Packet {
	// data to send to server
	private String gameKey;
	private String opponentUsername;
	private String text;
	
	// data to send to client
	// same as above
	private boolean opponentOnGame;

	/**
	 * Used by client to send data to server
	 * @param senderKey
	 * @param gameKey
	 * @param username
	 * @param opponentUsername
	 * @param text
	 */
	public Packet10SendChat(String senderKey, String gameKey, String opponentUsername, String text) {
		super(10, senderKey);
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
		
		try {
			String[] userdata = readData(data).split(":");
			setUuidKey(userdata[0]);
			setSenderKey(userdata[1]);
			setGameKey(userdata[2]);
			setOpponentUsername(userdata[3]);
			setText(userdata[4]);
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
	 * @param opponentOnGame
	 * @param text
	 * @param serverUse boolean that serves no purpose other than to distinguish constructors
	 */
	public Packet10SendChat(String packetKey, String senderKey, String gameKey,
							boolean opponentOnGame, String text, boolean serverUse) {
		super(10);
		setUuidKey(packetKey);
		setSenderKey(senderKey);
		setGameKey(gameKey);
		setOpponentOnGame(opponentOnGame);
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
		
		try {
			String[] userdata = readData(data).split(":");
			setUuidKey(userdata[0]);
			setSenderKey(userdata[1]);
			setGameKey(userdata[2]);
			setOpponentOnGame(Boolean.parseBoolean(userdata[3]));
			setText(userdata[4].replace('|', ':'));
		} catch (Exception e) {
			e.printStackTrace();
			setValid(false);
		}
	}

	@Override
	public byte[] getData() {
		return ("10" + getUuidKey() + ":" + getSenderKey() + ":" + getGameKey() + ":" + getOpponentUsername() + ":" + getText()).getBytes();
	}
	
	@Override
	public byte[] getDataServer() {
		return ("10" + getUuidKey() + ":" + getSenderKey() + ":" + getGameKey() + ":" + isOpponentOnGame() + ":" + getText()).getBytes();
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
