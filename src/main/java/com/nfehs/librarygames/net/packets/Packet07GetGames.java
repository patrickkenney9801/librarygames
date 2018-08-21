package com.nfehs.librarygames.net.packets;

/**
 * Packets pertaining to ActiveGamesScreen, gets active games
 * @author Patrick Kenney and Syed Quadri
 * @date 8/14/2018
 */

public class Packet07GetGames extends Packet {
	// data to send to server
	private String username;
	
	// data to send to client
	private int packetsSent;
	private int packetNumber;
	private String[] gameInfo;

	/**
	 * Used by client to send data to server
	 * @param senderKey
	 * @param userkey
	 * @param username
	 */
	public Packet07GetGames(String senderKey, String username) {
		super(07, senderKey);
		setUsername(username);
	}
	
	/**
	 * Used by server to retrieve data from client's packet
	 * @param data
	 */
	public Packet07GetGames(byte[] data) {
		super(07);
		
		try {
			String[] userdata = readData(data).split(":");
			setUuidKey(userdata[0]);
			setSenderKey(userdata[1]);
			setUsername(userdata[2]);
		} catch (Exception e) {
			e.printStackTrace();
			setValid(false);
		}
	}
	
	/**
	 * 
	 * Used by server to send data to client
	 * @param packetKey
	 * @param packetsSent
	 * @param packetNumber
	 * @param gameInfo
	 * @param serverUse boolean that serves no purpose other than to distinguish constructors
	 */
	public Packet07GetGames(String packetKey, int packetsSent, int packetNumber, String[] gameInfo, boolean serverUse) {
		super(07);
		setUuidKey(packetKey);
		setPacketsSent(packetsSent);
		setPacketNumber(packetNumber);
		setGameInfo(gameInfo);
	}
	
	/**
	 * Used by server to retrieve data from client's data packet
	 * @param data
	 * @param serverUse boolean that serves no purpose other than to distinguish constructors
	 */
	public Packet07GetGames(byte[] data, boolean serverUse) {
		super(07);
		
		try {
			String[] userdata = readData(data).split(":");
			setUuidKey(userdata[0]);
			setPacketsSent(Integer.parseInt(userdata[1]));
			setPacketNumber(Integer.parseInt(userdata[2]));
			
			String[] gameInfo = new String[userdata.length-3];
			for (int i = 3; i < userdata.length; i++)
				gameInfo[i-3] = userdata[i];
			setGameInfo(gameInfo);
		} catch (Exception e) {
			e.printStackTrace();
			setValid(false);
		}
	}

	@Override
	public byte[] getData() {
		return ("07" + getUuidKey() + ":" + getSenderKey() + ":" + getUsername()).getBytes();
	}

	@Override
	public byte[] getDataServer() {
		String data = "07" + getUuidKey() + ":" + getPacketsSent() + ":" + getPacketNumber();
		for (String info : getGameInfo())
			if (info != null)
				data += ":" + info;
		return (data).getBytes();
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

	/**
	 * @return the packetsSent
	 */
	public int getPacketsSent() {
		return packetsSent;
	}

	/**
	 * @param packetsSent the packetsSent to set
	 */
	public void setPacketsSent(int packetsSent) {
		this.packetsSent = packetsSent;
	}

	/**
	 * @return the packetNumber
	 */
	public int getPacketNumber() {
		return packetNumber;
	}

	/**
	 * @param packetNumber the packetNumber to set
	 */
	public void setPacketNumber(int packetNumber) {
		this.packetNumber = packetNumber;
	}
}
