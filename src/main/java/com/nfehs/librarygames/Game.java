package com.nfehs.librarygames;

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JFrame;

import com.nfehs.librarygames.net.GameClient;

/**
 * This class hosts the game flow
 * @author Patrick Kenney, Syed Quadri
 * @date 6/13/2018
 *
 */

public class Game {
	public static GameFrame gameFrame;
	public static JFrame window;
	public static Container mainWindow;
	public static Dimension screenSize;
	public static LoginScreen login;
	
	private static GameClient client;
	
	public static final int LOGIN = 0;
	public static final int OVER = 10;
	
	public static int gameState = Game.LOGIN;
	public static boolean gamePlaying = true;

	public Game() {
		client = new GameClient(this, "localhost");
		client.start();
	}

	/**
	 * This method opens up the login screen for the user
	 */
	public static void openLoginScreen() {
		login = new LoginScreen();
	}
}
