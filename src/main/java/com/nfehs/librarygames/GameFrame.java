package com.nfehs.librarygames;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import com.nfehs.librarygames.games.Piece;
import com.nfehs.librarygames.games.Tile;
import com.nfehs.librarygames.screens.GameScreen;

/**
 * This handles the setup and maintenance of the game window
 * @author Patrick Kenney, Syed Quadri
 * @date 6/13/2018
 */

public class GameFrame extends JFrame {
	public static final Color background = new Color(25, 25, 25);
	public static final Color textBackground = new Color(50, 50, 50);

	public GameFrame() {
		Game.screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Game.screenSize.setSize((int) Game.screenSize.getWidth() - 125, (int) Game.screenSize.getHeight() - 100);
		
		Game.window = new JFrame("Library Games");
		Game.window.setVisible(true);
		Game.window.setSize((int) Game.screenSize.getWidth(), (int) Game.screenSize.getHeight());
		Game.window.setDefaultCloseOperation(EXIT_ON_CLOSE);
		Game.window.setResizable(false);
		Game.window.setLocationRelativeTo(null);
		Game.window.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.out.println("Closing window");
				Game.logout();
			}
		});
		
		Game.mainWindow = Game.window.getContentPane();
		Game.mainWindow.setLayout(null);
		Game.mainWindow.setBackground(background);
		
		GameScreen.setBoardSize((Game.screenSize.getHeight() * 4 / 5));
		
		// load images here
		Tile.loadImages();
		Piece.loadImages();
	}
}