package com.nfehs.librarygames.games.go.tiles;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import com.nfehs.librarygames.games.Tile;

/**
 * This class handles the corner piece of a go board
 * @author Patrick Kenney and Syed Quadri
 * @date 8/15/2018
 */

public class CornerTile extends Tile {
	
	/**
	 * Sets tile image as corner and sets proper rotation for corners
	 * @param cornerNumber topleft is 0, TR 1, bottomright 2, BL 3
	 */
	public CornerTile(int cornerNumber) {
		super(getCornerTile(cornerNumber));
	}
	
	/**
	 * Returns the appropriate corner image
	 * @param cornerNumber
	 * @return
	 */
	private static BufferedImage getCornerTile(int cornerNumber) {
		try {
			return ImageIO.read(CornerTile.class.getResource("/com/nfehs/librarygames/media/go/goCornerTile" + cornerNumber + ".png"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
