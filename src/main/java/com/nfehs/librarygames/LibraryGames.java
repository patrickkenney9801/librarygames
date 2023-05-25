package com.nfehs.librarygames;

import javax.swing.UIManager;

/**
 * This file launches the game loop or server depending on env var LIBRARY_GAMES_SERVER
 * @author Patrick Kenney, Syed Quadri
 * @date 6/13/2018
 *
 */

public class LibraryGames {
  public static Game game;

  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

      // initialize game and gameframe
      game = new Game();
      Game.gameFrame = new GameFrame();

      // go to login screen
      Game.openLoginScreen();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
