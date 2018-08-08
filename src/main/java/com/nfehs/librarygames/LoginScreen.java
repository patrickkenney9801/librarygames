package com.nfehs.librarygames;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Base64;
import java.util.Base64.Encoder;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.nfehs.librarygames.net.packets.Packet00Login;

/**
 * This class handles the user's login and creating accounts
 * 
 * @author Patrick Kenney, Syed Quadri
 * @date 6/13/2018
 */

public class LoginScreen {
	private JLabel			user;
	private JLabel			pass;
	private JTextField		username;
	private JPasswordField	password;
	private JButton			login;
	private JButton			createAccount;
	
	public LoginScreen() {
		user = new JLabel("Username:");
		Game.mainWindow.add(user);
		user.setBackground(GameFrame.background);
		user.setForeground(Color.WHITE);
		user.setBounds((int) Game.screenSize.getWidth() / 2 - 150, (int) Game.screenSize.getHeight() / 2 - 220, 150, 30);
		
		username = new JTextField();
		Game.mainWindow.add(username);
		username.setBackground(GameFrame.textBackground);
		username.setForeground(Color.WHITE);
		username.setBounds((int) Game.screenSize.getWidth() / 2 - 150, (int) Game.screenSize.getHeight() / 2 - 190, 150, 30);

		pass = new JLabel("Password:");
		Game.mainWindow.add(pass);
		pass.setBackground(GameFrame.background);
		pass.setForeground(Color.WHITE);
		pass.setBounds((int) Game.screenSize.getWidth() / 2 - 150, (int) Game.screenSize.getHeight() / 2 - 140, 150, 30);
		
		password = new JPasswordField();
		Game.mainWindow.add(password);
		password.setBackground(GameFrame.textBackground);
		password.setForeground(Color.WHITE);
		password.setBounds((int) Game.screenSize.getWidth() / 2 - 150, (int) Game.screenSize.getHeight() / 2 - 110, 150, 30);
		
		login = new JButton("LOGIN");
		Game.mainWindow.add(login);
		login.setBounds((int) Game.screenSize.getWidth() / 2 - 150, (int) Game.screenSize.getHeight() / 2 - 50, 150, 30);
		login.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Login clicked");
				System.out.println(Security.encrypt(password.getText()).length());
				new Packet00Login("user", "pass").writeData(Game.client);
			}
		});
		
		createAccount = new JButton("CREATE ACCOUNT");
		Game.mainWindow.add(createAccount);
		createAccount.setBounds((int) Game.screenSize.getWidth() / 2 - 150, (int) Game.screenSize.getHeight() / 2, 150, 30);
		createAccount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Create account clicked");
				// TODO passwords must be 20 chars or less
			}
		});

		Game.mainWindow.repaint();
	}
	
	/**
	 * Removes all traces of the login screen (to be used before opening a new screen)
	 */
	private void exitLoginScreen() {
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
