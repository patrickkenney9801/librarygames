package com.nfehs.librarygames.net.packets;

/**
 * Packets pertaining to creating accounts
 * @author Patrick Kenney and Syed Quadri
 * @date 8/11/2018
 */

public class Packet01CreateAcc extends Packet {
	// data to send to server
	private String email;
	private String username;
	private String password;
	
	// data to send to client
	// username
	private String userKey;

	/**
	 * Used by client to send data to server
	 * @param email
	 * @param username
	 * @param password
	 */
	public Packet01CreateAcc(String email, String username, String password) {
		super(01);
		setEmail(email);
		setUsername(username);
		setPassword(password);
	}
	
	/**
	 * Used by server to retrieve data from client's data packet
	 * @param data
	 */
	public Packet01CreateAcc(byte[] data) {
		super(01);
		
		try {
			String[] userpass = readData(data).split(":");
			setUuidKey(userpass[0]);
			setEmail(userpass[1]);
			setUsername(userpass[2]);
			setPassword(userpass[3]);
		} catch (Exception e) {
			e.printStackTrace();
			setValid(false);
		}
	}
	
	/**
	 * Used by server to send data to client
	 * @param packetKey
	 * @param username
	 * @param userKey
	 * @param serverUse boolean that serves no purpose other than to distinguish constructors
	 */
	public Packet01CreateAcc(String packetKey, String username, String userKey, boolean serverUse) {
		super(01);
		setUuidKey(packetKey);
		setUsername(username);
		setUserKey(userKey);
	}
	
	/**
	 * Used by server to retrieve data from client's data packet
	 * @param data
	 * @param serverUse boolean that serves no purpose other than to distinguish constructors
	 */
	public Packet01CreateAcc(byte[] data, boolean serverUse) {
		super(01);
		
		try {
			String[] userdata = readData(data).split(":");
			setUuidKey(userdata[0]);
			setUsername(userdata[1]);
			setUserKey(userdata[2]);
		} catch (Exception e) {
			e.printStackTrace();
			setValid(false);
		}
	}

	@Override
	public byte[] getData() {
		return ("01" + getUuidKey() + ":" + getEmail() + ":" + getUsername() + ":" + getPassword()).getBytes();
	}

	@Override
	public byte[] getDataServer() {
		return ("01" + getUuidKey() + ":" + getUsername() + ":" + getUserKey()).getBytes();
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
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
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
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
}
