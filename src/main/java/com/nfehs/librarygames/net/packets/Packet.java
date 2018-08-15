package com.nfehs.librarygames.net.packets;

import java.net.InetAddress;
import java.util.UUID;

import com.nfehs.librarygames.net.GameClient;
import com.nfehs.librarygames.net.GameServer;

/**
 * This is the parent class for all packets sent to and from the server
 * Packets have data and methods for use by both client and server sides
 * @author Patrick Kenney, Syed Quadri
 * @date 6/13/2018
 */

public abstract class Packet {
	/**
	 * Creates an enum to encompass all packet types
	 * @author Patrick
	 */
	public static enum PacketTypes {
		INVALID(-1), LOGIN(00), CREATEACCOUNT(01), ERROR(02), LOGOUT(03),
		GETPLAYERS(04), ADDFRIEND(05), CREATEGAME(06), GETGAMES(07), GETBOARD(8);
		
		private int packetId;
		private PacketTypes(int packetId) {
			this.packetId = packetId;
		}
		
		public int getId() {
			return packetId;
		}
	}
	
	private byte packetId;
	private String uuidKey;
	
	public Packet(int packetId) {
		setPacketId((byte) packetId); 
		setUuidKey("" + UUID.randomUUID());
	}
	
	/**
	 * Sends data to the server from client
	 * @param client
	 */
	public void writeData(GameClient client) {
		client.sendData(getData());
	}
	/**
	 * Sends data to the client from server
	 * @param server
	 * @param address
	 * @param port
	 */
	public void writeData(GameServer server, InetAddress address, int port) {
		server.sendData(getDataServer(), address, port);
	}

	// these will be implemented to create data
	public abstract byte[] getData();			// creates packet data to send to server
	public abstract byte[] getDataServer();		// creates packet data to send to client
	
	/**
	 * This method will read messages sent, cuts off id (first 2 chars)
	 * @param data
	 * @return
	 */
	public String readData(byte[] data) {
		String message = new String(data).trim();
		return message.substring(2);
	}
	
	/**
	 * This method returns what type of packet the packet is
	 * @param id String
	 * @return
	 */
	public static PacketTypes lookupPacket(String id) {
		try {
			return lookupPacket(Integer.parseInt(id));
		} catch (Exception e) {
			return PacketTypes.INVALID;
		}
	}
	
	/**
	 * This method returns what type of packet the packet is
	 * @param id int
	 * @return
	 */
	public static PacketTypes lookupPacket(int id) {
		for (PacketTypes p : PacketTypes.values()) {
			if (p.getId() == id)
				return p;
		}
		return PacketTypes.INVALID;
	}

	/**
	 * @return the packetId
	 */
	public byte getPacketId() {
		return packetId;
	}

	/**
	 * @param packetId the packetId to set
	 */
	public void setPacketId(byte packetId) {
		this.packetId = packetId;
	}

	/**
	 * @return the uuidKey
	 */
	public String getUuidKey() {
		return uuidKey;
	}

	/**
	 * @param uuidKey the uuidKey to set
	 */
	public void setUuidKey(String uuidKey) {
		this.uuidKey = uuidKey;
	}
}
