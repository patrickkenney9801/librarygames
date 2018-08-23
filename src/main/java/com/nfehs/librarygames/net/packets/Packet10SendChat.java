package com.nfehs.librarygames.net.packets;

/**
 * Handles sending chat to the server and receiving response
 * @author Patrick Kenney and Syed Quadri
 * @date 8/17/2018
 */

public class Packet10SendChat extends Packet {
	// data to send to server
	private String gameKey;
	private String player1Username;
	private String player2Username;
	private boolean sendToSpectators;
	private String text;
	
	// data to send to client
	// same as above
	private boolean player1OnGame;
	private boolean player2OnGame;

	/**
	 * Used by client to send data to server
	 * @param senderKey
	 * @param gameKey
	 * @param player1Username
	 * @param player2Username
	 * @param sendToSpectators
	 * @param text
	 */
	public Packet10SendChat(String senderKey, String gameKey, String player1Username, String player2Username, boolean sendToSpectators, String text) {
		super(10, senderKey);
		setGameKey(gameKey);
		setPlayer1Username(player1Username);
		setPlayer2Username(player2Username);
		setSendToSpectators(sendToSpectators);
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
			setPlayer1Username(userdata[3]);
			setPlayer2Username(userdata[4]);
			setSendToSpectators(Boolean.parseBoolean(userdata[5]));
			setText(userdata[6]);
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
	 * @param player1OnGame
	 * @param player2OnGame
	 * @param text
	 * @param serverUse boolean that serves no purpose other than to distinguish constructors
	 */
	public Packet10SendChat(String packetKey, String senderKey, String gameKey,
							boolean player1OnGame, boolean player2OnGame, String text, boolean serverUse) {
		super(10);
		setUuidKey(packetKey);
		setSenderKey(senderKey);
		setGameKey(gameKey);
		setPlayer1OnGame(player1OnGame);
		setPlayer2OnGame(player2OnGame);
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
			setPlayer1OnGame(Boolean.parseBoolean(userdata[3]));
			setPlayer2OnGame(Boolean.parseBoolean(userdata[4]));
			setText(userdata[5].replace('|', ':'));
		} catch (Exception e) {
			e.printStackTrace();
			setValid(false);
		}
	}

	@Override
	public byte[] getData() {
		return ("10" + getUuidKey() + ":" + getSenderKey() + ":" + getGameKey() + ":" + getPlayer1Username() + ":" + getPlayer2Username() + ":" + isSendToSpectators() + ":" + getText()).getBytes();
	}
	
	@Override
	public byte[] getDataServer() {
		return ("10" + getUuidKey() + ":" + getSenderKey() + ":" + getGameKey() + ":" + isPlayer1OnGame() + ":" + isPlayer2OnGame() + ":" + getText()).getBytes();
	}

	public String getGameKey() {
		return gameKey;
	}

	public void setGameKey(String gameKey) {
		this.gameKey = gameKey;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isSendToSpectators() {
		return sendToSpectators;
	}

	public void setSendToSpectators(boolean sendToSpectators) {
		this.sendToSpectators = sendToSpectators;
	}

	public String getPlayer1Username() {
		return player1Username;
	}

	public void setPlayer1Username(String player1Username) {
		this.player1Username = player1Username;
	}

	public String getPlayer2Username() {
		return player2Username;
	}

	public void setPlayer2Username(String player2Username) {
		this.player2Username = player2Username;
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
