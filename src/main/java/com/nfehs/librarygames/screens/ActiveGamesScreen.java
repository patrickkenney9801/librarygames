package com.nfehs.librarygames.screens;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import com.nfehs.librarygames.Game;

/**
 * This class handles the user browsing their active games
 * It also directs players to creating new games and spectating games
 * @author Patrick Kenney and Syed Quadri
 * @date 8/11/2018
 */

public class ActiveGamesScreen extends Screen {
	private JButton		createGame;
	private JButton		spectatorGames;
	private JButton		refresh;
	private JLabel		userTurn;
	private JButton[]	activeGamesUserTurn;
	private JLabel		opponentTurn;
	private JButton[]	activeGames;
	
	public ActiveGamesScreen() {
		super(false);
		
		createGame = new JButton("NEW GAME");
		Game.mainWindow.add(createGame);
		createGame.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 10, 150, 30);
		createGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Create game clicked");
				Game.openCreateGameScreen();
			}
		});
		
		spectatorGames = new JButton("SPECTATE GAMES");
		Game.mainWindow.add(spectatorGames);
		spectatorGames.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 10 + 50, 150, 30);
		spectatorGames.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Spectator games clicked");
				// TODO
			}
		});
		
		refresh = new JButton("REFRESH");
		Game.mainWindow.add(refresh);
		refresh.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 10 + 160, 150, 30);
		refresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Refresh clicked");
				// TODO determine whether or not to keep refresh button
				Game.getActiveGames();
			}
		});
		activeGames = new JButton[0];
		activeGamesUserTurn = new JButton[0];
		userTurn = new JLabel();
		opponentTurn = new JLabel();
	}
	
	/**
	 * This method is called once the client has received the game data from the server
	 */
	public void loadActiveGames() {
		// remove current active games
		removeAndNullActiveGames();
		
		// add active games user turn
		userTurn = new JLabel("Your turn:");
		Game.mainWindow.add(userTurn);
		userTurn.setBounds ((int) Game.screenSize.getWidth() / 2 - 150, 
							(int) Game.screenSize.getHeight() / 10 + 200, 300, 30);
		
		activeGamesUserTurn = new JButton[Game.getPlayer().getYourTurnBoardGames().length];
		for (int i = 0; i < activeGamesUserTurn.length; i++) {
			activeGamesUserTurn[i] = new JButton(Game.getPlayer().getYourTurnBoardGames()[i].split("~")[1]);
			Game.mainWindow.add(activeGamesUserTurn[i]);
			activeGamesUserTurn[i].setBounds((int) Game.screenSize.getWidth() / 2 - 150, 
									 (int) Game.screenSize.getHeight() / 10 + 230 + i*50, 300, 30);
			activeGamesUserTurn[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("An active game clicked: " + ((JButton) e.getSource()).getText());
					
					// get the element number of button to find game key
					int elemNumber = -1;
					for (int i = 0; i < activeGamesUserTurn.length; i++)
						if (activeGamesUserTurn[i].equals(e.getSource()))
							elemNumber = i;
					
					// send get board request to server
					Game.getBoard(Game.getPlayer().getYourTurnBoardGames()[elemNumber].split("~")[0]);
				}
			});
		}
		
		// add new active games
		int height = activeGamesUserTurn.length * 50;
		
		opponentTurn = new JLabel("Opponent turn:");
		Game.mainWindow.add(opponentTurn);
		opponentTurn.setBounds ((int) Game.screenSize.getWidth() / 2 - 150, 
							(int) Game.screenSize.getHeight() / 10 + 240 + height, 300, 30);
		
		activeGames = new JButton[Game.getPlayer().getOpponentTurnBoardGames().length];
		for (int i = 0; i < activeGames.length; i++) {
			activeGames[i] = new JButton(Game.getPlayer().getOpponentTurnBoardGames()[i].split("~")[1]);
			Game.mainWindow.add(activeGames[i]);
			activeGames[i].setBounds((int) Game.screenSize.getWidth() / 2 - 150, 
									 (int) Game.screenSize.getHeight() / 10 + 270 + height + i*50, 300, 30);
			activeGames[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("An active game clicked: " + ((JButton) e.getSource()).getText());
					
					// get the element number of button to find game key
					int elemNumber = -1;
					for (int i = 0; i < activeGames.length; i++)
						if (activeGames[i].equals(e.getSource()))
							elemNumber = i;
					
					// send get board request to server
					Game.getBoard(Game.getPlayer().getOpponentTurnBoardGames()[elemNumber].split("~")[0]);
				}
			});
		}
		Game.mainWindow.repaint();
	}
	
	/**
	 * Removes all active games from main window and sets the null
	 */
	private void removeAndNullActiveGames() {
		for (JButton activeGame : activeGamesUserTurn) {
			Game.mainWindow.remove(activeGame);
			activeGame = null;
		}
		for (JButton activeGame : activeGames) {
			Game.mainWindow.remove(activeGame);
			activeGame = null;
		}
		
		Game.mainWindow.remove(userTurn);
		Game.mainWindow.remove(opponentTurn);
		
		userTurn = null;
		opponentTurn = null;
	}

	public void exit() {
		exitParentGUI();
		
		Game.mainWindow.remove(createGame);
		Game.mainWindow.remove(spectatorGames);
		Game.mainWindow.remove(refresh);
		
		createGame = null;
		spectatorGames = null;
		refresh = null;
		
		removeAndNullActiveGames();
		activeGames = null;
		activeGamesUserTurn = null;
		
		Game.mainWindow.repaint();
	}
}
