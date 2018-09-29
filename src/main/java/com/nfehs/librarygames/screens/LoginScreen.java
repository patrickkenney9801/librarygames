package com.nfehs.librarygames.screens;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.nfehs.librarygames.Game;

/**
 * This class handles the user's login, creating accounts
 * 
 * @author Patrick Kenney, Syed Quadri
 * @date 6/13/2018
 */

public class LoginScreen extends Screen {
	private JLabel			user;
	private JLabel			pass;
	private JTextField		username;
	private JPasswordField	password;
	private JButton			login;
	private JLabel			error;
	private JButton			createAccount;
	private JButton			offline;
	
	public LoginScreen() {
		super(true);
		
		user = new JLabel("Username:");
		Game.mainWindow.add(user);
		
		username = new JTextField();
		Game.mainWindow.add(username);
		username.requestFocusInWindow();
		username.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == '\n')
					password.requestFocus();
			}
		});

		pass = new JLabel("Password:");
		Game.mainWindow.add(pass);
		
		password = new JPasswordField();
		Game.mainWindow.add(password);
		password.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == '\n')
					login();
			}
		});
		
		login = new JButton("LOGIN");
		Game.mainWindow.add(login);
		login.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// logs the user into the server
				System.out.println("Login clicked");
				login();
			}
		});
		
		error = new JLabel();
		Game.mainWindow.add(error);
		error.setForeground(Color.RED);
		error.setHorizontalAlignment(SwingConstants.CENTER);
		
		createAccount = new JButton("CREATE ACCOUNT");
		Game.mainWindow.add(createAccount);
		createAccount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Create account clicked");
				Game.openCreateAccountScreen();
			}
		});
		
		offline = new JButton("PLAY OFFLINE");
		Game.mainWindow.add(offline);
		offline.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Offline play clicked");
				Game.openCreateOfflineGameScreen();
			}
		});

		setPositions();
		Game.mainWindow.repaint();
	}
	
	/**
	 * Sets the positions for all items on screen
	 */
	protected void setPositions() {
		user.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 220, 150, 30);
		username.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 190, 150, 30);
		pass.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 140, 150, 30);
		password.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 110, 150, 30);
		login.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 50, 150, 30);
		error.setBounds((int) Game.screenSize.getWidth() / 2 - 150, (int) Game.screenSize.getHeight() / 2 - 20, 300, 20);
		createAccount.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2, 150, 30);
		offline.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 + 75, 150, 30);
	}
	
	/**
	 * Sets error text to the errorMessage
	 * @param errorMessage
	 */
	public void setError(String errorMessage) {
		error.setText(errorMessage);
	}
	
	/**
	 * Attempts to log the user into server
	 */
	private void login() {
		Game.login(username.getText(), password.getPassword());
	}
	
	/**
	 * Removes all traces of the login screen (to be used before opening a new screen)
	 */
	public void exit() {
		Game.mainWindow.remove(user);
		Game.mainWindow.remove(pass);
		Game.mainWindow.remove(username);
		Game.mainWindow.remove(password);
		Game.mainWindow.remove(login);
		Game.mainWindow.remove(error);
		Game.mainWindow.remove(createAccount);
		Game.mainWindow.remove(offline);
		
		user			= null;
		pass			= null;
		username		= null;
		password		= null;
		login			= null;
		error			= null;
		createAccount	= null;
		offline			= null;
		
		Game.mainWindow.repaint();
	}
}
