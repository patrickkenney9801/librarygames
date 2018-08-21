package com.nfehs.librarygames.net.packets;

/**
 * Packets pertaining to errors
 * @author Patrick Kenney and Syed Quadri
 * @date 8/12/2018
 */

public class Packet02Error extends Packet {

	public Packet02Error() {
		super(02);
	}

	@Override
	public byte[] getData() {
		return null;
	}

	@Override
	public byte[] getDataServer() {
		return null;
	}
}
