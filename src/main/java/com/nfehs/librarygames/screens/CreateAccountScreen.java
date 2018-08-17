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
		super(true);
		
		user = new JLabel("Username:");
		Game.mainWindow.add(user);
		user.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 250, 150, 30);
		
		username = new JTextField();
		Game.mainWindow.add(username);
		username.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 220, 150, 30);
		username.requestFocusInWindow();
		username.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == '\n')
					email.requestFocus();
			}
		});

		em = new JLabel("Email (optional):");
		Game.mainWindow.add(em);
		em.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 190, 150, 30);
		
		email = new JTextField();
		Game.mainWindow.add(email);
		email.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 160, 150, 30);
		email.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == '\n')
					password.requestFocus();
			}
		});
		
		pass = new JLabel("Password:");
		Game.mainWindow.add(pass);
		pass.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 130, 150, 30);
		
		password = new JPasswordField();
		Game.mainWindow.add(password);
		password.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 100, 150, 30);
		password.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == '\n')
					passwordConfirm.requestFocus();
			}
		});
		
		passConfirm = new JLabel("Password (confirm):");
		Game.mainWindow.add(passConfirm);
		passConfirm.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 70, 150, 30);
		
		passwordConfirm = new JPasswordField();
		Game.mainWindow.add(passwordConfirm);
		passwordConfirm.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 40, 150, 30);
		passwordConfirm.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == '\n')
					createAccount();
			}
		});
		
		createAccount = new JButton("CREATE ACCOUNT");
		Game.mainWindow.add(createAccount);
		createAccount.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 + 10, 150, 30);
		createAccount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Create account clicked");
				createAccount();
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
	 * Creates account for user
	 */
	private void createAccount() {
		Game.createAccount(username.getText(), email.getText(), password.getPassword(), passwordConfirm.getPassword());
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
