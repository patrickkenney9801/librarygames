package com.nfehs.librarygames.screens;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.nfehs.librarygames.Game;
import com.nfehs.librarygames.games.BoardGame;
import com.nfehs.librarygames.net.Security;

/**
 * Handles the user creating an offline game, 
 * @author Patrick Kenney and Syed Quadri
 * @date 9/3/2018
 */

public class CreateOfflineGameScreen extends Screen {
	private final String[] choices = {"Go 9x9", "Go 13x13", "Go 19x19"};
	private JComboBox<String> gameChoices;

	private JLabel p1;
	private JLabel p2;
	private JTextField player1;
	private JTextField player2;
	
	private JButton createOfflineGame;
	private JButton back;
	
	public CreateOfflineGameScreen() {
		super(true);
		
		gameChoices = new JComboBox<String>(choices);
		Game.mainWindow.add(gameChoices);
		gameChoices.setSelectedIndex(0);
		
		
		p1 = new JLabel("Player 1:");
		Game.mainWindow.add(p1);
		
		player1 = new JTextField();
		Game.mainWindow.add(player1);
		player1.requestFocusInWindow();
		player1.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == '\n')
					player2.requestFocus();
			}
		});
		
		p2 = new JLabel("Player 2:");
		Game.mainWindow.add(p2);
		
		player2 = new JTextField();
		Game.mainWindow.add(player2);
		player2.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == '\n')
					createOfflineGame.requestFocus();
			}
		});

		createOfflineGame = new JButton("CREATE GAME");
		Game.mainWindow.add(createOfflineGame);
		createOfflineGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// create a new offline game
				System.out.println("Create offline game clicked");

				// if the usernames are the same, or left empty, set to default values
				String player1Username = player1.getText();
				String player2Username = player2.getText();
				
				if (player1Username.length() < 1)
					player1Username = "Player 1";
				if (player2Username.length() < 1)
					player2Username = "Player 2";
				if (player1Username.equals(player2Username)) {
					player1Username = "Player 1";
					player2Username = "Player 2";
				}
				player1Username = Security.encrypt(player1Username);
				player2Username = Security.encrypt(player2Username);
				
				// if the no gameType is selected or there is some other error, exit and send message to screen
				int gameType = gameChoices.getSelectedIndex();
				if (gameType < 0)
					gameType = 0;
				
				// create an offline board game and open game screen
				Game.setBoardGame(BoardGame.createGame("GameKey", gameType, player1Username, player2Username, 0, -5, -5, 0, true, true,
						BoardGame.createNewBoard(gameType), BoardGame.createExtraGameInfo(gameType)));
				Game.openGameScreen();
			}
		});

		back = new JButton("BACK");
		Game.mainWindow.add(back);
		back.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// goes back to login screen
				System.out.println("Back clicked from create offline game");
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
		gameChoices.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 300, 150, 30);
		p1.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 250, 150, 30);
		player1.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 220, 150, 30);
		p2.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 170, 150, 30);
		player2.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 140, 150, 30);
		createOfflineGame.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 50, 150, 30);
		back.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2, 150, 30);
	}
}