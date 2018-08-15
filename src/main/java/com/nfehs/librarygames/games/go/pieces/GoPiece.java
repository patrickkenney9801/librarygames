package com.nfehs.librarygames.games.go.pieces;

import java.awt.image.BufferedImage;

import com.nfehs.librarygames.games.Piece;

/**
 * This is the parent class for go game pieces
 * @author Patrick Kenney and Syed Quadri
 * @date 8/15/2018
 */

public abstract class GoPiece extends Piece {
	private boolean isBlackPiece;
	
	public GoPiece(BufferedImage piece, boolean isBlackPiece) {
		super(piece);
		setBlackPiece(isBlackPiece);
	}

	public boolean isBlackPiece() {
		return isBlackPiece;
	}

	public void setBlackPiece(boolean isBlackPiece) {
		this.isBlackPiece = isBlackPiece;
	}
}
