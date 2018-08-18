package com.nfehs.librarygames.games.go;

import java.awt.image.BufferedImage;

import com.nfehs.librarygames.Game;
import com.nfehs.librarygames.games.BoardGame;
import com.nfehs.librarygames.games.Piece;
import com.nfehs.librarygames.games.Tile;
import com.nfehs.librarygames.games.go.pieces.GoPiece;
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
	
	public Go(String gameKey, int gameType, String player1, String player2, int moves,
			int penultMove, int lastMove, int whiteStonesCaptured, int blackStonesCaptured, int winner, String board) {
		super(gameKey, gameType, player1, player2, moves, penultMove, lastMove, winner, board);
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
	public static String makeMove(String board1D, boolean isPlayer1Turn, int penultMove1D, int lastMove1D, int moveFrom1D, int moveTo1D) {
		// check pass or resign first
		if (moveTo1D < 0)
			return board1D;
		
		// check Ko rule
		if (moveTo1D == penultMove1D)
			return null;
		
		// get 2D data and implement placement of move
		char[][] paddedBoard = getPaddedBoard(board1D);
		int[] coors = get2DCoordinates(moveTo1D, paddedBoard.length-2);
		int x = coors[0]+1;
		int y = coors[1]+1;
		paddedBoard[x][y] = isPlayer1Turn ? '1' : '2';
		char piece = paddedBoard[x][y];
		char opposingPiece = piece == '1' ? '2' : '1';
		
		// check if the group of stones created has a liberty
		boolean hasLiberty = groupHasLiberty(paddedBoard, x, y);
		// if not reset paddedBoard
		paddedBoard = paintBoard(paddedBoard, x, y, '$', piece);
		
		// finally test if a surrounding opposing stone is captured, if so the group has a liberty
		// boards are not unpainted if the group has no liberties
		// unpaint if they do so that they are not removed
		if (paddedBoard[x][y+1] == opposingPiece)
			if (groupHasLiberty(paddedBoard, x, y+1))
				paddedBoard = paintBoard(paddedBoard, x, y+1, '$', opposingPiece);
			else
				hasLiberty = true;
		if (paddedBoard[x][y-1] == opposingPiece)
			if (groupHasLiberty(paddedBoard, x, y-1))
				paddedBoard = paintBoard(paddedBoard, x, y-1, '$', opposingPiece);
			else
				hasLiberty = true;
		if (paddedBoard[x+1][y] == opposingPiece)
			if (groupHasLiberty(paddedBoard, x+1, y))
				paddedBoard = paintBoard(paddedBoard, x+1, y, '$', opposingPiece);
			else
				hasLiberty = true;
		if (paddedBoard[x-1][y] == opposingPiece)
			if (groupHasLiberty(paddedBoard, x-1, y))
				paddedBoard = paintBoard(paddedBoard, x-1, y, '$', opposingPiece);
			else
				hasLiberty = true;
		
		// if it is a valid move, get board in String form and return
		if (hasLiberty)
			return buildBoard(paddedBoard);
		return null;
	}

	/**
	 * Calculates the territory of each player in a Go game given its board
	 * Does not account for Seki
	 * For use by server
	 * @param board
	 * @return
	 */
	public static int[] calculateTerritory(String board) {
		int[] territoryScores = new int[2];
		
		// get 2D padded board representation
		char[][] paddedBoard = getPaddedBoard(board);
		
		// cycle through board to determine who controls empty territory or if it is neutral
		// Board Key: 0=empty, 1=black, 2=white, 3=black territory, 4=white territory, 5=neutral territory
		for (int i = 1; i < paddedBoard.length-1; i++)
			for (int j = 1; j < paddedBoard.length-1; j++)
				if (paddedBoard[i][j] == '0') {
					// when an empty intersection is found, paint its entire group '$'
					paddedBoard = paintBoard(paddedBoard, i, j, '0', '$');
					
					// check if the territory touches black stones
					boolean touchesBlack = charTouches(paddedBoard, '$', '1');
					// check if the territory touched white stones
					boolean touchesWhite = charTouches(paddedBoard, '$', '2');
					
					// if only touches black stones, set territory black territory
					if (touchesBlack && !touchesWhite)
						paddedBoard = paintBoard(paddedBoard, i, j, '$', '3');
					else if (!touchesBlack && touchesWhite)
						paddedBoard = paintBoard(paddedBoard, i, j, '$', '4');
					else
						paddedBoard = paintBoard(paddedBoard, i, j, '$', '5');
				}
		// count territories each player holds
		for (int i = 1; i < paddedBoard.length-1; i++)
			for (int j = 1; j < paddedBoard.length-1; j++)
				if (paddedBoard[i][j] == '3')
					territoryScores[0]++;
				else if (paddedBoard[i][j] == '4')
					territoryScores[1]++;
		return territoryScores;
	}
	
	/**
	 * Returns true if the given @param base touches a @param target anywhere on the board
	 * @param board
	 * @param base
	 * @param target
	 * @return
	 */
	private static boolean charTouches(char[][] board, char base, char target) {
		for (int i = 1; i < board.length-1; i++)
			for (int j = 1; j < board.length-1; j++)
				if (board[i][j] == base)
					if (	board[i][j+1] == target || board[i][j-1] == target
						 || board[i+1][j] == target || board[i-1][j] == target)
						return true;
		return false;
	}

	/**
	 * Returns true if the proposed move is allowed
	 */
	public boolean validMove(int x, int y) {
		// if the location already has a stone, the move is invalid
		if (getBoard()[x][y] != '0')
			return false;
		
		// check ko rule
		if (getPenultMove() == getLinearCoordinate(x, y))
			return false;
		
		// copy board and add new piece to the copy
		char[][] paddedCopy = getPaddedBoardCopy();
		paddedCopy[x+1][y+1] = isPlayer1() ? '1' : '2';
		
		// check if the move has any liberties, if not it is invalid
		return hasLiberties(paddedCopy, x+1, y+1);
	}
	
	/**
	 * Returns true if the placement has at least one liberty
	 * @param board
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean hasLiberties(char[][] paddedBoard, int x, int y) {
		char piece = paddedBoard[x][y];
		char opposingPiece = '2';
		if (piece == '2')
			opposingPiece = '1';
		
		// check if the group of stones created has a liberty
		if (groupHasLiberty(paddedBoard, x, y))
			return true;
		// if not reset paddedBoard
		paddedBoard = paintBoard(paddedBoard, x, y, '$', piece);
		
		// finally test if a surrounding opposing stone is captured, if so the move is valid
		// unpainting boards is not neccessary
		if (paddedBoard[x][y+1] == opposingPiece)
			if (!groupHasLiberty(paddedBoard, x, y+1))
				return true;
		if (paddedBoard[x][y-1] == opposingPiece)
			if (!groupHasLiberty(paddedBoard, x, y-1))
				return true;
		if (paddedBoard[x+1][y] == opposingPiece)
			if (!groupHasLiberty(paddedBoard, x+1, y))
				return true;
		if (paddedBoard[x-1][y] == opposingPiece)
			if (!groupHasLiberty(paddedBoard, x-1, y))
				return true;
		return false;
	}
	
	/**
	 * Returns true if a group of stones containing (x, y) has a liberty
	 * @param board
	 * @param x
	 * @param y
	 * @return
	 */
	private static boolean groupHasLiberty(char[][] board, int x, int y) {
		board = paintBoard(board, x, y, board[x][y], '$');
		for (int i = 1; i < board.length-1; i++)
			for (int j = 1; j < board.length-1; j++)
				if (board[i][j] == '$')
					if (board[i][j+1] == '0' || board[i][j-1] == '0'|| board[i+1][j] == '0' || board[i-1][j] == '0')
						return true;
		return false;
	}

	/**
	 * Updates a Go game specifically after receiving a 09 packet or 08 if on game screen
	 * Returns false if not current game
	 * @Override
	 */
	public boolean update(String gameKey, String board, int penultMove, int lastMove, int player1Score, int player2Score) {
		if (!super.update(gameKey, board, penultMove, lastMove, player1Score, player2Score))
			return false;
		setWhiteStonesCaptured(player1Score);
		setBlackStonesCaptured(player2Score);
		return true;
	}
	
	public BufferedImage getPlayer1Icon() {
		if (isPlayer1Turn())
			return GoPiece.getPlayer1IconPlaying();
		return GoPiece.getPlayer1Icon();
	}
	public BufferedImage getPlayer2Icon() {
		if (!isPlayer1Turn())
			return GoPiece.getPlayer2IconPlaying();
		return GoPiece.getPlayer2Icon();
	}
	
	/**
	 * Returns a 2D array of the images of capturable pieces
	 * col 0 are white pieces, col 1 are black pieces
	 */
	public BufferedImage[][] getCapturablePieces() {
		return GoPiece.getCapturablePieces();
	}
	
	/**
	 * Returns 2D array of the number of pieces taken
	 * col 0 are white pieces, col 1 are black pieces
	 */
	public int[][] getNumberCapturedPieces() {
		int[][] numCaptured = new int[1][2];
		numCaptured[0][0] = getWhiteStonesCaptured();
		numCaptured[0][1] = getBlackStonesCaptured();
		return numCaptured;
	}

	@Override
	public void handleMouseEnterTile(int[] coordinates) {
		if (getWinner() != null)
			return;
		if (!isPlayerTurn())
			return;
		if (!validMove(coordinates[0], coordinates[1]))
			return;
		if (Game.screen instanceof GameScreen)
			((GameScreen) Game.screen).displayPieceShadow(coordinates[0], coordinates[1]);
	}

	@Override
	public void handleMouseLeaveTile() {
		if (getWinner() != null)
			return;
		if (!isPlayerTurn())
			return;
		if (Game.screen instanceof GameScreen)
			((GameScreen) Game.screen).removePieceShadow();
	}

	@Override
	public void handleMouseClickTile(int[] coordinates) {
		if (getWinner() != null)
			return;
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
	public void handleMouseEnterCapturedPiece(int[] coordinates) {}
	@Override
	public void handleMouseLeaveCapturedPiece() {}
	@Override
	public void handleMouseClickCapturedPiece(int[] coordinates) {}
	

	@Override
	protected void setTiles() {
		int boardLength = getBoard().length;
		
		// set tiles
		Tile[][] tiles = new Tile[boardLength][boardLength];
		//first set center tiles and edge tiles
		for (int i = 1; i < tiles.length - 1; i++) {
			for (int j = 1; j < tiles.length - 1; j++)
				tiles[i][j] = new GoTile(0, getGameType(), 0);
			tiles[i][0] = new GoTile(1, getGameType(), 0);					// set left edges
			tiles[0][i] = new GoTile(1, getGameType(), 1);					// top
			tiles[i][tiles.length-1] = new GoTile(1, getGameType(), 2);		// right
			tiles[tiles.length-1][i] = new GoTile(1, getGameType(), 3);		// bottom
		}
		// set corner tiles
		tiles[0][0] = new GoTile(2, getGameType(), 0);
		tiles[0][tiles.length-1] = new GoTile(2, getGameType(), 1);
		tiles[tiles.length-1][tiles.length-1] = new GoTile(2, getGameType(), 2);
		tiles[tiles.length-1][0] = new GoTile(2, getGameType(), 3);
		
		this.tiles = tiles;
	}

	@Override
	protected void setPieces() {
		Piece[][] pieces = new Piece[getBoard().length][getBoard().length];
		
		// set pieces, '0' is empty space, '1' is black stone, '2' is white stone
		for (int i = 0; i < getBoard().length; i++)
			for (int j = 0; j < getBoard().length; j++)
				if (getBoard()[i][j] == '1')
					pieces[i][j] = new Stone(getGameType(), true);
				else if (getBoard()[i][j] == '2')
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
