package com.nfehs.librarygames.net.packets;

import com.nfehs.librarygames.net.GameClient;
import com.nfehs.librarygames.net.GameServer;

/**
 * This is the parent class for all packets sent to and from the server
 * @author Patrick Kenney, Syed Quadri
 * @date 6/13/2018
 */

public abstract class Packet {
	/**
	 * Creates an enum to encompass all packet types
	 * @author Patrick
	 *
	 */
	public static enum PacketTypes {
		INVALID(-1), LOGIN(00), CREATEACCOUNT(01);
		
		private int packetId;
		private PacketTypes(int packetId) {
			this.packetId = packetId;
		}
		
		public int getId() {
			return packetId;
		}
	}
	
	public byte packetId;
	
	public Packet(int packetId) {
		this.packetId = (byte) packetId;
	}
	
	// these will be implemented to send data
	public abstract void writeData(GameClient client);
	public abstract void writeData(GameServer server);
	
	public abstract byte[] getData();
	
	/**
	 * This method will read messages sent, cuts off id (first 2 chars)
	 * @param data
	 * @return
	 */
	public String readData(byte[] data) {
		String message = new String(data).trim();
		return message.substring(2);
	}
	
	public static PacketTypes lookupPacket(String id) {
		try {
			return lookupPacket(Integer.parseInt(id));
		} catch (Exception e) {
			return PacketTypes.INVALID;
		}
	}
	
	/**
	 * This method returns what type of packet the packet is
	 * @param id
	 * @return
	 */
	public static PacketTypes lookupPacket(int id) {
		for (PacketTypes p : PacketTypes.values()) {
			if (p.getId() == id)
				return p;
		}
		return PacketTypes.INVALID;
	}
}
