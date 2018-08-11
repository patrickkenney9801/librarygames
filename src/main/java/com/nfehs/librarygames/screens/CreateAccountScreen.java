package com.nfehs.librarygames.screens;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.nfehs.librarygames.Game;
import com.nfehs.librarygames.GameFrame;

/**
 * This class handles the user creating accounts
 * 
 * @author Patrick Kenney, Syed Quadri
 * @date 8/8/2018
 */

public class CreateAccountScreen extends Screen {
	private JLabel			user;
	private JLabel			em;
	private JLabel			pass;
	private JLabel			passConfirm;
	private JTextField		username;
	private JTextField		email;
	private JPasswordField	password;
	private JPasswordField	passwordConfirm;
	private JButton			createAccount;
	private JButton			back;
	
	public CreateAccountScreen() {
		user = new JLabel("Username:");
		Game.mainWindow.add(user);
		user.setBackground(GameFrame.background);
		user.setForeground(Color.WHITE);
		user.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 250, 150, 30);
		
		username = new JTextField();
		Game.mainWindow.add(username);
		username.setBackground(GameFrame.textBackground);
		username.setForeground(Color.WHITE);
		username.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 220, 150, 30);

		em = new JLabel("Email (optional):");
		Game.mainWindow.add(em);
		em.setBackground(GameFrame.background);
		em.setForeground(Color.WHITE);
		em.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 190, 150, 30);
		
		email = new JTextField();
		Game.mainWindow.add(email);
		email.setBackground(GameFrame.textBackground);
		email.setForeground(Color.WHITE);
		email.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 160, 150, 30);

		pass = new JLabel("Password:");
		Game.mainWindow.add(pass);
		pass.setBackground(GameFrame.background);
		pass.setForeground(Color.WHITE);
		pass.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 130, 150, 30);
		
		password = new JPasswordField();
		Game.mainWindow.add(password);
		password.setBackground(GameFrame.textBackground);
		password.setForeground(Color.WHITE);
		password.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 100, 150, 30);

		passConfirm = new JLabel("Password (confirm):");
		Game.mainWindow.add(passConfirm);
		passConfirm.setBackground(GameFrame.background);
		passConfirm.setForeground(Color.WHITE);
		passConfirm.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 70, 150, 30);
		
		passwordConfirm = new JPasswordField();
		Game.mainWindow.add(passwordConfirm);
		passwordConfirm.setBackground(GameFrame.textBackground);
		passwordConfirm.setForeground(Color.WHITE);
		passwordConfirm.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 40, 150, 30);
		
		createAccount = new JButton("CREATE ACCOUNT");
		Game.mainWindow.add(createAccount);
		createAccount.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 + 10, 150, 30);
		createAccount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Create account clicked");
				Game.createAccount(username.getText(), email.getText(), password.getPassword(), passwordConfirm.getPassword());
			}
		});
		
		back = new JButton("BACK");
		Game.mainWindow.add(back);
		back.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 + 60, 150, 30);
		back.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// returns user back to login screen
				System.out.println("Back clicked");
				Game.openLoginScreen();
			}
		});

		Game.mainWindow.repaint();
	}
	
	/**
	 * Removes all traces of the login screen (to be used before opening a new screen)
	 */
	public void exit() {
		Game.mainWindow.remove(user);
		Game.mainWindow.remove(em);
		Game.mainWindow.remove(pass);
		Game.mainWindow.remove(passConfirm);
		Game.mainWindow.remove(username);
		Game.mainWindow.remove(email);
		Game.mainWindow.remove(password);
		Game.mainWindow.remove(passwordConfirm);
		Game.mainWindow.remove(createAccount);
		Game.mainWindow.remove(back);
		
		user			= null;
		em				= null;
		pass			= null;
		passConfirm		= null;
		username		= null;
		email			= null;
		password		= null;
		passwordConfirm	= null;
		createAccount	= null;
		back			= null;
		
		Game.mainWindow.repaint();
	}
}
