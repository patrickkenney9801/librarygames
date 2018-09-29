package com.nfehs.librarygames.screens;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.nfehs.librarygames.Game;
import com.nfehs.librarygames.GameFrame;

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
	
	private JPanel		finishedGamesPanel;
	private JScrollPane	finishedGamesTab;
	private JButton[]	finishedGames;

	private JPanel		activeGamesUserPanel;
	private JScrollPane	activeGamesUserTab;
	private JButton[]	activeGamesUserTurn;

	private JPanel		activeGamesPanel;
	private JScrollPane	activeGamesTab;
	private JButton[]	activeGames;
	
	public ActiveGamesScreen() {
		super(false);
		
		createGame = new JButton("NEW GAME");
		Game.mainWindow.add(createGame);
		createGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Create game clicked");
				Game.openCreateGameScreen();
			}
		});
		
		spectatorGames = new JButton("SPECTATE GAMES");
		Game.mainWindow.add(spectatorGames);
		spectatorGames.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Spectator games clicked");
				Game.openSpectatorGamesScreen();
			}
		});
		
		refresh = new JButton("REFRESH");
		Game.mainWindow.add(refresh);
		refresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Refresh clicked");
				Game.getActiveGames();
			}
		});
		
		finished = new JLabel("Finished Games:");
		Game.mainWindow.add(finished);
		
		finishedGamesPanel = new JPanel();
		finishedGamesPanel.setLayout(null);
		finishedGamesPanel.setBackground(GameFrame.background);
		finishedGamesTab = new JScrollPane(finishedGamesPanel);
		finishedGamesTab.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		finishedGamesTab.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		finishedGamesTab.setAutoscrolls(true);
		finishedGamesTab.getVerticalScrollBar().setUnitIncrement(15);
		Game.mainWindow.add(finishedGamesTab);

		userTurn = new JLabel("Your turn:");
		Game.mainWindow.add(userTurn);
		
		activeGamesUserPanel = new JPanel();
		activeGamesUserPanel.setLayout(null);
		activeGamesUserPanel.setBackground(GameFrame.background);
		activeGamesUserTab = new JScrollPane(activeGamesUserPanel);
		activeGamesUserTab.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		activeGamesUserTab.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		activeGamesUserTab.setAutoscrolls(true);
		activeGamesUserTab.getVerticalScrollBar().setUnitIncrement(15);
		Game.mainWindow.add(activeGamesUserTab);

		opponentTurn = new JLabel("Opponent's turn:");
		Game.mainWindow.add(opponentTurn);
		
		activeGamesPanel = new JPanel();
		activeGamesPanel.setLayout(null);
		activeGamesPanel.setBackground(GameFrame.background);
		activeGamesTab = new JScrollPane(activeGamesPanel);
		activeGamesTab.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		activeGamesTab.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		activeGamesTab.setAutoscrolls(true);
		activeGamesTab.getVerticalScrollBar().setUnitIncrement(15);
		Game.mainWindow.add(activeGamesTab);

		setPositions();
		Game.mainWindow.repaint();
	}
	
	/**
	 * Sets the positions for all items on screen
	 */
	protected void setPositions() {
		createGame.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 10, 150, 30);
		spectatorGames.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 10 + 50, 150, 30);
		refresh.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 10 + 160, 150, 30);
		finished.setBounds ((int) Game.screenSize.getWidth() / 2 - 500, 
							(int) Game.screenSize.getHeight() / 10 + 200, 300, 30);
		finishedGamesTab.setBounds ((int) Game.screenSize.getWidth() / 2 - 500,
				(int) Game.screenSize.getHeight() / 10 + 230, 300,
				(int) Game.screenSize.getHeight() - ((int) Game.screenSize.getHeight() / 10 + 230) - 50);
		userTurn.setBounds ((int) Game.screenSize.getWidth() / 2 - 150, 
				(int) Game.screenSize.getHeight() / 10 + 200, 300, 30);
		activeGamesUserTab.setBounds ((int) Game.screenSize.getWidth() / 2 - 150,
				(int) Game.screenSize.getHeight() / 10 + 230, 300,
				(int) Game.screenSize.getHeight() - ((int) Game.screenSize.getHeight() / 10 + 230) - 50);
		opponentTurn.setBounds ((int) Game.screenSize.getWidth() / 2 + 200, 
				(int) Game.screenSize.getHeight() / 10 + 200, 300, 30);
		activeGamesTab.setBounds   ((int) Game.screenSize.getWidth() / 2 + 200,
									(int) Game.screenSize.getHeight() / 10 + 230, 300,
									(int) Game.screenSize.getHeight() - ((int) Game.screenSize.getHeight() / 10 + 230) - 50);
	}
	
	/**
	 * This method is called once the client has received the game data from the server
	 */
	public void loadActiveGames() {
		// remove current active games
		finishedGamesPanel.removeAll();
		activeGamesUserPanel.removeAll();
		activeGamesPanel.removeAll();
		
		// finished games
		finishedGames = new JButton[Game.getPlayer().getFinishedBoardGames().length];
		finishedGamesPanel.setPreferredSize(new Dimension(285, finishedGames.length*50 - 10));
		finishedGamesPanel.setBounds(5, 5, 285, finishedGames.length*50 - 10);
		
		for (int i = 0; i < finishedGames.length; i++) {
			finishedGames[i] = new JButton(Game.getPlayer().getFinishedBoardGames()[i].split("~")[1]);
			finishedGames[i].setBounds(5, 5 + i*50, finishedGamesPanel.getWidth() - 10, 30);
			finishedGamesPanel.add(finishedGames[i]);
			finishedGames[i].setForeground(Boolean.parseBoolean(Game.getPlayer().getFinishedBoardGames()[i].split("~")[4]) ? new Color(0, 150, 0) : Color.RED);
			finishedGames[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("An finished game clicked: " + ((JButton) e.getSource()).getText());
					
					// get the element number of button to find game key
					int elemNumber = -1;
					for (int i = 0; i < finishedGames.length; i++)
						if (finishedGames[i].equals(e.getSource()))
							elemNumber = i;
					if (elemNumber == -1)
						return;
					
					// send get board request to server
					Game.getBoard(Game.getPlayer().getFinishedBoardGames()[elemNumber].split("~")[0],
							Integer.parseInt(Game.getPlayer().getFinishedBoardGames()[elemNumber].split("~")[3]));
				}
			});
		}
		
		
		
		// add active games user turn
		activeGamesUserTurn = new JButton[Game.getPlayer().getYourTurnBoardGames().length];
		activeGamesUserPanel.setPreferredSize(new Dimension(285, activeGamesUserTurn.length*50 - 10));
		activeGamesUserPanel.setBounds(5, 5, 285, activeGamesUserTurn.length*50 - 10);
		
		for (int i = 0; i < activeGamesUserTurn.length; i++) {
			activeGamesUserTurn[i] = new JButton(Game.getPlayer().getYourTurnBoardGames()[i].split("~")[1]);
			activeGamesUserPanel.add(activeGamesUserTurn[i]);
			activeGamesUserTurn[i].setBounds(5, 5 + i*50, activeGamesUserPanel.getWidth() - 10, 30);
			activeGamesUserTurn[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("An active game clicked: " + ((JButton) e.getSource()).getText());
					
					// get the element number of button to find game key
					int elemNumber = -1;
					for (int i = 0; i < activeGamesUserTurn.length; i++)
						if (activeGamesUserTurn[i].equals(e.getSource()))
							elemNumber = i;
					if (elemNumber == -1)
						return;
					
					// send get board request to server
					Game.getBoard(Game.getPlayer().getYourTurnBoardGames()[elemNumber].split("~")[0],
							Integer.parseInt(Game.getPlayer().getYourTurnBoardGames()[elemNumber].split("~")[3]));
				}
			});
		}
		
		
		
		// add new active games, opponent's turn games
		activeGames = new JButton[Game.getPlayer().getOpponentTurnBoardGames().length];
		activeGamesPanel.setPreferredSize(new Dimension(285, activeGames.length*50 - 10));
		activeGamesPanel.setBounds(5, 5, 285, activeGames.length*50 - 10);
		
		for (int i = 0; i < activeGames.length; i++) {
			activeGames[i] = new JButton(Game.getPlayer().getOpponentTurnBoardGames()[i].split("~")[1]);
			activeGamesPanel.add(activeGames[i]);
			activeGames[i].setBounds(5, 5 + i*50, activeGamesUserPanel.getWidth() - 10, 30);
			activeGames[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("An active game clicked: " + ((JButton) e.getSource()).getText());
					
					// get the element number of button to find game key
					int elemNumber = -1;
					for (int i = 0; i < activeGames.length; i++)
						if (activeGames[i].equals(e.getSource()))
							elemNumber = i;
					if (elemNumber == -1)
						return;
					
					// send get board request to server
					Game.getBoard(Game.getPlayer().getOpponentTurnBoardGames()[elemNumber].split("~")[0],
						Integer.parseInt(Game.getPlayer().getOpponentTurnBoardGames()[elemNumber].split("~")[3]));
				}
			});
		}
		finishedGamesTab.revalidate();
		activeGamesUserTab.revalidate();
		activeGamesTab.revalidate();
		Game.mainWindow.repaint();
	}

	public void exit() {
		exitParentGUI();
		
		Game.mainWindow.remove(createGame);
		Game.mainWindow.remove(spectatorGames);
		Game.mainWindow.remove(refresh);
		Game.mainWindow.remove(finishedGamesTab);
		Game.mainWindow.remove(activeGamesUserTab);
		Game.mainWindow.remove(activeGamesTab);
		Game.mainWindow.remove(finished);
		Game.mainWindow.remove(userTurn);
		Game.mainWindow.remove(opponentTurn);
		Game.mainWindow.remove(finishedGamesTab);
		Game.mainWindow.remove(activeGamesTab);
		Game.mainWindow.remove(activeGamesUserTab);
		
		createGame = null;
		spectatorGames = null;
		refresh = null;
		finishedGamesTab = null;
		activeGamesUserTab = null;
		activeGamesTab = null;
		finished = null;
		userTurn = null;
		opponentTurn = null;
		
		Game.mainWindow.repaint();
	}
}
