package com.nfehs.librarygames;

import com.nfehs.librarygames.net.GameServer;

/**
 * This file launches the server
 * @author Patrick Kenney, Syed Quadri
 * @date 8/7/2018
 *
 */

public class LibraryGamesServer {

	public static void main(String[] args) {
		GameServer server = new GameServer();
		server.run();
	}

}
