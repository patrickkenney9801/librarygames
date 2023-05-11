package com.nfehs.librarygames;

import javax.swing.UIManager;
import com.nfehs.librarygames.net.GameServer;

/**
 * This file launches the game loop or server depending on env var LIBRARY_GAMES_SERVER
 * @author Patrick Kenney, Syed Quadri
 * @date 6/13/2018
 *
 */

public class LibraryGames {
	public static Game game;

	public static void main(String[] args) {
		String serverMode = System.getenv("LIBRARY_GAMES_SERVER");
		if (serverMode != null && serverMode.equals("true")) {
			GameServer server = new GameServer();
			server.run();
		} else {
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
				
				// initialize game and gameframe
				game = new Game();
				Game.gameFrame = new GameFrame();
				
				// go to login screen
				Game.openLoginScreen();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
