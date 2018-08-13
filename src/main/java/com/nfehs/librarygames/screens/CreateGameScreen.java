package com.nfehs.librarygames.screens;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import com.nfehs.librarygames.Game;
import com.nfehs.librarygames.GameFrame;

/**
 * Handles the user creating a game, 
 * @author Patrick Kenney and Syed Quadri
 * @date 8/11/2018
 */

public class CreateGameScreen extends Screen {
	private final String[] choices = {"Go 9x9", "Go 13x13", "Go 19x19"};
	private JComboBox<String> gameChoices;
	private ButtonGroup startingPlayer;
	private JRadioButton userGoesFirst;
	private JRadioButton opponentGoesFirst;
	private JButton createGame;
	private JButton back;
	
	private JLabel chooseOpponent;
	private JLabel chooseFriend;
	private JLabel chooseRandomPlayer;
	private ButtonGroup players;
	private JRadioButton[] friends;
	private JRadioButton[] randomPlayers;
	private JButton[] addFriends;
	
	public CreateGameScreen() {
		super(false);
		
		gameChoices = new JComboBox<String>(choices);
		Game.mainWindow.add(gameChoices);
		gameChoices.setBounds((int) Game.screenSize.getWidth() / 2 - 225, (int) Game.screenSize.getHeight() / 10, 150, 30);
		gameChoices.setBackground(GameFrame.background);
		gameChoices.setForeground(Color.WHITE);
		gameChoices.setSelectedIndex(0);
		
		userGoesFirst = new JRadioButton("I go first");
		Game.mainWindow.add(userGoesFirst);
		userGoesFirst.setBounds((int) Game.screenSize.getWidth() / 2 + 30, (int) Game.screenSize.getHeight() / 10, 120, 30);
		userGoesFirst.setBackground(GameFrame.background);
		userGoesFirst.setForeground(Color.WHITE);
		userGoesFirst.setSelected(true);
		
		opponentGoesFirst = new JRadioButton("Opponent goes first");
		Game.mainWindow.add(opponentGoesFirst);
		opponentGoesFirst.setBounds((int) Game.screenSize.getWidth() / 2 + 160, (int) Game.screenSize.getHeight() / 10, 150, 30);
		opponentGoesFirst.setBackground(GameFrame.background);
		opponentGoesFirst.setForeground(Color.WHITE);
		
		startingPlayer = new ButtonGroup();
		startingPlayer.add(userGoesFirst);
		startingPlayer.add(opponentGoesFirst);
		
		chooseOpponent = new JLabel("Choose opponent:");
		Game.mainWindow.add(chooseOpponent);
		chooseOpponent.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 10 + 75, 150, 30);
		chooseOpponent.setBackground(GameFrame.background);
		chooseOpponent.setForeground(Color.WHITE);
		
		chooseFriend = new JLabel("Friends:");
		Game.mainWindow.add(chooseFriend);
		chooseFriend.setBounds((int) Game.screenSize.getWidth() / 2 - 275, (int) Game.screenSize.getHeight() / 10 + 125, 150, 30);
		chooseFriend.setBackground(GameFrame.background);
		chooseFriend.setForeground(Color.WHITE);
		
		chooseRandomPlayer = new JLabel("All players:");
		Game.mainWindow.add(chooseRandomPlayer);
		chooseRandomPlayer.setBounds((int) Game.screenSize.getWidth() / 2 + 10, (int) Game.screenSize.getHeight() / 10 + 125, 150, 30);
		chooseRandomPlayer.setBackground(GameFrame.background);
		chooseRandomPlayer.setForeground(Color.WHITE);
		
		players = new ButtonGroup();
		friends = new JRadioButton[0];
		randomPlayers = new JRadioButton[0];
		addFriends = new JButton[0];
		
		createGame = new JButton("CREATE GAME");
		Game.mainWindow.add(createGame);
		createGame.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 5 * 4, 150, 30);
		createGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// creates game
				System.out.println("Create game clicked");
			}
		});

		back = new JButton("BACK");
		Game.mainWindow.add(back);
		back.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 5 * 4 + 60, 150, 30);
		back.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// goes back to active games list
				System.out.println("Back clicked from create game");
				Game.openActiveGamesScreen();
			}
		});
	}
	
	/**
	 * This method is called once the client has received player data from the server
	 */
	public void loadPlayers() {
		// remove current players
		removeAndNullPlayers();
		
		// add friends
		friends = new JRadioButton[Game.getPlayer().getFriends().length];
		for (int i = 0; i < friends.length; i++) {
			friends[i] = new JRadioButton(Game.getPlayer().getFriends()[i]);
			friends[i].setBounds((int) Game.screenSize.getWidth() / 2 - 275, 
								(int) Game.screenSize.getHeight() / 10 + 155 + 30*i, 150, 30);
			friends[i].setForeground(Color.WHITE);
			friends[i].setBackground(GameFrame.background);
			players.add(friends[i]);
		}
		
		// add other players
		randomPlayers = new JRadioButton[Game.getPlayer().getOtherPlayers().length];
		for (int i = 0; i < randomPlayers.length; i++) {
			randomPlayers[i] = new JRadioButton(Game.getPlayer().getOtherPlayers()[i]);
			randomPlayers[i].setBounds((int) Game.screenSize.getWidth() / 2 + 10, 
								(int) Game.screenSize.getHeight() / 10 + 155 + 30*i, 150, 30);
			randomPlayers[i].setForeground(Color.WHITE);
			randomPlayers[i].setBackground(GameFrame.background);
			players.add(randomPlayers[i]);
		}
		
		// add 'add friend' buttons
		addFriends = new JButton[randomPlayers.length];
		for (int i = 0; i < addFriends.length; i++) {
			addFriends[i] = new JButton("ADD");
			Game.mainWindow.add(addFriends[i]);
			addFriends[i].setBounds((int) Game.screenSize.getWidth() / 2 + 160, 
					 (int) Game.screenSize.getHeight() / 10 + 200 + i*50, 150, 30);
			addFriends[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// get the element number for the username
					int elemNumber = -1;
					for (int i = 0; i < addFriends.length; i++)
						if (addFriends[i].equals(e.getSource()))
							elemNumber = i;
					System.out.println("Add friend button clicked: " + randomPlayers[elemNumber].getText());
					((JButton) e.getSource()).setEnabled(false);
					
					// send friend request to server
					Game.addFriend(randomPlayers[elemNumber].getText());
				}
			});
		}
	}
	
	/**
	 * Removes and nulls all current players in JRadioButtons
	 */
	private void removeAndNullPlayers() {
		for (JRadioButton friend : friends) {
			Game.mainWindow.remove(friend);
			friend = null;
		}
		for (JRadioButton randomPlayer : randomPlayers) {
			Game.mainWindow.remove(randomPlayer);
			randomPlayer = null;
		}
		for (JButton addFriend : addFriends) {
			Game.mainWindow.remove(addFriend);
			addFriend = null;
		}
	}

	@Override
	public void exit() {
		exitParentGUI();
		
		Game.mainWindow.remove(gameChoices);
		Game.mainWindow.remove(userGoesFirst);
		Game.mainWindow.remove(opponentGoesFirst);
		Game.mainWindow.remove(createGame);
		Game.mainWindow.remove(chooseOpponent);
		Game.mainWindow.remove(chooseFriend);
		Game.mainWindow.remove(chooseRandomPlayer);
		Game.mainWindow.remove(back);
		
		gameChoices = null;
		userGoesFirst = null;
		opponentGoesFirst = null;
		startingPlayer = null;
		createGame = null;
		chooseOpponent = null;
		chooseFriend = null;
		chooseRandomPlayer = null;
		back = null;
		
		removeAndNullPlayers();
		players = null;
		addFriends = null;
		
		Game.mainWindow.repaint();
	}
}