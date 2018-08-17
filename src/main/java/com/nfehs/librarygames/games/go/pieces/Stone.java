package com.nfehs.librarygames.games.go.pieces;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import com.nfehs.librarygames.games.go.tiles.GoTile;
import com.nfehs.librarygames.screens.GameScreen;

/**
 * This class handles the go piece stone
 * @author Patrick Kenney and Syed Quadri
 * @date 8/15/2018
 */

public class Stone extends GoPiece {
	private static BufferedImage[][] stones;
	
	/**
	 * Constructor for a stone, a Go piece
	 * @param gameType
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
		for (int i = 0; i < stones.length; i++) {
			stones[i][0] = getStoneImage(true, (int) GameScreen.getBoardSize() / ROWS[i], false);
			stones[i][1] = getStoneImage(false, (int) GameScreen.getBoardSize() / ROWS[i], false);
		}
	}
}
