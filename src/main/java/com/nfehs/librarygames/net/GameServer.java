package com.nfehs.librarygames.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import com.nfehs.librarygames.net.packets.*;
import com.nfehs.librarygames.net.packets.Packet.PacketTypes;

/**
 * This class handles receiving packets from and sending to the client
 * @author Patrick Kenney, Syed Quadri
 * @date 6/13/2018
 */

public class GameServer extends Thread {

	private DatagramSocket socket;
	private static final int PORT = 19602;
	
	public GameServer() {
		try {
			this.socket = new DatagramSocket(PORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Receives incoming packets from clients
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
			parsePacket(packet.getData(), packet.getAddress(), packet.getPort());
			
			System.out.println("CLIENT > " + new String(packet.getData()));
		}
	}
	
	/**
	 * Sends data to server
	 * @param data
	 */
	public void sendData(byte[] data, InetAddress ipAddress, int port) {
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Handles packets sent to the server
	 * @param data
	 * @param address
	 * @param port
	 */
	private void parsePacket(byte[] data, InetAddress address, int port) {
		String message = new String(data).trim();
		PacketTypes type = Packet.lookupPacket(message.substring(0, 2));
		Packet packet;
		
		switch (type) {
			case INVALID:				break;
			case LOGIN:					packet = new Packet00Login(data);
										break;
			case CREATEACCOUNT:			packet = new Packet01CreateAcc(data);
										break;
			default:					break;
		}
	}
}
