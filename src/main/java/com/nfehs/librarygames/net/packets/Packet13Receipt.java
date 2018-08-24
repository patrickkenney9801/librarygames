package com.nfehs.librarygames.net.packets;

/**
 * Packets that are used as simple receipts
 * @author Patrick Kenney and Syed Quadri
 * @date 8/12/2018
 */

public class Packet13Receipt extends Packet {
	
	/**
	 * Used by both client and server to send
	 * @param packetKey
	 */
	public Packet13Receipt(String packetKey) {
		super(13);
		setUuidKey(packetKey);
	}
	
	/**
	 * Used by client and server to retrieve data
	 * @param data
	 */
	public Packet13Receipt(byte[] data) {
		super(13);
		
		try {
			setUuidKey(readData(data));
		} catch (Exception e) {
			e.printStackTrace();
			setValid(false);
		}
	}

	@Override
	public byte[] getData() {
		return ("13" + getUuidKey()).getBytes();
	}

	@Override
	public byte[] getDataServer() {
		return ("13" + getUuidKey()).getBytes();
	}
	
	
}
