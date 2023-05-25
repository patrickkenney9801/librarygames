package com.nfehs.librarygames.screens;

import com.nfehs.librarygames.Game;
import com.nfehs.librarygames.games.BoardGame.GameType;
import com.nfehs.librarygames.games.Piece;
import com.nfehs.librarygames.games.Tile;
import com.nfehs.librarygames.games.go.pieces.Stone;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

/**
 * This class handles the game being played
 *
 * @author Patrick Kenney, Syed Quadri
 * @date 6/14/2018
 */
public class GameScreen extends Screen {
  private static final double imageTileSize = 100;
  private static double boardSize;
  private static int infoTextSize;

  private double scale;
  private double screenTileSize;
  private int topLeftX;
  private int topLeftY;

  protected JLabel title;
  private JButton back;
  private JButton pass;
  private JButton resign;

  private JLayeredPane pane;
  private JLabel[][] board;
  private JLabel[][] pieces;
  // used in Go games
  private JLabel shadowPiece;

  private JPanel gameOverInfo;
  private JLabel winner;
  private JLabel score;

  private JPanel gameInfo;
  private JLabel moveCount;
  private JLabel player1Icon;
  private JLabel player2Icon;
  private JLabel player1User;
  private JLabel player2User;

  private JLayeredPane capturedPieces;
  private JPanel capturedPiecesBG;
  private JLabel captured;
  private JLabel[][] playerCaptured;
  private JLabel[][] playerCapturedNumber;

  private JPanel chatInterface;
  private JTextPane chatBox;
  private JTextField chat;
  private JCheckBox allowSpectatorsInChat;

  public GameScreen() {
    super(!Game.isOnline());

    // get the scale for tiles (all images are 50pixels), tile size, and top left coordinates
    int rowLength = Game.getBoardGame().getBoard().length;
    setScreenTileSize((int) (getBoardSize() / rowLength));
    setScale(getScreenTileSize() / imageTileSize);
    // System.out.println(getScale());
    setTopLeftY((int) Game.screenSize.getHeight() / 25);
    setTopLeftX(
        (int)
            (Game.screenSize.getWidth() / 2
                - (getScreenTileSize() * Game.getBoardGame().getBoard().length / 2)));

    title = new JLabel(Game.getBoardGame().getGameTitle());
    Game.mainWindow.add(title);
    title.setBounds(getTopLeftX(), getTopLeftY() - 20, 250, 15);

    back = new JButton("RETURN");
    Game.mainWindow.add(back);
    back.setBounds(getTopLeftX(), getTopLeftY() + (int) getBoardSize() + 10, 150, 30);
    back.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            System.out.println("RETURN TO GAMES LIST CLICKED");
            // if online player, return to active games, otherwise create offline game screen
            if (Game.isOnline()) Game.openActiveGamesScreen();
            else Game.openCreateOfflineGameScreen();
          }
        });

    resign = new JButton("RESIGN");
    Game.mainWindow.add(resign);
    resign.setBounds(
        getTopLeftX() + (int) getBoardSize() - 150,
        getTopLeftY() + (int) getBoardSize() + 10,
        150,
        30);
    resign.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            System.out.println("RESIGN CLICKED");
            if (JOptionPane.showConfirmDialog(
                    null,
                    "Are you sure you want to resign?",
                    "Resign",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE)
                == JOptionPane.YES_OPTION) {
              Game.sendMove(-2, -2);
              if (Game.getBoardGame().getMoves() < 3) Game.openActiveGamesScreen();
            }
            chat.requestFocus();
          }
        });
    // if the user is a spectator disable this button
    if (Game.getBoardGame().isPlayerIsSpectating()) resign.setEnabled(false);

    shadowPiece = new JLabel();
    pass = new JButton("PASS");

    // if playing go define the pass button
    GameType gameType = Game.getBoardGame().getGameType();
    if (gameType == GameType.GO9x9
        || gameType == GameType.GO13x13
        || gameType == GameType.GO19x19) {
      Game.mainWindow.add(pass);
      pass.setBounds(
          (int) (Game.screenSize.getWidth() / 2 - 75),
          getTopLeftY() + (int) getBoardSize() + 10,
          150,
          30);
      pass.addActionListener(
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              System.out.println("PASS CLICKED");
              // send move pass to server
              Game.sendMove(-1, -1);
              chat.requestFocus();
            }
          });
      // if the user is a spectator disable this button
      if (Game.getBoardGame().isPlayerIsSpectating()) pass.setEnabled(false);
    }

    int panelWidth =
        (int) (Game.screenSize.getWidth() - getTopLeftX() - (int) getBoardSize()) * 9 / 10;
    gameOverInfo = new JPanel();
    gameOverInfo.setBounds(
        getTopLeftX() + (int) getBoardSize() + panelWidth / 20,
        getTopLeftY(),
        panelWidth,
        getInfoTextSize() * 7 / 2);
    gameOverInfo.setLayout(null);

    winner = new JLabel();
    gameOverInfo.add(winner);
    winner.setBounds(
        getInfoTextSize() / 2,
        getInfoTextSize() / 2,
        panelWidth - getInfoTextSize(),
        getInfoTextSize());
    winner.setFont(new Font("Serif", Font.PLAIN, getInfoTextSize()));

    score = new JLabel();
    gameOverInfo.add(score);
    score.setBounds(
        getInfoTextSize() / 2,
        getInfoTextSize() * 2,
        panelWidth - getInfoTextSize(),
        getInfoTextSize());
    score.setFont(new Font("Serif", Font.PLAIN, getInfoTextSize()));

    gameInfo = new JPanel();
    Game.mainWindow.add(gameInfo);
    gameInfo.setBounds(
        getTopLeftX() + (int) getBoardSize() + panelWidth / 20,
        getTopLeftY() + getInfoTextSize() * 2,
        panelWidth,
        getInfoTextSize() * 5);
    gameInfo.setLayout(null);

    moveCount = new JLabel();
    gameInfo.add(moveCount);
    moveCount.setBounds(
        getInfoTextSize() / 2,
        getInfoTextSize() / 2,
        panelWidth - getInfoTextSize(),
        getInfoTextSize());
    moveCount.setFont(new Font("Serif", Font.PLAIN, getInfoTextSize()));

    player1Icon = new JLabel();
    gameInfo.add(player1Icon);
    player1Icon.setBounds(
        getInfoTextSize() / 2, getInfoTextSize() * 2, getInfoTextSize(), getInfoTextSize());

    player1User = new JLabel(Game.getBoardGame().getPlayer1());
    gameInfo.add(player1User);
    player1User.setBounds(
        getInfoTextSize() * 2,
        getInfoTextSize() * 2,
        panelWidth - getInfoTextSize() * 5 / 2,
        getInfoTextSize());
    player1User.setFont(new Font("Serif", Font.PLAIN, getInfoTextSize()));
    player1User.setForeground(Color.GREEN);

    player2Icon = new JLabel();
    gameInfo.add(player2Icon);
    player2Icon.setBounds(
        getInfoTextSize() / 2, getInfoTextSize() * 7 / 2, getInfoTextSize(), getInfoTextSize());

    player2User = new JLabel(Game.getBoardGame().getPlayer2());
    gameInfo.add(player2User);
    player2User.setBounds(
        getInfoTextSize() * 2,
        getInfoTextSize() * 7 / 2,
        panelWidth - getInfoTextSize() * 5 / 2,
        getInfoTextSize());
    player2User.setFont(new Font("Serif", Font.PLAIN, getInfoTextSize()));
    player2User.setForeground(Color.GREEN);

    capturedPieces = new JLayeredPane();
    Game.mainWindow.add(capturedPieces);
    capturedPieces.setBounds(
        getTopLeftX() + (int) getBoardSize() + panelWidth / 20,
        getTopLeftY() + getInfoTextSize() * 8,
        panelWidth,
        getInfoTextSize() * 6);
    capturedPieces.setLayout(null);

    capturedPiecesBG = new JPanel();
    capturedPiecesBG.setBounds(0, 0, capturedPieces.getWidth(), capturedPieces.getHeight());
    capturedPieces.add(capturedPiecesBG, JLayeredPane.FRAME_CONTENT_LAYER);

    captured = new JLabel("Captured Pieces: ");
    capturedPieces.add(captured);
    captured.setBounds(
        getInfoTextSize() / 2,
        getInfoTextSize() / 2,
        panelWidth - getInfoTextSize(),
        getInfoTextSize());
    captured.setFont(new Font("Serif", Font.PLAIN, getInfoTextSize()));

    BufferedImage[][] capturablePieces = Game.getBoardGame().getCapturablePieces();
    playerCaptured = new JLabel[capturablePieces.length][capturablePieces[0].length];
    playerCapturedNumber = new JLabel[capturablePieces.length][capturablePieces[0].length];
    int gap =
        (capturedPieces.getWidth() - getInfoTextSize() * capturablePieces.length)
            / (capturablePieces.length + 1);

    for (int i = 0; i < playerCaptured.length; i++) {
      for (int j = 0; j < playerCaptured[i].length; j++) {
        playerCaptured[i][j] = new JLabel(new ImageIcon(capturablePieces[i][j]));
        capturedPieces.add(playerCaptured[i][j], JLayeredPane.DEFAULT_LAYER);
        playerCaptured[i][j].setBounds(
            gap + i * (getInfoTextSize()),
            getInfoTextSize() * 2 + j * (getInfoTextSize() * 2),
            getInfoTextSize(),
            getInfoTextSize());
        playerCaptured[i][j].addMouseListener(
            new MouseAdapter() {
              public void mouseClicked(MouseEvent e) {
                Game.getBoardGame()
                    .handleMouseClickCapturedPiece(getCoordinates((JLabel) e.getSource()));
              }

              public void mouseEntered(MouseEvent e) {
                Game.getBoardGame()
                    .handleMouseEnterCapturedPiece(getCoordinates((JLabel) e.getSource()));
              }

              public void mouseExited(MouseEvent e) {
                Game.getBoardGame().handleMouseLeaveCapturedPiece();
              }

              private int[] getCoordinates(JLabel piece) {
                int[] coordinates = new int[2];
                for (int i = 0; i < playerCaptured.length; i++)
                  for (int j = 0; j < playerCaptured[i].length; j++)
                    if (piece.equals(board[i][j])) {
                      coordinates[0] = i;
                      coordinates[1] = j;
                    }
                return coordinates;
              }
            });

        playerCapturedNumber[i][j] = new JLabel();
        capturedPieces.add(playerCapturedNumber[i][j], JLayeredPane.PALETTE_LAYER);
        playerCapturedNumber[i][j].setBounds(
            gap + i * (getInfoTextSize()) + getInfoTextSize() / 2,
            getInfoTextSize() * 5 / 2 + j * (getInfoTextSize() * 2),
            gap - getInfoTextSize() / 2,
            getInfoTextSize());
        playerCapturedNumber[i][j].setFont(new Font("Serif", Font.PLAIN, getInfoTextSize()));
        // if game is go set top number to black
        if (gameType == GameType.GO9x9
            || gameType == GameType.GO13x13
            || gameType == GameType.GO19x19) {
          playerCapturedNumber[0][0].setForeground(Color.BLACK);
        }
      }
    }

    chatInterface = new JPanel();
    Game.mainWindow.add(chatInterface);
    chatInterface.setBounds(
        panelWidth / 20, getTopLeftY(), panelWidth, (int) getBoardSize() * 7 / 8);
    chatInterface.setLayout(null);

    chatBox = new JTextPane();
    chatInterface.add(chatBox);
    chatBox.setBounds(5, 5, chatInterface.getWidth() - 10, chatInterface.getHeight() - 40);
    chatBox.setEditable(false);
    chatBox.setMargin(new Insets(chatBox.getHeight() - 20, 0, 0, 0));

    chat = new JTextField();
    chatInterface.add(chat);
    chat.setBounds(5, chatInterface.getHeight() - 35, chatInterface.getWidth() - 10, 30);
    chat.setFont(new Font("Serif", Font.PLAIN, 15));
    chat.addKeyListener(
        new KeyListener() {
          public void keyPressed(KeyEvent e) {}

          public void keyReleased(KeyEvent e) {}

          public void keyTyped(KeyEvent e) {
            if (e.getKeyChar() == '\n' && ((JTextField) e.getSource()).getText().length() > 0)
              Game.sendChat(allowSpectatorsInChat.isSelected(), chat.getText());
          }
        });
    chat.requestFocus();
    if (!Game.isOnline()) chat.setEnabled(false);

    allowSpectatorsInChat = new JCheckBox("Allow spectators in chat");
    // if the user is a spectator or offline do not include check box on screen but set it true
    if (Game.getBoardGame().isPlayerIsSpectating() || !Game.isOnline())
      allowSpectatorsInChat.setSelected(true);
    else {
      // if the user isn't a spectator set not allowed by default
      allowSpectatorsInChat.setSelected(false);
      allowSpectatorsInChat.setBounds(
          panelWidth * 11 / 20 - 150, getTopLeftY() + chatInterface.getHeight() + 50, 300, 50);
      allowSpectatorsInChat.setHorizontalAlignment(JCheckBox.CENTER);
      Game.mainWindow.add(allowSpectatorsInChat);
    }

    pane = new JLayeredPane();
    pane.setBounds(
        getTopLeftX(),
        getTopLeftY(),
        ((int) getScreenTileSize()) * rowLength,
        ((int) getScreenTileSize()) * rowLength);
    pane.setOpaque(false);
    Game.mainWindow.add(pane);

    // add board to screen and update pieces
    pieces = new JLabel[rowLength][rowLength];
    addBoard();
    updateBoard();

    Game.mainWindow.repaint();
  }

  /** This method adds tiles of board Should only be called by constructor */
  private void addBoard() {
    Tile[][] tiles = Game.getBoardGame().getTiles();
    board = new JLabel[tiles.length][tiles.length];

    for (int i = 0; i < board.length; i++)
      for (int j = 0; j < board.length; j++) {
        board[i][j] = new JLabel(new ImageIcon(tiles[i][j].getTile()));
        pane.add(board[i][j], JLayeredPane.FRAME_CONTENT_LAYER);

        board[i][j].setBounds(
            (int) (j * getScreenTileSize()),
            (int) (i * getScreenTileSize()),
            (int) getScreenTileSize(),
            (int) getScreenTileSize());
        board[i][j].addMouseListener(
            new MouseAdapter() {
              public void mouseClicked(MouseEvent e) {
                Game.getBoardGame().handleMouseClickTile(getCoordinates((JLabel) e.getSource()));
              }

              public void mouseEntered(MouseEvent e) {
                Game.getBoardGame().handleMouseEnterTile(getCoordinates((JLabel) e.getSource()));
              }

              public void mouseExited(MouseEvent e) {
                Game.getBoardGame().handleMouseLeaveTile();
              }

              private int[] getCoordinates(JLabel tile) {
                int[] coordinates = new int[2];
                for (int i = 0; i < board.length; i++)
                  for (int j = 0; j < board.length; j++)
                    if (tile.equals(board[i][j])) {
                      coordinates[0] = i;
                      coordinates[1] = j;
                    }
                return coordinates;
              }
            });
      }
    pane.repaint();
  }

  /**
   * Returns a proper image in transparency
   *
   * @param img
   * @param transparent
   * @return
   */
  private BufferedImage getProperImage(BufferedImage img, float transparent) {
    BufferedImage properImage =
        new BufferedImage(
            (int) getScreenTileSize(), (int) getScreenTileSize(), BufferedImage.TYPE_INT_ARGB);

    // change transparency
    AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparent);

    // draw image
    Graphics2D g2d = properImage.createGraphics();
    g2d.setComposite(ac);
    g2d.drawImage(img, 0, 0, null);
    g2d.dispose();
    return properImage;
  }

  /**
   * This method updates pieces on the board as well as other information that changes during game
   * play
   */
  public void updateBoard() {
    if (Game.getBoardGame().isPlayerTurn()) pass.setEnabled(true);
    else pass.setEnabled(false);
    Piece[][] gamePieces = Game.getBoardGame().getPieces();

    for (int i = 0; i < pieces.length; i++)
      for (int j = 0; j < pieces.length; j++) {
        if (pieces[i][j] != null) pane.remove(pieces[i][j]);
        if (gamePieces[i][j] != null) {
          pieces[i][j] = new JLabel(new ImageIcon(gamePieces[i][j].getPiece()));
          pane.add(pieces[i][j], JLayeredPane.DEFAULT_LAYER);

          pieces[i][j].setBounds(
              (int) (j * getScreenTileSize()),
              (int) (i * getScreenTileSize()),
              (int) getScreenTileSize(),
              (int) getScreenTileSize());
          pieces[i][j].addMouseListener(
              new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                  Game.getBoardGame().handleMouseClickPiece(getCoordinates((JLabel) e.getSource()));
                }

                public void mouseEntered(MouseEvent e) {
                  Game.getBoardGame().handleMouseEnterPiece(getCoordinates((JLabel) e.getSource()));
                }

                public void mouseExited(MouseEvent e) {
                  Game.getBoardGame().handleMouseLeavePiece();
                }

                private int[] getCoordinates(JLabel piece) {
                  int[] coordinates = new int[2];
                  for (int i = 0; i < pieces.length; i++)
                    for (int j = 0; j < pieces.length; j++)
                      if (piece.equals(pieces[i][j])) {
                        coordinates[0] = i;
                        coordinates[1] = j;
                      }
                  return coordinates;
                }
              });
        }
      }
    // highlight last move
    int lastMove = Game.getBoardGame().getLastMove();
    if (lastMove > -1)
      pieces[lastMove / pieces.length][lastMove % pieces.length].setIcon(
          new ImageIcon(
              gamePieces[lastMove / pieces.length][lastMove % pieces.length].getLastMovePiece()));

    // update game info panel, update moves below
    player1Icon.setIcon(new ImageIcon(Game.getBoardGame().getPlayer1Icon()));
    player2Icon.setIcon(new ImageIcon(Game.getBoardGame().getPlayer2Icon()));

    // update captured pieces panel
    int[][] capturedPieces = Game.getBoardGame().getNumberCapturedPieces();
    for (int i = 0; i < playerCapturedNumber.length; i++) {
      // ensure both numbers are the same length
      String cap0 = "" + capturedPieces[i][0];
      String cap1 = "" + capturedPieces[i][1];
      while (cap0.length() < cap1.length()) cap0 = "0" + cap0;
      while (cap1.length() < cap0.length()) cap1 = "0" + cap1;

      playerCapturedNumber[i][0].setText(cap0);
      playerCapturedNumber[i][1].setText(cap1);
    }

    // handle playing game
    if (Game.getBoardGame().getWinner() == null) {
      // handle a pass
      if (lastMove == -1) {
        new Thread(
                new Runnable() {
                  public void run() {
                    try {
                      JLabel pass =
                          new JLabel(
                              (Game.getBoardGame().isPlayer1Turn()
                                      ? Game.getBoardGame().getPlayer2()
                                      : Game.getBoardGame().getPlayer1())
                                  + "   Passed",
                              SwingConstants.CENTER);
                      pane.add(pass, JLayeredPane.POPUP_LAYER);
                      pass.setBounds(
                          0,
                          (int) getBoardSize() / 2 - (int) getScreenTileSize() / 2,
                          (int) getBoardSize(),
                          (int) getScreenTileSize());
                      pass.setFont(new Font("Serif", Font.PLAIN, 50));
                      pass.setOpaque(true);

                      Thread.sleep(1000);
                      pane.remove(pass);
                      pane.repaint();
                    } catch (Exception e) {
                      e.printStackTrace();
                    }
                  }
                })
            .start();
      }
      moveCount.setText("Move: " + (Game.getBoardGame().getMoves() + 1));
    } else {
      Game.mainWindow.add(gameOverInfo);
      gameInfo.setBounds(
          getTopLeftX() + (int) getBoardSize() + gameInfo.getWidth() / 20,
          getTopLeftY() + getInfoTextSize() * 4,
          gameInfo.getWidth(),
          getInfoTextSize() * 5);
      this.capturedPieces.setBounds(
          getTopLeftX() + (int) getBoardSize() + gameInfo.getWidth() / 20,
          getTopLeftY() + getInfoTextSize() * 10,
          gameInfo.getWidth(),
          getInfoTextSize() * 6);
      moveCount.setText("Move: " + (Game.getBoardGame().getMoves()));
      winner.setText("Winner: " + Game.getBoardGame().getWinner());
      score.setText(Game.getBoardGame().getScoreInfo());

      removePieceShadow();
      pass.setEnabled(false);
      resign.setEnabled(false);
      this.capturedPieces.repaint();
    }
    pane.repaint();
    gameOverInfo.repaint();
    gameInfo.repaint();

    updatePlayersOnGame();
  }

  /**
   * Updates the game chat
   *
   * @param sender
   * @param message
   */
  public void updateChat(String sender, String message) {
    boolean clearText = sender.equals(Game.getPlayer().getUsername());
    boolean displayMessage = true;
    Color senderColor = Color.CYAN;

    if (sender.equals(Game.getPlayer().getUsername())) {
      senderColor = Color.GREEN;
    } else if (sender.equals(Game.getBoardGame().getPlayer1())
        || sender.equals(Game.getBoardGame().getPlayer2())) {
      senderColor = Color.RED;
    } else {
      senderColor = Color.CYAN;
      displayMessage = allowSpectatorsInChat.isSelected();
    }

    if (displayMessage) {
      chatBox.setMargin(new Insets(chatBox.getInsets().top - 20, 0, 0, 0));
      appendText(chatBox, "\n" + sender + ": ", senderColor);
      appendText(chatBox, message, Color.WHITE);
    }
    if (clearText) {
      chat.setText("");
    }

    chat.requestFocus();

    chatBox.repaint();
    chatInterface.repaint();
  }

  /** Sets players' name green if they are online, red if not */
  public void updatePlayersOnGame() {
    player1User.setForeground(Game.getBoardGame().isPlayer1OnGame() ? Color.GREEN : Color.RED);
    player2User.setForeground(Game.getBoardGame().isPlayer2OnGame() ? Color.GREEN : Color.RED);
  }

  /**
   * Used in Go games, this will show a shadow of where a piece would be placed on click
   *
   * @param piece
   * @param x
   * @param y
   */
  public void displayPieceShadow(int x, int y) {
    // adds shadow piece to screen
    shadowPiece.setIcon(
        new ImageIcon(
            getProperImage(
                Stone.getPiece(
                    (byte) (Game.getBoardGame().getGameType().getType() - 1),
                    Game.getBoardGame().isPlayer1()),
                .75f)));
    pane.add(shadowPiece, JLayeredPane.PALETTE_LAYER);

    shadowPiece.setBounds(
        (int) (y * getScreenTileSize()),
        (int) (x * getScreenTileSize()),
        (int) getScreenTileSize(),
        (int) getScreenTileSize());
    pane.repaint();
  }

  /** Used in Go games, this will remove a shadow piece */
  public void removePieceShadow() {
    pane.remove(shadowPiece);

    pane.repaint();
  }

  @Override
  public void exit() {
    if (Game.isOnline()) {
      Game.stopPlaying();
      exitParentGUI();
    }

    Game.mainWindow.remove(back);
    Game.mainWindow.remove(title);
    Game.mainWindow.remove(pane);
    Game.mainWindow.remove(pass);
    Game.mainWindow.remove(resign);
    Game.mainWindow.remove(gameOverInfo);
    Game.mainWindow.remove(gameInfo);
    Game.mainWindow.remove(capturedPieces);
    Game.mainWindow.remove(chatInterface);
    Game.mainWindow.remove(allowSpectatorsInChat);

    back = null;
    title = null;
    pane = null;
    board = null;
    shadowPiece = null;
    pass = null;
    resign = null;
    gameOverInfo = null;
    gameInfo = null;
    capturedPieces = null;
    chatInterface = null;
    allowSpectatorsInChat = null;

    Game.mainWindow.repaint();
  }

  public double getScale() {
    return scale;
  }

  public void setScale(double scale) {
    this.scale = scale;
  }

  public double getScreenTileSize() {
    return screenTileSize;
  }

  public void setScreenTileSize(double screenTileSize) {
    this.screenTileSize = screenTileSize;
  }

  public int getTopLeftX() {
    return topLeftX;
  }

  public void setTopLeftX(int topLeftX) {
    this.topLeftX = topLeftX;
  }

  public int getTopLeftY() {
    return topLeftY;
  }

  public void setTopLeftY(int topLeftY) {
    this.topLeftY = topLeftY;
  }

  /**
   * @return the boardSize
   */
  public static double getBoardSize() {
    return boardSize;
  }

  /**
   * @param boardSize the boardSize to set
   */
  public static void setBoardSize(double boardSize) {
    GameScreen.boardSize = boardSize;
  }

  public static double getImagetilesize() {
    return imageTileSize;
  }

  /**
   * @return the infoTextSize
   */
  public static int getInfoTextSize() {
    return infoTextSize;
  }

  /**
   * @param infoTextSize the infoTextSize to set
   */
  public static void setInfoTextSize(int infoTextSize) {
    GameScreen.infoTextSize = infoTextSize;
  }
}
