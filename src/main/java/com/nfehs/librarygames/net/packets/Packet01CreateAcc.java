package com.nfehs.librarygames.net.packets;

import com.nfehs.librarygames.net.GameClient;
import com.nfehs.librarygames.net.GameServer;

public class Packet01CreateAcc extends Packet {
	
	public Packet01CreateAcc(byte[] data) {
		super(01);
	}

	@Override
	public void writeData(GameClient client) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeData(GameServer server) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] getData() {
		// TODO Auto-generated method stub
		return null;
	}

}
