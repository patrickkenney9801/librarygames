package com.nfehs.librarygames.screens;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.nfehs.librarygames.Game;
import com.nfehs.librarygames.games.BoardGame;
import com.nfehs.librarygames.games.go.Go;

/**
 * Handles the user creating an offline game,
 * @author Patrick Kenney and Syed Quadri
 * @date 9/3/2018
 */

public class CreateOfflineGameScreen extends Screen {
  private final String[] choices = {"Go 9x9", "Go 13x13", "Go 19x19"};
  private JComboBox<String> gameChoices;

  private JLabel p1;
  private JLabel p2;
  private JTextField player1;
  private JTextField player2;

  private JButton createOfflineGame;
  private JButton back;

  public CreateOfflineGameScreen() {
    super(true);

    gameChoices = new JComboBox<String>(choices);
    Game.mainWindow.add(gameChoices);
    gameChoices.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 300, 150, 30);
    gameChoices.setSelectedIndex(0);


    p1 = new JLabel("Player 1:");
    Game.mainWindow.add(p1);
    p1.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 250, 150, 30);

    player1 = new JTextField();
    Game.mainWindow.add(player1);
    player1.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 220, 150, 30);
    player1.requestFocusInWindow();
    player1.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {}
      public void keyReleased(KeyEvent e) {}
      public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == '\n')
          player2.requestFocus();
      }
    });

    p2 = new JLabel("Player 2:");
    Game.mainWindow.add(p2);
    p2.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 170, 150, 30);

    player2 = new JTextField();
    Game.mainWindow.add(player2);
    player2.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 140, 150, 30);
    player2.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {}
      public void keyReleased(KeyEvent e) {}
      public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == '\n')
          createOfflineGame.requestFocus();
      }
    });

    createOfflineGame = new JButton("CREATE GAME");
    Game.mainWindow.add(createOfflineGame);
    createOfflineGame.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2 - 50, 150, 30);
    createOfflineGame.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // create a new offline game
        System.out.println("Create offline game clicked");

        // if the usernames are the same, or left empty, set to default values
        String player1Username = player1.getText();
        String player2Username = player2.getText();

        if (player1Username.length() < 1)
          player1Username = "Player 1";
        if (player2Username.length() < 1)
          player2Username = "Player 2";
        if (player1Username.equals(player2Username)) {
          player1Username = "Player 1";
          player2Username = "Player 2";
        }

        // if the no gameType is selected or there is some other error, exit and send message to screen
        int type = gameChoices.getSelectedIndex();
        if (type < 0)
          type = 0;
        BoardGame.GameType gameType = BoardGame.GameType.values()[type + 1];
        int moves = 0;
        int penultMove = -5;
        int lastMove = -5;
        int winner = 0;
        boolean player1Online = true;
        boolean player2Online = true;
        String board = BoardGame.createNewBoard(gameType);

        // create an offline board game and open game screen
        switch (gameType) {
          case GO9x9:
          case GO13x13:
          case GO19x19:
                          Game.setBoardGame(new Go("GameKey", gameType, player1Username, player2Username,
                                                    moves, penultMove, lastMove, winner, player1Online, player2Online, board,
                                                    0, 0, 0, 0));
                          break;
          default:        // handle invalid game type
                          System.out.println("ERROR INVALID GAME TYPE");
                          return;
        }
        Game.openGameScreen();
      }
    });

    back = new JButton("BACK");
    Game.mainWindow.add(back);
    back.setBounds((int) Game.screenSize.getWidth() / 2 - 75, (int) Game.screenSize.getHeight() / 2, 150, 30);
    back.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // goes back to login screen
        System.out.println("Back clicked from create offline game");
        Game.openLoginScreen();
      }
    });
    Game.mainWindow.repaint();
  }

  @Override
  public void exit() {
    Game.mainWindow.remove(gameChoices);
    Game.mainWindow.remove(p1);
    Game.mainWindow.remove(player1);
    Game.mainWindow.remove(p2);
    Game.mainWindow.remove(player2);
    Game.mainWindow.remove(createOfflineGame);
    Game.mainWindow.remove(back);

    gameChoices = null;
    p1 = null;
    player1 = null;
    p2 = null;
    player2 = null;
    createOfflineGame = null;
    back = null;

    Game.mainWindow.repaint();
  }
}
