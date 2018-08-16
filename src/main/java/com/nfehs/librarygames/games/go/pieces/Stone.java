package com.nfehs.librarygames.games.go.pieces;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import com.nfehs.librarygames.games.go.tiles.GoTile;

/**
 * This class handles the go piece stone
 * @author Patrick Kenney and Syed Quadri
 * @date 8/15/2018
 */

public class Stone extends GoPiece {
	private static BufferedImage[][] stones;
	
	/**
	 * Constructor for a stone, a Go piece
	 * @param isBlackPiece
	 */
	public Stone(byte gameType, boolean isBlackPiece) {
		super(getImage(gameType, isBlackPiece), isBlackPiece);
	}

	/**
	 * Returns the image of a stone
	 * @param isBlackPiece
	 * @return
	 */
	public static BufferedImage getImage(byte gameType, boolean isBlackPiece) {
		if (isBlackPiece)
			return stones[gameType][0];
		return stones[gameType][1];
	}
	
	/**
	 * Called on startup from GoPiece, initializes images values
	 */
	public static void loadImages() {
		// initialize media variables
		stones = new BufferedImage[3][2];
		
		// load images
		try {
			stones[2][0] = ImageIO.read(Stone.class.getResource("/com/nfehs/librarygames/media/go/blackStone.png"));
			stones[2][1] = ImageIO.read(Stone.class.getResource("/com/nfehs/librarygames/media/go/whiteStone.png"));
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		// get proper sizes for images
		for (int i = 0; i < stones.length; i++) {
			stones[i][0] = getProperImage(stones[2][0], ROWS[i], 0);
			stones[i][1] = getProperImage(stones[2][1], ROWS[i], 0);
		}
	}
}
