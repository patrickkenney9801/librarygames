package com.nfehs.librarygames.games.go.tiles;

import com.nfehs.librarygames.games.BoardGame.GameType;
import com.nfehs.librarygames.games.Tile;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/**
 * This class handles the tiles of a go board
 *
 * @author Patrick Kenney and Syed Quadri
 * @date 8/16/2018
 */
public class GoTile extends Tile {
  private static final int[] ROWS = {9, 13, 19};

  private static BufferedImage[] centerTiles;
  private static BufferedImage[][] edgeTiles;
  private static BufferedImage[][] cornerTiles;

  private int tileType;
  private GameType gameType;

  /**
   * Sets the image for the tile
   *
   * @param tileType 0 for center, 1 for edge, 2 for corner
   * @param gameType
   * @param rotation
   */
  public GoTile(int tileType, GameType gameType, int rotation) {
    super(rotation);
    setTileType(tileType);
    setGameType(gameType);
  }

  /**
   * Returns the image of a specified go tile
   *
   * @param tileType
   * @param gameType
   * @param rotation
   * @return
   */
  public BufferedImage getTile() {
    switch (getTileType()) {
      case 0:
        return centerTiles[getGameType().getType() - 1];
      case 1:
        return edgeTiles[getGameType().getType() - 1][getRotations()];
      case 2:
        return cornerTiles[getGameType().getType() - 1][getRotations()];
      default:
        return null;
    }
  }

  /** Called on startup from Tile, initializes images values */
  public static void loadImages() {
    // initialize media variables
    centerTiles = new BufferedImage[3];
    edgeTiles = new BufferedImage[3][4];
    cornerTiles = new BufferedImage[3][4];

    // load images
    try {
      centerTiles[2] = ImageIO.read(GoTile.class.getResource("/go/goCenterTile.png"));

      for (int i = 0; i < edgeTiles[0].length; i++) {
        edgeTiles[2][i] = ImageIO.read(GoTile.class.getResource("/go/goEdgeTile" + i + ".png"));
        cornerTiles[2][i] = ImageIO.read(GoTile.class.getResource("/go/goCornerTile" + i + ".png"));
      }
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }

    // get proper sizes for images
    for (int i = 0; i < centerTiles.length; i++) {
      centerTiles[i] = getProperImage(centerTiles[2], ROWS[i], 0);
      for (int j = 0; j < edgeTiles[i].length; j++) {
        edgeTiles[i][j] = getProperImage(edgeTiles[2][j], ROWS[i], 0);
        cornerTiles[i][j] = getProperImage(cornerTiles[2][j], ROWS[i], 0);
      }
    }
  }

  public int getTileType() {
    return tileType;
  }

  public void setTileType(int tileType) {
    this.tileType = tileType;
  }

  public GameType getGameType() {
    return gameType;
  }

  public void setGameType(GameType gameType) {
    this.gameType = gameType;
  }
}
