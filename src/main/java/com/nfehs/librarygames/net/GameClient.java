package com.nfehs.librarygames.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.nfehs.librarygames.Game;
import com.nfehs.librarygames.net.packets.Packet;
import com.nfehs.librarygames.net.packets.Packet00Login;
import com.nfehs.librarygames.net.packets.Packet01CreateAcc;

/**
 * This class handles receiving packets from and sending to the server
 * @author Patrick Kenney, Syed Quadri
 * @date 6/13/2018
 */

public class GameClient extends Thread {

	private InetAddress ipAddress;
	private DatagramSocket socket;
	private Game game;
	public static final int PORT = 19602;
	
	public GameClient(Game game, byte[] ipAddress) {
		this.game = game;
		try {
			this.socket = new DatagramSocket();
			this.ipAddress = InetAddress.getByAddress(ipAddress);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Receives incoming packets from server
	 */
	public void run() {
		while (true) {
			byte[] data = new byte[1024];
			DatagramPacket packet = new DatagramPacket(data, data.length);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("SERVER > " + new String(packet.getData()));
			handle(packet);
		}
	}
	
	/**
	 * Determines what to do with incoming packets
	 * @param packet
	 */
	private void handle(DatagramPacket packet) {
		switch (Packet.lookupPacket(new String(packet.getData()).trim().substring(0, 2))) {
		case INVALID:
			break;
		case LOGIN:
			break;
		case CREATEACCOUNT:
			break;
		default:
			break;
		}
	}

	/**
	 * Sends data to server
	 * @param data
	 */
	public void sendData(byte[] data) {
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, PORT);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Handles a user logging into the game
	 * @param username
	 * @param password
	 */
	public void login(String username, String password) {
		new Packet00Login(username, password).writeData(this);
	}

	/**
	 * Handles a user creating a new account
	 * @param email
	 * @param username
	 * @param password
	 */
	public void createAccount(String email, String username, String password) {
		new Packet01CreateAcc(email, username, password).writeData(this);
	}
}
