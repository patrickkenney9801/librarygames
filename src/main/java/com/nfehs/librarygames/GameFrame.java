package com.nfehs.librarygames;

import java.awt.Color;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * This handles the setup and maintenance of the game window
 * @author Patrick Kenney, Syed Quadri
 * @date 6/13/2018
 */

public class GameFrame extends JFrame {
	public static final Color background = new Color(25, 25, 25);
	public static final Color textBackground = new Color(50, 50, 50);
	
	public static JLabel loggedUser;

	public GameFrame() {
		Game.screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Game.screenSize.setSize((int) Game.screenSize.getWidth() - 125, (int) Game.screenSize.getHeight() - 100);
		
		Game.window = new JFrame("Library Games");
		Game.window.setVisible(true);
		Game.window.setSize((int) Game.screenSize.getWidth(), (int) Game.screenSize.getHeight());
		Game.window.setDefaultCloseOperation(EXIT_ON_CLOSE);
		Game.window.setResizable(false);
		Game.window.setLocationRelativeTo(null);
		
		Game.mainWindow = Game.window.getContentPane();
		Game.mainWindow.setLayout(null);
		Game.mainWindow.setBackground(background);
		
		loggedUser = new JLabel();
		loggedUser.setBounds(0, 0, 300, 30);
		loggedUser.setForeground(Color.WHITE);
		loggedUser.setBackground(background);
		Game.mainWindow.add(loggedUser);
	}
}