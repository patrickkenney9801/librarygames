package com.nfehs.librarygames.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.nfehs.librarygames.Game;
import com.nfehs.librarygames.GameFrame;
import com.nfehs.librarygames.Player;
import com.nfehs.librarygames.net.packets.Packet;
import com.nfehs.librarygames.net.packets.Packet00Login;
import com.nfehs.librarygames.net.packets.Packet01CreateAcc;
import com.nfehs.librarygames.screens.CreateAccountScreen;

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
			case INVALID:				break;
			case LOGIN:					loginUser(packet.getData());
										break;
			case CREATEACCOUNT:			createAccountLogin(packet.getData());
										break;
			default:					break;
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

	// TODO use uuid keys for packages to keep things organized
	
	/**
	 * Handles a user successfully logging to server
	 * @param data
	 */
	private void loginUser(byte[] data) {
		System.out.println("REA" + Game.gameState);
		// verify that user is still on the login screen, if not exit
		if (Game.gameState != Game.LOGIN)
			return;
		Packet00Login packet = new Packet00Login(data, true);
		
		// create player from packet
		Game.setPlayer(new Player(Security.decrypt(packet.getUsername()), packet.getUserKey()));
		
		// update GameFrame data
		GameFrame.loggedUser.setText("Logged in as: " + Security.decrypt(packet.getUsername()));
		
		// open active games screen
		Game.openActiveGamesScreen();
	}

	/**
	 * Handles a user successfully creating an account and then logging into server
	 * @param data
	 */
	private void createAccountLogin(byte[] data) {
		// verify that user is still on the create account screen, if not exit
		if (Game.gameState != Game.CREATE_ACCOUNT)
			return;
		
		Packet01CreateAcc packet = new Packet01CreateAcc(data, true);
		
		// create player from packet
		Game.setPlayer(new Player(Security.decrypt(packet.getUsername()), packet.getUserKey()));
		
		// update GameFrame data
		GameFrame.loggedUser.setText("Logged in as: " + Security.decrypt(packet.getUsername()));
		
		// open active games screen
		Game.openActiveGamesScreen();
	}
}
