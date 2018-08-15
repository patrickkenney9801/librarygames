package com.nfehs.librarygames.games;

import java.awt.image.BufferedImage;

/**
 * This is the parent class for game pieces
 * @author Patrick Kenney and Syed Quadri
 * @date 8/15/2018
 */

public abstract class Piece {
	private BufferedImage piece;
	
	public Piece (BufferedImage piece) {
		setPiece(piece);
	}

	public BufferedImage getPiece() {
		return piece;
	}

	public void setPiece(BufferedImage piece) {
		this.piece = piece;
	}
}
