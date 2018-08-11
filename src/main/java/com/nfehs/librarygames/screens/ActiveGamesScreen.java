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
	private JButton[]	activeGames;
	
	public ActiveGamesScreen() {
		createGame = new JButton("NEW GAME");
		Game.mainWindow.add(createGame);
		createGame.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 10, 150, 30);
		createGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Create game clicked");
			}
		});
	}
	
	public void exit() {
		Game.mainWindow.remove(createGame);
		
		createGame = null;
		
		Game.mainWindow.repaint();
	}
}
