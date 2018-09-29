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
	private JLabel			error;
	private JButton			back;
	
	public CreateAccountScreen() {
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
					email.requestFocus();
			}
		});

		em = new JLabel("Email (optional):");
		Game.mainWindow.add(em);
		
		email = new JTextField();
		Game.mainWindow.add(email);
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
		
		password = new JPasswordField();
		Game.mainWindow.add(password);
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
		
		passwordConfirm = new JPasswordField();
		Game.mainWindow.add(passwordConfirm);
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
		createAccount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Create account clicked");
				createAccount();
			}
		});
		
		error = new JLabel();
		Game.mainWindow.add(error);
		error.setForeground(Color.RED);
		error.setHorizontalAlignment(SwingConstants.CENTER);
		
		back = new JButton("BACK");
		Game.mainWindow.add(back);
		back.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// returns user back to login screen
				System.out.println("Back clicked");
				Game.openLoginScreen();
			}
		});

		setPositions();
		Game.mainWindow.repaint();
	}
	
	/**
	 * Sets the positions for all items on screen
	 */
	protected void setPositions() {
		user.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 250, 150, 30);
		username.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 220, 150, 30);
		em.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 190, 150, 30);
		email.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 160, 150, 30);
		pass.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 130, 150, 30);
		password.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 100, 150, 30);
		passConfirm.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 70, 150, 30);
		passwordConfirm.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 40, 150, 30);
		createAccount.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 + 10, 150, 30);
		error.setBounds((int) Game.screenSize.getWidth() / 2 - 200, (int) Game.screenSize.getHeight() / 2 + 40, 400, 20);
		back.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 + 60, 150, 30);
	}
	
	/**
	 * Sets error text to the errorMessage
	 * @param errorMessage
	 */
	public void setError(String errorMessage) {
		error.setText(errorMessage);
	}
	
	/**
	 * Creates account for user
	 */
	private void createAccount() {
		Game.createAccount(username.getText(), email.getText(), password.getPassword(), passwordConfirm.getPassword());
	}
}
