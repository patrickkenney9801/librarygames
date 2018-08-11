package com.nfehs.librarygames.net.packets;

/**
 * Packets pertaining to login
 * @author Patrick Kenney and Syed Quadri
 * @date 8/11/2018
 */

public class Packet00Login extends Packet {
	// data to send to server
	private String username;
	private String password;
	
	public Packet00Login(byte[] data) {
		super(00);
		String[] userpass = readData(data).split(":");
		setUuidKey(userpass[0]);
		setUsername(userpass[1]);
		setPassword(userpass[2]);
	}
	
	public Packet00Login(String username, String password) {
		super(00);
		setUsername(username);
		setPassword(password);
	}

	@Override
	public byte[] getData() {
		return ("00" + getUuidKey() + ":" + getUsername() + ":" + getPassword()).getBytes();
	}

	@Override
	public byte[] getDataServer() {
		// TODO Auto-generated method stub
		return null;
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
}
