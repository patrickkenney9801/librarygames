package com.nfehs.librarygames.screens;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import com.nfehs.librarygames.Game;
import com.nfehs.librarygames.GameFrame;

/**
 * This class handles the game being played
 * 
 * @author Patrick Kenney, Syed Quadri
 * @date 6/14/2018
 */

public class GameScreen extends Screen {
	private JButton back;

	public GameScreen() {
		super(false);
		
		back = new JButton("RETURN");
		Game.mainWindow.add(back);
		back.setBounds(0, 40, 150, 30);
		back.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("RETURN TO GAMES LIST CLICKED");
				Game.openActiveGamesScreen();
			}
		});
	}

	@Override
	public void exit() {
		Game.mainWindow.remove(back);
		
		back = null;
		
		Game.mainWindow.repaint();
	}

}
