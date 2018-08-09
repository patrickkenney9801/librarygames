package com.nfehs.librarygames.net.packets;

import com.nfehs.librarygames.net.GameClient;
import com.nfehs.librarygames.net.GameServer;

public class Packet01CreateAcc extends Packet {

	private String email;
	private String username;
	private String password;
	
	public Packet01CreateAcc(byte[] data) {
		super(01);
		String[] userpass = readData(data).split(":");
		setEmail(userpass[0]);
		setUsername(userpass[1]);
		setPassword(userpass[2]);
	}

	public Packet01CreateAcc(String email, String username, String password) {
		super(01);
		setEmail(email);
		setUsername(username);
		setPassword(password);
	}

	@Override
	public void writeData(GameClient client) {
		client.sendData(getData());
	}

	@Override
	public void writeData(GameServer server) {
		// TODO Auto-generated method stub
	}

	@Override
	public byte[] getData() {
		return ("01" + this.email + ":" + this.username + ":" + this.password).getBytes();
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
}
