package com.nfehs.librarygames.games.go;

import com.nfehs.librarygames.Game;
import com.nfehs.librarygames.games.BoardGame;
import com.nfehs.librarygames.games.Piece;
import com.nfehs.librarygames.games.Tile;
import com.nfehs.librarygames.games.go.pieces.Stone;
import com.nfehs.librarygames.games.go.tiles.*;
import com.nfehs.librarygames.screens.GameScreen;

/**
 * This is the BoardGame class for Go
 * @author Patrick Kenney and Syed Quadri
 * @date 8/15/2018
 */

public class Go extends BoardGame {
	private int whiteStonesCaptured;
	private int blackStonesCaptured;
	
	public Go(String gameKey, int gameType, String player1, String player2, boolean player1Turn,
			int lastMove, int whiteStonesCaptured, int blackStonesCaptured, int winner, String board) {
		super(gameKey, gameType, player1, player2, player1Turn, lastMove, winner, board);
		setWhiteStonesCaptured(whiteStonesCaptured);
		setBlackStonesCaptured(blackStonesCaptured);
	}

	/**
	 * Makes a given move if it is legal, otherwise return a null String
	 * @param board1D
	 * @param isPlayer1Turn
	 * @param lastMove1D
	 * @param moveFrom1D
	 * @param moveTo1D
	 * @return
	 */
	public static String makeMove(String board1D, boolean isPlayer1Turn, int lastMove1D, int moveFrom1D, int moveTo1D) {
		return board1D.charAt(moveTo1D) == '0' ? board1D.substring(0, moveTo1D) + (isPlayer1Turn ? "1" : "2") + board1D.substring(moveTo1D + 1) : null;
	}

	/**
	 * Returns true if the proposed move is allowed
	 */
	public boolean validMove(int x, int y) {
		return true;
	}
	
	// TODO
	private static int getLiberties() {
		return 0;
	}

	/**
	 * Updates a Go game specifically after receiving a 09 packet or 08 if on game screen
	 * Returns false if not current game
	 * @Override
	 */
	public boolean update(String gameKey, String board, int lastMove, int player1Score, int player2Score) {
		if (!super.update(gameKey, board, lastMove, player1Score, player2Score))
			return false;
		setWhiteStonesCaptured(player1Score);
		setBlackStonesCaptured(player2Score);
		return true;
	}

	@Override
	public void handleMouseEnterTile(int[] coordinates) {
		if (!isPlayerTurn())
			return;
		if (!validMove(coordinates[0], coordinates[1]))
			return;
		if (Game.screen instanceof GameScreen)
			((GameScreen) Game.screen).displayPieceShadow(coordinates[0], coordinates[1]);
	}

	@Override
	public void handleMouseLeaveTile() {
		if (!isPlayerTurn())
			return;
		if (Game.screen instanceof GameScreen)
			((GameScreen) Game.screen).removePieceShadow();
	}

	@Override
	public void handleMouseClickTile(int[] coordinates) {
		if (!isPlayerTurn())
			return;
		if (!validMove(coordinates[0], coordinates[1]))
			return;
		// first remove piece shadow
		if (Game.screen instanceof GameScreen)
			((GameScreen) Game.screen).removePieceShadow();
		
		// get move coordinates in 1D
		int move = getLinearCoordinate(coordinates[0], coordinates[1]);
		
		// send packet
		Game.sendMove(move, move);
	}
	
	// these methods are not used in go
	@Override
	public void handleMouseEnterPiece(int[] coordinates) {}
	@Override
	public void handleMouseLeavePiece() {}
	@Override
	public void handleMouseClickPiece(int[] coordinates) {}
	

	@Override
	protected void setTiles() {
		int boardLength = getBoard().length;
		
		// set tiles
		Tile[][] tiles = new Tile[boardLength][boardLength];
		//first set center tiles and edge tiles
		for (int i = 1; i < tiles.length - 1; i++) {
			for (int j = 1; j < tiles.length - 1; j++)
				tiles[i][j] = new GoTile(0, getGameType(), 0);
			tiles[i][0] = new GoTile(1, getGameType(), 1);					// set left edges
			tiles[0][i] = new GoTile(1, getGameType(), 0);					// top
			tiles[i][tiles.length-1] = new GoTile(1, getGameType(), 3);		// right
			tiles[tiles.length-1][i] = new GoTile(1, getGameType(), 2);		// bottom
		}
		// set corner tiles
		tiles[0][0] = new GoTile(2, getGameType(), 0);
		tiles[0][tiles.length-1] = new GoTile(2, getGameType(), 3);
		tiles[tiles.length-1][tiles.length-1] = new GoTile(2, getGameType(), 2);
		tiles[tiles.length-1][0] = new GoTile(2, getGameType(), 1);
		
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
					pieces[i][j] = new Stone(getGameType(), true);
				else if (board[i][j] == '2')
					pieces[i][j] = new Stone(getGameType(), false);
		this.pieces = pieces;
	}

	public int getBlackStonesCaptured() {
		return blackStonesCaptured;
	}

	public void setBlackStonesCaptured(int blackStonesCaptured) {
		this.blackStonesCaptured = blackStonesCaptured;
	}

	public int getWhiteStonesCaptured() {
		return whiteStonesCaptured;
	}

	public void setWhiteStonesCaptured(int whiteStonesCaptured) {
		this.whiteStonesCaptured = whiteStonesCaptured;
	}
}
