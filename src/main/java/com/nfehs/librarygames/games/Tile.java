package com.nfehs.librarygames.games;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import com.nfehs.librarygames.games.go.tiles.GoTile;
import com.nfehs.librarygames.screens.GameScreen;

/**
 * This is the parent class for game tiles
 * @author Patrick Kenney and Syed Quadri
 * @date 8/15/2018
 */

public abstract class Tile {
  private int rotations;

  /**
   * Does not allow rotation of tile
   * @param tile
   */
  public Tile () {
    setRotations(0);
  }

  /**
   * Will rotate tile 90 degrees, @param rotations times
   * @param tile
   * @param rotations
   */
  public Tile (int rotations) {
    setRotations(rotations);
  }

  /**
   * Should only be called once from GameFrame
   * Loads all media for the Tile class and subclasses in proper size and rotation
   */
  public static void loadImages() {
    GoTile.loadImages();
  }

  /**
   * Returns a proper image in size and rotation
   * @param img
   * @param boardLength
   * @param rotations
   * @return
   */
  protected static BufferedImage getProperImage(BufferedImage img, int boardLength, int rotations) {
    int newLength = (int) (GameScreen.getBoardSize() / boardLength);

    BufferedImage properImage = new BufferedImage(newLength, newLength, BufferedImage.TYPE_INT_ARGB);
    AffineTransform at = new AffineTransform();

    // rotate image about center
    at.rotate(Math.PI/2 * rotations, newLength / 2, newLength / 2);
    // scale image to right size
    at.scale(newLength / GameScreen.getImagetilesize(), newLength / GameScreen.getImagetilesize());

    // draw image
    Graphics2D g2d = properImage.createGraphics();
    g2d.setTransform(at);
    g2d.drawImage(img, 0, 0, null);
    g2d.dispose();
    return properImage;
  }

  public int getRotations() {
    return this.rotations;
  }

  /**
   * This method sets rotation for the tile 90 degrees * @param rotation times
   * @param rotations range [1-3]
   */
  public void setRotations(int rotations) {
    if (rotations < 1 || rotations > 3)
      this.rotations = 0;
    else
      this.rotations = rotations;
  }

  public abstract BufferedImage getTile();
}
