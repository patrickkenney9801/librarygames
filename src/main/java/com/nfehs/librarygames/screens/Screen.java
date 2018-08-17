package com.nfehs.librarygames.screens;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import com.nfehs.librarygames.Game;
import com.nfehs.librarygames.GameFrame;

/**
 * This is the parent class for all screens
 * @author Patrick Kenney and Syed Quadri
 * @date 8/11/2018
 */

public abstract class Screen {
	public static JLabel loggedUser;
	public static JButton logout;
	
	public Screen(boolean isNotLogged) {
		if (!isNotLogged) {
			loggedUser = new JLabel("Logged in as: " + Game.getPlayer().getUsername());
			Game.mainWindow.add(loggedUser);
			loggedUser.setBounds(0, 0, 300, 30);
			
			logout = new JButton("Logout");
			Game.mainWindow.add(logout);
			logout.setBounds((int) Game.screenSize.getWidth() - 110, 5, 100, 30);
			logout.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("Logout clicked");
					Game.logout();
					Game.openLoginScreen();
				}
			});
		}
	}
	
	public abstract void exit();
	protected void exitParentGUI() {
		Game.mainWindow.remove(loggedUser);
		Game.mainWindow.remove(logout);
		
		loggedUser = null;
		logout = null;
	}
}
