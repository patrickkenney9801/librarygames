package com.nfehs.librarygames;

import java.awt.Container;
import java.awt.Dimension;
import java.net.UnknownHostException;

import javax.swing.JFrame;

import com.nfehs.librarygames.net.GameClient;
import com.nfehs.librarygames.net.Security;
import com.nfehs.librarygames.startup.CreateAccountScreen;
import com.nfehs.librarygames.startup.LoginScreen;

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
	private static LoginScreen login;
	private static CreateAccountScreen createAccount;
	
	private static GameClient client;
	
	public static final byte[] SERVER_IP_ADDRESS = {108, (byte) 205, (byte) 143, 97};
	
	public static final int LOGIN = 0;
	public static final int OVER = 10;
	
	public static int gameState = Game.LOGIN;
	public static boolean gamePlaying = true;

	public Game() throws UnknownHostException {
		client = new GameClient(this, SERVER_IP_ADDRESS);
		client.start();
	}

	/**
	 * This method opens up the login screen for the user
	 */
	public static void openLoginScreen() {
		login = new LoginScreen();
	}

	/**
	 * This method opens up the login screen for the user
	 */
	public static void openCreateAccountScreen() {
		createAccount = new CreateAccountScreen();
	}
	
	/**
	 * This method logs in the user online
	 * @param username
	 * @param password
	 */
	public static void login(String user, char[] pass) {
		if (user == null || user.length() < 1) {
			// TODO error user empty
			return;
		} else if (pass == null || pass.length < 1) {
			// TODO error pass empty
			return;
		} else if (user.length() > 20) {
			// TODO error user too long
			return;
		} else if (pass.length > 20) {
			// TODO error pass too long
			return;
		}
		
		String username = Security.encrypt(user);
		String password = "";
		for (char c : pass)
			password += (c + 15);
		password = Security.encrypt(password);
		
		client.login(username, password);
	}

	/**
	 * This method attempts to create a new account
	 * @param username
	 * @param email
	 * @param password
	 * @param password2
	 */
	public static void createAccount(String user, String email, char[] pass, char[] pass2) {
		if (user == null || user.length() < 1) {
			// TODO error user empty
			return;
		} else if (pass == null || pass.length < 1) {
			// TODO error pass empty
			return;
		} else if (user.length() > 20) {
			// TODO error user too long
			return;
		} else if (pass.length > 20) {
			// TODO error pass too long
			return;
		} else if (pass.length != pass2.length) {
			// TODO error passwords do not match
			return;
		} else if (email.contains(":")) {
			// TODO error invalid email
			return;
		}
		
		String username = Security.encrypt(user);
		String password = "";
		for (int i = 0; i < pass.length; i++) {
			if (pass[i] != pass2[i]) {
				// TODO error passwords do not match
				return;
			}
			password += (pass[i] + 15);
		}
		password = Security.encrypt(password);
		
		client.createAccount(email, username, password);
	}
}
