package com.nfehs.librarygames.games.go.pieces;

import java.awt.image.BufferedImage;

import com.nfehs.librarygames.games.Piece;

/**
 * This is the parent class for go game pieces
 * @author Patrick Kenney and Syed Quadri
 * @date 8/15/2018
 */

public abstract class GoPiece extends Piece {
	protected static final int[] ROWS = {9, 13, 19};
	
	private boolean isBlackPiece;
	
	public GoPiece(BufferedImage piece, boolean isBlackPiece) {
		super(piece);
		setBlackPiece(isBlackPiece);
	}
	
	/**
	 * Should only be called once from superclass Piece
	 * Loads all media for the GoPiece class and subclasses in proper size and rotation
	 */
	public static void loadImages() {
		Stone.loadImages();
	}

	public boolean isBlackPiece() {
		return isBlackPiece;
	}

	public void setBlackPiece(boolean isBlackPiece) {
		this.isBlackPiece = isBlackPiece;
	}
}
