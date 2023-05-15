package com.nfehs.librarygames.games.go.pieces;

import java.awt.image.BufferedImage;

import com.nfehs.librarygames.screens.GameScreen;

/**
 * This class handles the go piece stone
 * @author Patrick Kenney and Syed Quadri
 * @date 8/15/2018
 */

public class Stone extends GoPiece {
  private static BufferedImage[][] stones;
  private static BufferedImage[][] stonesLastMove;

  /**
   * Constructor for a stone, a Go piece
   * @param gameType
   * @param isBlackPiece
   */
  public Stone(byte gameType, boolean isBlackPiece) {
    super(gameType, isBlackPiece);
  }

  /**
   * Returns the image of a stone
   * @param gameType
   * @param isPlayer1
   * @return
   */
  public static BufferedImage getPiece(byte gameType, boolean isPlayer1) {
    if (isPlayer1)
      return stones[gameType][0];
    return stones[gameType][1];
  }

  /**
   * Returns the image of the stone
   * @return
   */
  public BufferedImage getPiece() {
    if (isBlackPiece())
      return stones[getGameType()][0];
    return stones[getGameType()][1];
  }

  /**
   * Returns the image of the stone selected as last move
   * @return
   */
  public BufferedImage getLastMovePiece() {
    if (isBlackPiece())
      return stonesLastMove[getGameType()][0];
    return stonesLastMove[getGameType()][1];
  }

  /**
   * Called on startup from GoPiece, initializes images values
   */
  public static void loadImages() {
    // initialize media variables
    stones = new BufferedImage[3][2];
    stonesLastMove = new BufferedImage[3][2];

    // load images
    for (int i = 0; i < stones.length; i++) {
      stones[i][0] = getStoneImage(true, (int) GameScreen.getBoardSize() / ROWS[i], false);
      stones[i][1] = getStoneImage(false, (int) GameScreen.getBoardSize() / ROWS[i], false);
      stonesLastMove[i][0] = getStoneImage(true, (int) GameScreen.getBoardSize() / ROWS[i], true);
      stonesLastMove[i][1] = getStoneImage(false, (int) GameScreen.getBoardSize() / ROWS[i], true);
    }
  }
}
