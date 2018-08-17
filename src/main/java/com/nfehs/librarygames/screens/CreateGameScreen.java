package com.nfehs.librarygames.screens;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import com.nfehs.librarygames.Game;

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
		gameChoices.setSelectedIndex(0);
		
		userGoesFirst = new JRadioButton("I go first");
		Game.mainWindow.add(userGoesFirst);
		userGoesFirst.setBounds((int) Game.screenSize.getWidth() / 2 + 30, (int) Game.screenSize.getHeight() / 10, 120, 30);
		userGoesFirst.setSelected(true);
		
		opponentGoesFirst = new JRadioButton("Opponent goes first");
		Game.mainWindow.add(opponentGoesFirst);
		opponentGoesFirst.setBounds((int) Game.screenSize.getWidth() / 2 + 160, (int) Game.screenSize.getHeight() / 10, 150, 30);
		
		startingPlayer = new ButtonGroup();
		startingPlayer.add(userGoesFirst);
		startingPlayer.add(opponentGoesFirst);
		
		chooseOpponent = new JLabel("Choose opponent:");
		Game.mainWindow.add(chooseOpponent);
		chooseOpponent.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 10 + 75, 150, 30);
		
		chooseFriend = new JLabel("Friends:");
		Game.mainWindow.add(chooseFriend);
		chooseFriend.setBounds((int) Game.screenSize.getWidth() / 2 - 275, (int) Game.screenSize.getHeight() / 10 + 125, 150, 30);
		
		chooseRandomPlayer = new JLabel("All players:");
		Game.mainWindow.add(chooseRandomPlayer);
		chooseRandomPlayer.setBounds((int) Game.screenSize.getWidth() / 2 + 10, (int) Game.screenSize.getHeight() / 10 + 125, 150, 30);
		
		players = new ButtonGroup();
		friends = new JRadioButton[0];
		randomPlayers = new JRadioButton[0];
		addFriends = new JButton[0];
		
		createGame = new JButton("CREATE GAME");
		Game.mainWindow.add(createGame);
		createGame.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 5 * 4, 150, 30);
		createGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// attempts to create a new game
				System.out.println("Create game clicked");
				
				// get other user from selected JRadioButton
				String otherUser = null;
				for (JRadioButton button : friends)
					if (button.isSelected())
						otherUser = button.getText();
				for (JRadioButton button : randomPlayers)
					if (button.isSelected())
						otherUser = button.getText();
				
				// if no opponent is selected to play against, exit and send error to screen
				if (otherUser == null) {
					// TODO put error message
					System.out.println("No opponent selected");
					return;
				}
				
				// get creator goes first from startingPlayer
				boolean creatorGoesFirst = false;
				if (userGoesFirst.isSelected())
					creatorGoesFirst = true;
				
				// get gameType from selected index of gameChoices
				int gameType = gameChoices.getSelectedIndex();
				
				// if the no gameType is selected or there is some other error, exit and send message to screen
				if (gameType < 0) {
					// TODO put error message
					System.out.println("No game type selected");
					return;
				}
				Game.createGame(otherUser, creatorGoesFirst, gameType);
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
			players.add(friends[i]);
			Game.mainWindow.add(friends[i]);
		}
		
		// add other players
		randomPlayers = new JRadioButton[Game.getPlayer().getOtherPlayers().length];
		for (int i = 0; i < randomPlayers.length; i++) {
			randomPlayers[i] = new JRadioButton(Game.getPlayer().getOtherPlayers()[i]);
			randomPlayers[i].setBounds((int) Game.screenSize.getWidth() / 2 + 10, 
								(int) Game.screenSize.getHeight() / 10 + 155 + 30*i, 200, 30);
			players.add(randomPlayers[i]);
			Game.mainWindow.add(randomPlayers[i]);
		}
		
		// add 'add friend' buttons
		addFriends = new JButton[randomPlayers.length];
		for (int i = 0; i < addFriends.length; i++) {
			addFriends[i] = new JButton("ADD");
			Game.mainWindow.add(addFriends[i]);
			addFriends[i].setBounds((int) Game.screenSize.getWidth() / 2 + 210, 
					 (int) Game.screenSize.getHeight() / 10 + 155 + i*30, 75, 30);
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
		Game.mainWindow.repaint();
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