package com.nfehs.librarygames.games.go;

import com.nfehs.librarygames.games.BoardGame;
import com.nfehs.librarygames.games.Piece;
import com.nfehs.librarygames.games.Tile;
import com.nfehs.librarygames.games.go.pieces.Stone;
import com.nfehs.librarygames.games.go.tiles.*;

/**
 * This is the BoardGame class for Go
 * @author Patrick Kenney and Syed Quadri
 * @date 8/15/2018
 */

public class Go extends BoardGame {
	private int blackStonesCaptured;
	private int whiteStonesCaptured;
	
	public Go(String gameKey, int gameType, String player1, String player2, boolean player1Turn,
			int lastMove, String board) {
		super(gameKey, gameType, player1, player2, player1Turn, lastMove, board);
	}

	/**
	 * Returns true if the proposed move is allowed
	 */
	public boolean validMove(int x, int y) {
		return true;
	}

	@Override
	public void handleMouseEnterTile(int[] coordinates) {
		if (!validMove(coordinates[0], coordinates[1]))
			return;
	}

	@Override
	public void handleMouseLeaveTile(int[] coordinates) {
		// TODO remove shadow
	}

	@Override
	public void handleMouseClickTile(int[] coordinates) {
		if (!validMove(coordinates[0], coordinates[1]))
			return;
	}

	@Override
	protected void setTiles() {
		int boardLength = getBoard().length;
		
		// set tiles
		Tile[][] tiles = new Tile[boardLength][boardLength];
		//first set center tiles and edge tiles
		for (int i = 1; i < tiles.length - 1; i++) {
			for (int j = 1; j < tiles.length - 1; j++)
				tiles[i][j] = new CenterTile();
			tiles[i][0] = new EdgeTile(1);					// set left edges
			tiles[0][i] = new EdgeTile(0);					// top
			tiles[i][tiles.length-1] = new EdgeTile(3);		// right
			tiles[tiles.length-1][i] = new EdgeTile(2);		// bottom
		}
		// set corner tiles
		tiles[0][0] = new CornerTile(0);
		tiles[0][tiles.length-1] = new CornerTile(3);
		tiles[tiles.length-1][tiles.length-1] = new CornerTile(2);
		tiles[tiles.length-1][0] = new CornerTile(1);
		
		this.tiles = tiles;
	}

	@Override
	protected void setPieces() {
		char[][] board = getBoard();
		Piece[][] pieces = new Piece[board.length][board.length];
		
		// set pieces, '0' is empty space, '1' is black stone, '2' is white stone
		for (int i = 0; i < pieces.length; i++)
			for (int j = 0; j < pieces.length; j++)
				if (board[i][j] == '1')
					pieces[i][j] = new Stone(true);
				else if (board[i][j] == '2')
					pieces[i][j] = new Stone(false);
		this.pieces = pieces;
	}
}
