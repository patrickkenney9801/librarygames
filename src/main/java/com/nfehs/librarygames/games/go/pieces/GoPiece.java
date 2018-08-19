package com.nfehs.librarygames.games.go.pieces;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import com.nfehs.librarygames.games.Piece;
import com.nfehs.librarygames.screens.GameScreen;

/**
 * This is the parent class for go game pieces
 * @author Patrick Kenney and Syed Quadri
 * @date 8/15/2018
 */

public abstract class GoPiece extends Piece {
	protected static final int[] ROWS = {9, 13, 19};
	
	private static BufferedImage player1Icon;
	private static BufferedImage player2Icon;
	private static BufferedImage player1IconPlaying;
	private static BufferedImage player2IconPlaying;
	
	private boolean isBlackPiece;
	private byte gameType;
	
	public GoPiece(byte gameType, boolean isBlackPiece) {
		setBlackPiece(isBlackPiece);
		setGameType(gameType);
	}
	
	/**
	 * Returns a 2D array of all capturable pieces
	 * 0,0 is white stone
	 * 0,1 is black stone
	 * @return
	 */
	public static BufferedImage[][] getCapturablePieces() {
		BufferedImage[][] pieces = new BufferedImage[1][2];
		pieces[0][0] = getPlayer2Icon();
		pieces[0][1] = getPlayer1Icon();
		return pieces;
	}

	/**
	 * Returns a stone in proper size given
	 * @param isBlackStone
	 * @param size
	 * @param border
	 * @return
	 */
	protected static BufferedImage getStoneImage(boolean isBlackStone, int size, boolean border) {
		BufferedImage stoneImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		
		// draw image
		Graphics2D g2d = stoneImage.createGraphics();
		g2d.setComposite(AlphaComposite.Src);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Color.CYAN);
		if (border)
			g2d.fillOval(0, 0, size, size);
		g2d.setColor(isBlackStone ? Color.BLACK : Color.WHITE);
		if (!border)
			g2d.fillOval(0, 0, size, size);
		else
			g2d.fillOval(size / 20, size / 20, size - 2*(size / 20), size - 2*(size / 20));
		g2d.dispose();
		return stoneImage;
	}
	
	/**
	 * Should only be called once from superclass Piece
	 * Loads all media for the GoPiece class and subclasses in proper size and rotation
	 */
	public static void loadImages() {
		Stone.loadImages();
		
		int iconSize = GameScreen.getInfoTextSize();
		setPlayer1Icon(getStoneImage(true, iconSize, false));
		setPlayer1IconPlaying(getStoneImage(true, iconSize, true));
		setPlayer2Icon(getStoneImage(false, iconSize, false));
		setPlayer2IconPlaying(getStoneImage(false, iconSize, true));
	}

	public boolean isBlackPiece() {
		return isBlackPiece;
	}

	public void setBlackPiece(boolean isBlackPiece) {
		this.isBlackPiece = isBlackPiece;
	}

	/**
	 * @return the player1Icon
	 */
	public static BufferedImage getPlayer1Icon() {
		return player1Icon;
	}

	/**
	 * @param player1Icon the player1Icon to set
	 */
	public static void setPlayer1Icon(BufferedImage player1Icon) {
		GoPiece.player1Icon = player1Icon;
	}

	/**
	 * @return the player2Icon
	 */
	public static BufferedImage getPlayer2Icon() {
		return player2Icon;
	}

	/**
	 * @param player2Icon the player2Icon to set
	 */
	public static void setPlayer2Icon(BufferedImage player2Icon) {
		GoPiece.player2Icon = player2Icon;
	}


	public static BufferedImage getPlayer1IconPlaying() {
		return player1IconPlaying;
	}


	public static void setPlayer1IconPlaying(BufferedImage player1IconPlaying) {
		GoPiece.player1IconPlaying = player1IconPlaying;
	}


	public static BufferedImage getPlayer2IconPlaying() {
		return player2IconPlaying;
	}


	public static void setPlayer2IconPlaying(BufferedImage player2IconPlaying) {
		GoPiece.player2IconPlaying = player2IconPlaying;
	}


	public byte getGameType() {
		return gameType;
	}


	public void setGameType(byte gameType) {
		this.gameType = gameType;
	}
}
