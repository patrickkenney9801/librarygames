package com.nfehs.librarygames.games;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import com.nfehs.librarygames.games.go.pieces.GoPiece;
import com.nfehs.librarygames.screens.GameScreen;

/**
 * This is the parent class for game pieces
 * @author Patrick Kenney and Syed Quadri
 * @date 8/15/2018
 */

public abstract class Piece {
	/**
	 * Should only be called once from GameFrame
	 * Loads all media for the Piece class and subclasses in proper size and rotation
	 */
	public static void loadImages() {
		GoPiece.loadImages();
	}
	
	/**
	 * Returns a proper image in size and rotation
	 * @param img
	 * @param boardLength
	 * @param rotations
	 * @return
	 */
	protected static BufferedImage getProperImage(BufferedImage img, int boardLength, int rotations) {
		int newLength = (int) (GameScreen.getBoardHeighth() / boardLength);
		
		BufferedImage properImage = new BufferedImage(newLength, newLength, BufferedImage.TYPE_INT_ARGB);
		AffineTransform at = new AffineTransform();
		
		// rotate image about center
		at.rotate(Math.PI/2 * rotations, newLength / 2, newLength / 2);
		// scale image to right size
		at.scale(newLength / GameScreen.getImagetilesize(), newLength / GameScreen.getImagetilesize());
		
		// draw image
		Graphics2D g2d = properImage.createGraphics();
		g2d.setTransform(at);
		g2d.drawImage(img, 0, 0, null);
		g2d.dispose();
		return properImage;
	}

	public abstract BufferedImage getPiece();
	public abstract BufferedImage getLastMovePiece();
}
