package com.nfehs.librarygames.games.go.tiles;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import com.nfehs.librarygames.games.Tile;

/**
 * This class handles the edge piece of a go board
 * @author Patrick Kenney and Syed Quadri
 * @date 8/15/2018
 */

public class EdgeTile extends Tile {
	
	/**
	 * Sets tile image as edge and sets proper rotation for edges
	 * @param cornerNumber left is 0, top 1, right 2, bottom 3
	 */
	public EdgeTile(int edgeNumber) {
		super(getEdgeTile(edgeNumber));
	}
	
	/**
	 * Returns proper edge tile
	 * @param edgeNumber
	 * @return
	 */
	private static BufferedImage getEdgeTile(int edgeNumber) {
		try {
			return ImageIO.read(EdgeTile.class.getResource("/com/nfehs/librarygames/media/go/goEdgeTile" + edgeNumber + ".png"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
