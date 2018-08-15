package com.nfehs.librarygames.games;

import java.awt.image.BufferedImage;

/**
 * This is the parent class for game tiles
 * @author Patrick Kenney and Syed Quadri
 * @date 8/15/2018
 */

public abstract class Tile {
	private BufferedImage tile;
	private int rotations;
	
	/**
	 * Does not allow rotation of tile
	 * @param tile
	 */
	public Tile (BufferedImage tile) {
		setTile(tile);
		setRotations(0);
	}
	
	/**
	 * Will rotate tile 90 degrees, @param rotations times
	 * @param tile
	 * @param rotations
	 */
	public Tile (BufferedImage tile, int rotations) {
		setTile(tile);
		setRotations(rotations);
	}
	
	public int getRotations() {
		return this.rotations;
	}
	
	/**
	 * This method sets rotation for the tile 90 degrees * @param rotation times
	 * @param rotations range [1-3]
	 */
	public void setRotations(int rotations) {
		if (rotations < 1 || rotations > 3)
			this.rotations = 0;
		else
			this.rotations = rotations;
	}

	public BufferedImage getTile() {
		return tile;
	}

	public void setTile(BufferedImage tile) {
		this.tile = tile;
	}
}
