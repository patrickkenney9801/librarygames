package com.nfehs.librarygames.screens;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

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
	private JButton[]	activeGames;
	
	public ActiveGamesScreen() {
		createGame = new JButton("NEW GAME");
		Game.mainWindow.add(createGame);
		createGame.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 10, 150, 30);
		createGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Create game clicked");
				// TODO
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
				// TODO
			}
		});
		
		activeGames = new JButton[0];
	}
	
	/**
	 * This method is called once the client has received the game data from the server
	 */
	public void loadActiveGames() {
		// remove current active games
		removeAndNullActiveGames();
		
		// add new active games
		activeGames = new JButton[Game.getPlayer().boardGames.size()];
		for (int i = 0; i < activeGames.length; i++) {
			activeGames[i] = new JButton(Game.getPlayer().boardGames.get(i).getGameTitle());
			activeGames[i].setBounds((int) Game.screenSize.getWidth() / 2 - 75, 
									 (int) Game.screenSize.getHeight() / 10 + 200 + i*50, 150, 30);
			activeGames[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("An active game clicked: " + ((JButton) e.getSource()).getText());
					// TODO
				}
			});
		}
	}
	
	/**
	 * Removes all active games from main window and sets the null
	 */
	private void removeAndNullActiveGames() {
		for (JButton activeGame : activeGames) {
			Game.mainWindow.remove(activeGame);
			activeGame = null;
		}
	}

	public void exit() {
		Game.mainWindow.remove(createGame);
		Game.mainWindow.remove(spectatorGames);
		
		createGame = null;
		spectatorGames = null;
		
		removeAndNullActiveGames();
		activeGames = null;
		
		Game.mainWindow.repaint();
	}
}
