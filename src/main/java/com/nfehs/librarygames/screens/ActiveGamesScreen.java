package com.nfehs.librarygames.screens;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

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
	private JLabel		finished;
	private JLabel		userTurn;
	private JLabel		opponentTurn;
	
	private JScrollPane	finishedGamesTab;
	private JButton[]	finishedGames;
	
	private JScrollPane	activeGamesUserTab;
	private JButton[]	activeGamesUserTurn;
	
	private JScrollPane	activeGamesTab;
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
				Game.getActiveGames();
			}
		});
		finishedGamesTab = new JScrollPane();
		activeGamesUserTab = new JScrollPane();
		activeGamesTab = new JScrollPane();
		
		finished = new JLabel();
		userTurn = new JLabel();
		opponentTurn = new JLabel();
		
		finishedGames = new JButton[0];
		activeGames = new JButton[0];
		activeGamesUserTurn = new JButton[0];
	}
	
	/**
	 * This method is called once the client has received the game data from the server
	 */
	public void loadActiveGames() {
		// remove current active games
		removeAndNullActiveGames();
		
		
		
		// finished games
		finished = new JLabel("Finished Games:");
		Game.mainWindow.add(finished);
		finished.setBounds ((int) Game.screenSize.getWidth() / 2 - 500, 
							(int) Game.screenSize.getHeight() / 10 + 200, 300, 30);
		
		finishedGamesTab.setBounds ((int) Game.screenSize.getWidth() / 2 - 500,
									(int) Game.screenSize.getHeight() / 10 + 230, 300,
									(int) Game.screenSize.getHeight() - ((int) Game.screenSize.getHeight() / 10 + 230) - 50);
		Game.mainWindow.add(finishedGamesTab);
		finishedGamesTab.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		finishedGamesTab.setLayout(null);
		
		finishedGames = new JButton[Game.getPlayer().getFinishedBoardGames().length];
		for (int i = 0; i < finishedGames.length; i++) {
			finishedGames[i] = new JButton(Game.getPlayer().getFinishedBoardGames()[i].split("~")[1]);
			finishedGamesTab.add(finishedGames[i]);
			finishedGames[i].setBounds(5, 5 + i*50, finishedGamesTab.getWidth() - 10, 30);
			finishedGames[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("An finished game clicked: " + ((JButton) e.getSource()).getText());
					
					// get the element number of button to find game key
					int elemNumber = -1;
					for (int i = 0; i < finishedGames.length; i++)
						if (finishedGames[i].equals(e.getSource()))
							elemNumber = i;
					
					// send get board request to server
					Game.getBoard(Game.getPlayer().getFinishedBoardGames()[elemNumber].split("~")[0],
							Integer.parseInt(Game.getPlayer().getFinishedBoardGames()[elemNumber].split("~")[3]));
				}
			});
		}
		
		
		
		// add active games user turn
		userTurn = new JLabel("Your turn:");
		Game.mainWindow.add(userTurn);
		userTurn.setBounds ((int) Game.screenSize.getWidth() / 2 - 150, 
							(int) Game.screenSize.getHeight() / 10 + 200, 300, 30);
		
		activeGamesUserTab.setBounds ((int) Game.screenSize.getWidth() / 2 - 150,
									(int) Game.screenSize.getHeight() / 10 + 230, 300,
									(int) Game.screenSize.getHeight() - ((int) Game.screenSize.getHeight() / 10 + 230) - 50);
		Game.mainWindow.add(activeGamesUserTab);
		activeGamesUserTab.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		activeGamesUserTab.setLayout(null);
		
		activeGamesUserTurn = new JButton[Game.getPlayer().getYourTurnBoardGames().length];
		for (int i = 0; i < activeGamesUserTurn.length; i++) {
			activeGamesUserTurn[i] = new JButton(Game.getPlayer().getYourTurnBoardGames()[i].split("~")[1]);
			activeGamesUserTab.add(activeGamesUserTurn[i]);
			activeGamesUserTurn[i].setBounds(5, 5 + i*50, activeGamesUserTab.getWidth() - 10, 30);
			activeGamesUserTurn[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("An active game clicked: " + ((JButton) e.getSource()).getText());
					
					// get the element number of button to find game key
					int elemNumber = -1;
					for (int i = 0; i < activeGamesUserTurn.length; i++)
						if (activeGamesUserTurn[i].equals(e.getSource()))
							elemNumber = i;
					
					// send get board request to server
					Game.getBoard(Game.getPlayer().getYourTurnBoardGames()[elemNumber].split("~")[0],
							Integer.parseInt(Game.getPlayer().getYourTurnBoardGames()[elemNumber].split("~")[3]));
				}
			});
		}
		
		
		
		// add new active games, opponent's turn games
		opponentTurn = new JLabel("Opponent's turn:");
		Game.mainWindow.add(opponentTurn);
		opponentTurn.setBounds ((int) Game.screenSize.getWidth() / 2 + 200, 
							(int) Game.screenSize.getHeight() / 10 + 200, 300, 30);
		
		activeGamesTab.setBounds   ((int) Game.screenSize.getWidth() / 2 + 200,
									(int) Game.screenSize.getHeight() / 10 + 230, 300,
									(int) Game.screenSize.getHeight() - ((int) Game.screenSize.getHeight() / 10 + 230) - 50);
		Game.mainWindow.add(activeGamesTab);
		activeGamesTab.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		activeGamesTab.setLayout(null);
		
		activeGames = new JButton[Game.getPlayer().getOpponentTurnBoardGames().length];
		for (int i = 0; i < activeGames.length; i++) {
			activeGames[i] = new JButton(Game.getPlayer().getOpponentTurnBoardGames()[i].split("~")[1]);
			activeGamesTab.add(activeGames[i]);
			activeGames[i].setBounds(5, 5 + i*50, activeGamesUserTab.getWidth() - 10, 30);
			activeGames[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("An active game clicked: " + ((JButton) e.getSource()).getText());
					
					// get the element number of button to find game key
					int elemNumber = -1;
					for (int i = 0; i < activeGames.length; i++)
						if (activeGames[i].equals(e.getSource()))
							elemNumber = i;
					
					// send get board request to server
					Game.getBoard(Game.getPlayer().getOpponentTurnBoardGames()[elemNumber].split("~")[0],
						Integer.parseInt(Game.getPlayer().getOpponentTurnBoardGames()[elemNumber].split("~")[3]));
				}
			});
		}
		Game.mainWindow.repaint();
	}
	
	/**
	 * Removes all active games from main window and sets the null
	 */
	private void removeAndNullActiveGames() {
		for (JButton finishedGame : finishedGames) {
			activeGamesUserTab.remove(finishedGame);
			finishedGame = null;
		}
		for (JButton activeGame : activeGamesUserTurn) {
			activeGamesUserTab.remove(activeGame);
			activeGame = null;
		}
		for (JButton activeGame : activeGames) {
			activeGamesTab.remove(activeGame);
			activeGame = null;
		}
		Game.mainWindow.remove(finished);
		Game.mainWindow.remove(userTurn);
		Game.mainWindow.remove(opponentTurn);
		
		finished = null;
		userTurn = null;
		opponentTurn = null;
	}

	public void exit() {
		exitParentGUI();
		
		removeAndNullActiveGames();
		
		Game.mainWindow.remove(createGame);
		Game.mainWindow.remove(spectatorGames);
		Game.mainWindow.remove(refresh);
		Game.mainWindow.remove(finishedGamesTab);
		Game.mainWindow.remove(activeGamesUserTab);
		Game.mainWindow.remove(activeGamesTab);
		
		createGame = null;
		spectatorGames = null;
		refresh = null;
		finishedGamesTab = null;
		activeGamesUserTab = null;
		activeGamesTab = null;
		
		Game.mainWindow.repaint();
	}
}
