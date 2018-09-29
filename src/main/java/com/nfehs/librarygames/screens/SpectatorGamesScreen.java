package com.nfehs.librarygames.screens;

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
 * This class handles the user browsing active games that are not their own
 * It also directs players back to ActiveGamesScreen
 * @author Patrick Kenney and Syed Quadri
 * @date 8/22/2018
 */

public class SpectatorGamesScreen extends Screen {
	private JButton		refresh;
	private JButton		back;
	private JLabel		spectate;
	
	private JPanel		spectatorGamesPanel;
	private JScrollPane	spectatorGamesTab;
	private JButton[]	spectatorGames;

	public SpectatorGamesScreen() {
		super(false);

		refresh = new JButton("REFRESH");
		Game.mainWindow.add(refresh);
		refresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// goes back to active games list
				System.out.println("Refresh clicked from spectator games");
				Game.getSpectatorGames();
			}
		});

		back = new JButton("BACK");
		Game.mainWindow.add(back);
		back.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// goes back to active games list
				System.out.println("Back clicked from spectator games");
				Game.openActiveGamesScreen();
			}
		});
		
		spectate = new JLabel("Spectate:");
		Game.mainWindow.add(spectate);
		
		spectatorGamesPanel = new JPanel();
		spectatorGamesPanel.setLayout(null);
		spectatorGamesPanel.setBackground(GameFrame.background);
		spectatorGamesTab = new JScrollPane(spectatorGamesPanel);
		spectatorGamesTab.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		spectatorGamesTab.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		spectatorGamesTab.setAutoscrolls(true);
		spectatorGamesTab.getVerticalScrollBar().setUnitIncrement(15);
		Game.mainWindow.add(spectatorGamesTab);
		
		spectatorGames = new JButton[0];

		setPositions();
		Game.mainWindow.repaint();
	}
	
	/**
	 * Sets the positions for all items on screen
	 */
	protected void setPositions() {
		refresh.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 5 - 60, 150, 30);
		back.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 5 * 4 + 60, 150, 30);
		spectate.setBounds ((int) Game.screenSize.getWidth() / 2 - 150, 
							(int) Game.screenSize.getHeight() / 5 - 30, 300, 30);
		spectatorGamesTab.setBounds ((int) Game.screenSize.getWidth() / 2 - 150,
				(int) Game.screenSize.getHeight() / 5, 300,
				((int) Game.screenSize.getHeight() * 3 / 5) - 50);
	}
	
	/**
	 * This method is called once the client has received the game data from the server
	 */
	public void loadSpectatorGames() {
		// remove current spectator games
		spectatorGamesPanel.removeAll();
		
		// finished games
		spectatorGames = new JButton[Game.getPlayer().getSpectatorBoardGames().length];
		spectatorGamesPanel.setPreferredSize(new Dimension(285, spectatorGames.length*50 - 10));
		spectatorGamesPanel.setBounds(5, 5, 285, spectatorGames.length*50 - 10);
		
		for (int i = 0; i < spectatorGames.length; i++) {
			spectatorGames[i] = new JButton(Game.getPlayer().getSpectatorBoardGames()[i].split("~")[1]);
			spectatorGames[i].setBounds(5, 5 + i*50, spectatorGamesPanel.getWidth() - 10, 30);
			spectatorGamesPanel.add(spectatorGames[i]);
			spectatorGames[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("An finished game clicked: " + ((JButton) e.getSource()).getText());
					
					// get the element number of button to find game key
					int elemNumber = -1;
					for (int i = 0; i < spectatorGames.length; i++)
						if (spectatorGames[i].equals(e.getSource()))
							elemNumber = i;
					if (elemNumber == -1)
						return;
					
					// send get board request to server
					Game.getBoard(Game.getPlayer().getSpectatorBoardGames()[elemNumber].split("~")[0],
							Integer.parseInt(Game.getPlayer().getSpectatorBoardGames()[elemNumber].split("~")[3]));
				}
			});
		}
		spectatorGamesTab.revalidate();
		Game.mainWindow.repaint();
	}

	@Override
	public void exit() {
		exitParentGUI();
		
		Game.mainWindow.remove(refresh);
		Game.mainWindow.remove(back);
		Game.mainWindow.remove(spectatorGamesTab);
		Game.mainWindow.remove(spectate);
		
		spectate = null;
		refresh = null;
		back = null;
		spectatorGamesTab = null;
		
		Game.mainWindow.repaint();
	}
}
