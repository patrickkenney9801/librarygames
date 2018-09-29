package com.nfehs.librarygames.screens;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import com.nfehs.librarygames.Game;
import com.nfehs.librarygames.games.BoardGame;

/**
 * This is the parent class for all screens
 * @author Patrick Kenney and Syed Quadri
 * @date 8/11/2018
 */

public abstract class Screen {
	public JLabel loggedUser;
	public JButton logout;
	private JButton notification;
	private BoardGame latestBoardGameUpdate;
	
	private Thread alertUser;
	
	public Screen(boolean isNotLogged) {
		if (!isNotLogged) {
			loggedUser = new JLabel("Logged in as: " + Game.getPlayer().getUsername());
			Game.mainWindow.add(loggedUser);
			loggedUser.setBounds(0, 0, 300, 30);
			
			logout = new JButton("Logout");
			Game.mainWindow.add(logout);
			logout.setBounds((int) Game.screenSize.getWidth() - 125, 5, 100, 30);
			logout.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("Logout clicked");
					Game.logout();
					Game.openLoginScreen();
				}
			});
			
			notification = new JButton();
			notification.setBounds(200, 5, (int) Game.screenSize.getWidth() - 400, 30);
			notification.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Game.setBoardGame(getLatestBoardGameUpdate());
					Game.openGameScreen();
					Game.sendOnGameUpdate();
				}
			});
			
			alertUser = new Thread (new Runnable () {
				public void run() {
					try {
						Thread.sleep(5000);
						if (notification != null)
							Game.mainWindow.remove(notification);
						if (Game.screen instanceof GameScreen)
							Game.mainWindow.add(((GameScreen) Game.screen).title);
						Game.mainWindow.repaint();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
	
	/**
	 * Creates a link to the latest game that was just updated
	 * @param latestBoardGame
	 */
	public void notifyUser(BoardGame latestBoardGame) {
		if (Game.screen instanceof GameScreen)
			Game.mainWindow.remove(((GameScreen) Game.screen).title);
		Game.mainWindow.remove(notification);
		setLatestBoardGameUpdate(latestBoardGame);
		notification.setText("Action made: " + getLatestBoardGameUpdate().getGameTitle());
		Game.mainWindow.add(notification);
		
		alertUser.start();
		Game.mainWindow.repaint();
	}
	
	/**
	 * Appends a message in a certain color to a JTextPane
	 * @param tp
	 * @param msg
	 * @param c
	 */
	protected void appendText(JTextPane tp, String msg, Color c) {
		// set color for text
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet as = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
		as = sc.addAttribute(as, StyleConstants.FontFamily, "Serif");
		as = sc.addAttribute(as, StyleConstants.FontSize, 15);
		as = sc.addAttribute(as, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
		
		tp.setEditable(true);
		tp.setCaretPosition(tp.getDocument().getLength());
		tp.setCharacterAttributes(as, false);
		tp.replaceSelection(msg);
		tp.setEditable(false);
	}
	
	public void exit() {
		Game.mainWindow.removeAll();
		Game.mainWindow.repaint();
	}
	
	protected abstract void setPositions();
	public void resize() {
		resizeParentGUI();
		setPositions();
		Game.mainWindow.repaint();
	}
	private void resizeParentGUI() {
		if (logout != null)
			logout.setBounds((int) Game.screenSize.getWidth() - 125, 5, 100, 30);
		if (notification != null)
			notification.setBounds(200, 5, (int) Game.screenSize.getWidth() - 400, 30);
	}

	/**
	 * @return the latestBoardGameUpdate
	 */
	public BoardGame getLatestBoardGameUpdate() {
		return latestBoardGameUpdate;
	}

	/**
	 * @param latestBoardGameUpdate the latestBoardGameUpdate to set
	 */
	public void setLatestBoardGameUpdate(BoardGame latestBoardGameUpdate) {
		this.latestBoardGameUpdate = latestBoardGameUpdate;
	}
}
