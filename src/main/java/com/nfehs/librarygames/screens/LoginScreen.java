package com.nfehs.librarygames.screens;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.nfehs.librarygames.Game;

/**
 * This class handles the user's login creating accounts
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
	private JButton			createAccount;
	
	public LoginScreen() {
		super(true);
		
		user = new JLabel("Username:");
		Game.mainWindow.add(user);
		user.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 220, 150, 30);
		
		username = new JTextField();
		Game.mainWindow.add(username);
		username.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 190, 150, 30);
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
		pass.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 140, 150, 30);
		
		password = new JPasswordField();
		Game.mainWindow.add(password);
		password.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 110, 150, 30);
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
		login.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 50, 150, 30);
		login.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// logs the user into the server
				System.out.println("Login clicked");
				login();
			}
		});
		
		createAccount = new JButton("CREATE ACCOUNT");
		Game.mainWindow.add(createAccount);
		createAccount.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2, 150, 30);
		createAccount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Create account clicked");
				Game.openCreateAccountScreen();
			}
		});

		Game.mainWindow.repaint();
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
		Game.mainWindow.remove(createAccount);
		
		user			= null;
		pass			= null;
		username		= null;
		password		= null;
		login			= null;
		createAccount	= null;
		
		Game.mainWindow.repaint();
	}
}
