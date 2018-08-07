package com.nfehs.librarygames.net.packets;

import com.nfehs.librarygames.net.GameClient;
import com.nfehs.librarygames.net.GameServer;

public class Packet00Login extends Packet {
	
	private String username;
	private String password;
	
	public Packet00Login(byte[] data) {
		super(00);
		String[] userpass = readData(data).split(":");
		this.username = userpass[0];
		this.password = userpass[1];
	}
	
	public Packet00Login(String username, String password) {
		super(00);
		this.username = username;
		this.password = password;
	}

	@Override
	public void writeData(GameClient client) {
		client.sendData(getData());
	}

	@Override
	public void writeData(GameServer server) {
		
	}

	@Override
	public byte[] getData() {
		return ("00" + this.username + ":" + this.password).getBytes();
	}
}
