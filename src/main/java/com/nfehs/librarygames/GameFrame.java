package com.nfehs.librarygames;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.UIManager;

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
			@Override
			public void windowClosing(WindowEvent e) {
				System.out.println("Closing window");
				Game.logout();
			}
		});
		/*
		Game.window.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Game.screenSize.setSize(Game.window.getWidth(), Game.window.getHeight());
				Game.refresh();
			}
		});*/
		
		Game.mainWindow = Game.window.getContentPane();
		Game.mainWindow.setLayout(null);
		Game.mainWindow.setBackground(background);
		
		GameScreen.setBoardSize((Game.screenSize.getHeight() * 4 / 5));
		
		// set UI presets
		UIManager.put("Label.foreground", Color.WHITE);
		UIManager.put("Label.background", background);
		UIManager.put("TextArea.foreground", Color.WHITE);
		UIManager.put("TextArea.background", textBackground);
		UIManager.put("TextField.foreground", Color.WHITE);
		UIManager.put("TextField.background", textBackground);
		UIManager.put("PasswordField.foreground", Color.WHITE);
		UIManager.put("PasswordField.background", textBackground);
		UIManager.put("ComboBox.foreground", Color.WHITE);
		UIManager.put("ComboBox.background", textBackground);
		UIManager.put("RadioButton.foreground", Color.WHITE);
		UIManager.put("RadioButton.background", background);
		UIManager.put("Panel.background", textBackground);
		
		// load images here
		Tile.loadImages();
		Piece.loadImages();
	}
}