package com.nfehs.librarygames.games.go.tiles;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import com.nfehs.librarygames.games.Tile;

/**
 * This class handles the center piece of a go board
 * @author Patrick Kenney and Syed Quadri
 * @date 8/15/2018
 */

public class CenterTile extends Tile {
	
	/**
	 * Sets tile image as center tile
	 */
	public CenterTile() {
		super(getCenterTile());
	}
	
	private static BufferedImage getCenterTile() {
		try {
			return ImageIO.read(EdgeTile.class.getResource("/com/nfehs/librarygames/media/go/goCenterTile.png"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
