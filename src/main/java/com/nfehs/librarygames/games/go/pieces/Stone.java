package com.nfehs.librarygames.games.go.pieces;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

/**
 * This class handles the go piece stone
 * @author Patrick Kenney and Syed Quadri
 * @date 8/15/2018
 */

public class Stone extends GoPiece {
	
	/**
	 * Constructor for a stone, a Go piece
	 * @param isBlackPiece
	 */
	public Stone(boolean isBlackPiece) {
		super(getImage(isBlackPiece), isBlackPiece);
	}

	/**
	 * Returns the image of a stone
	 * @param isBlackPiece
	 * @return
	 */
	private static BufferedImage getImage(boolean isBlackPiece) {
		if (isBlackPiece)
			return getBlackStone();
		return getWhiteStone();
	}

	/**
	 * Returns image of a black stone
	 * @return
	 */
	public static BufferedImage getBlackStone() {
		try {
			return ImageIO.read(Stone.class.getResource("/com/nfehs/librarygames/media/go/blackStone.png"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns image of a white stone
	 * @return
	 */
	public static BufferedImage getWhiteStone() {
		try {
			return ImageIO.read(Stone.class.getResource("/com/nfehs/librarygames/media/go/whiteStone.png"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
