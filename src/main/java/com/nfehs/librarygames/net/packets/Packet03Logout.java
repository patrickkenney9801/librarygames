package com.nfehs.librarygames.net.packets;

/**
 * Packets pertaining to logging out, this class does not send a response package from server
 * @author Patrick Kenney and Syed Quadri
 * @date 8/12/2018
 */

public class Packet03Logout extends Packet {
	public Packet03Logout() {
		super(03);
	}

	@Override
	public byte[] getData() {
		return ("03" + getUuidKey()).getBytes();
	}

	/**
	 * @Override
	 */
	public byte[] getDataServer() {
		return getData();
	}
}
