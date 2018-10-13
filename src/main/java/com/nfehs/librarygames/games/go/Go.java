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
	private int player1Score;
	private int player2Score;
	private String initialBoard;
	
	/**
	 * Constructor for a game of Go
	 * @param gameKey
	 * @param gameType
	 * @param player1
	 * @param player2
	 * @param moves
	 * @param penultMove
	 * @param lastMove
	 * @param winner
	 * @param player1OnGame
	 * @param player2OnGame
	 * @param board
	 * @param extraData
	 */
	public Go(String gameKey, int gameType, String player1, String player2, int moves,
			int penultMove, int lastMove, int winner, boolean player1OnGame, boolean player2OnGame, String board, String extraData) {
		super(gameKey, gameType, player1, player2, moves, penultMove, lastMove, winner, player1OnGame, player2OnGame, board);
		String[] goData = extraData.split(",");
		
		try {
			setWhiteStonesCaptured(Integer.parseInt(goData[0]));
			setBlackStonesCaptured(Integer.parseInt(goData[1]));
			setPlayer1Score(Integer.parseInt(goData[2]));
			setPlayer2Score(Integer.parseInt(goData[3]));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		if (winner == 1 || winner == 2)
			setScoreInfo("Score: " + getPlayer1Score() + " - " + getPlayer2Score() + ".5");
	}
	
	@Override
	public void makeMoveOffline(int movingFrom, int movingTo) {
		// first handle resignation case
		if (movingTo == -2)
			if (isPlayer1Turn())
				updateWinner(4);
			else
				updateWinner(3);
		
		// get new move
		String oldBoard = getInitialBoard();
		String newBoard = makeMove(oldBoard, getMoves() % 2 == 0, getPenultMove(), getLastMove(), movingFrom, movingTo);
		if (newBoard == null)
			return;
		setBoard(newBoard);

		// if the user did not pass, calculate score
		if (movingTo > -1) {
			int capturedPieces = -1;	// for use in go games
			// calculate number of captured pieces in a go game
			String olderBoard = Go.retrieveOldBoard(newBoard);
			String newestBoard = Go.retrieveCurrentBoard(newBoard);
			for (int i = 0; i < olderBoard.length(); i++)
				if (olderBoard.charAt(i) != newestBoard.charAt(i))
					capturedPieces++;
			if (getMoves() % 2 == 0)
				setWhiteStonesCaptured(getWhiteStonesCaptured() + capturedPieces);
			else
				setBlackStonesCaptured(getBlackStonesCaptured() + capturedPieces);
		}
		
		// if the user passed, it is end of game if last move was a pass too
		if (movingTo == -1 && getLastMove() == -1) {
			// get player scores
			int[] territoryScores = Go.calculateTerritory(retrieveCurrentBoard(oldBoard));
			setPlayer1Score(territoryScores[0] + getWhiteStonesCaptured());
			
			// apply Komi
			switch (getGameType()) {
				case 2:				setPlayer2Score(territoryScores[1] + getBlackStonesCaptured() + 6);
									break;
				case 1:				setPlayer2Score(territoryScores[1] + getBlackStonesCaptured() + 3);
									break;
				default: 			setPlayer2Score(territoryScores[1] + getBlackStonesCaptured() + 1);
			}
			updateWinner(1);
			if (getPlayer2Score() >= getPlayer1Score())
				updateWinner(2);
			setScoreInfo("Score: " + getPlayer1Score() + " - " + getPlayer2Score() + ".5");
		}

		setPieces();
		setPlayer1Turn(!isPlayer1Turn());
		setMoves(getMoves() + 1);
		setPlayer1(!isPlayer1());
		setPenultMove(getLastMove());
		setLastMove(movingTo);
	}

	/**
	 * Makes a given move if it is legal, otherwise return a null String
	 * @param board1D
	 * @param isPlayer1Turn
	 * @param lastMove1D
	 * @param moveFrom1D
	 * @param moveTo1D
	 * @return
	 * @Override
	 */
	public static String makeMove(String board1D, boolean isPlayer1Turn, int penultMove1D, int lastMove1D, int moveFrom1D, int moveTo1D) {
		// check pass or resign first
		if (moveTo1D < 0)
			return board1D;
		
		// get 2D data and implement placement of move
		String currBoard = retrieveCurrentBoard(board1D);
		char[][] paddedBoard = getPaddedBoard(currBoard);
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
		if (hasLiberty) {
			String newBoard = buildBoard(paddedBoard);
			// check Ko rule
			if (!newBoard.equals(retrieveOldBoard(board1D)))
				// encode in current board to newBoard
				return combineBoards(newBoard, currBoard);
		}
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
	 *
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
	}*/
	
	/**
	 * Returns true if the placement has at least one liberty
	 * @param board
	 * @param x
	 * @param y
	 * @return
	 *
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
			else
				paddedBoard = paintBoard(paddedBoard, x, y+1, '$', opposingPiece);
		if (paddedBoard[x][y-1] == opposingPiece)
			if (!groupHasLiberty(paddedBoard, x, y-1))
				return true;
			else
				paddedBoard = paintBoard(paddedBoard, x, y-1, '$', opposingPiece);
		if (paddedBoard[x+1][y] == opposingPiece)
			if (!groupHasLiberty(paddedBoard, x+1, y))
				return true;
			else
				paddedBoard = paintBoard(paddedBoard, x+1, y, '$', opposingPiece);
		if (paddedBoard[x-1][y] == opposingPiece)
			if (!groupHasLiberty(paddedBoard, x-1, y))
				return true;
		return false;
	}*/
	
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
	public boolean update(String gameKey, String board, int penultMove, int lastMove, int moves, int winner, boolean player1OnGame, boolean player2OnGame, String extraData) {
		if (!super.update(gameKey, board, penultMove, lastMove, moves, winner, player1OnGame, player2OnGame, extraData))
			return false;
		String[] goData = extraData.split(",");
		
		try {
			setWhiteStonesCaptured(Integer.parseInt(goData[0]));
			setBlackStonesCaptured(Integer.parseInt(goData[1]));
			setPlayer1Score(Integer.parseInt(goData[2]));
			setPlayer2Score(Integer.parseInt(goData[3]));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return false;
		}
		updateWinner(winner);
		if (winner == 1 || winner == 2)
			setScoreInfo("Score: " + getPlayer1Score() + " - " + getPlayer2Score() + ".5");
		return true;
	}
	
	@Override
	public BufferedImage getPlayer1Icon() {
		if (getWinner() == null && isPlayer1Turn())
			return GoPiece.getPlayer1IconPlaying();
		if (getPlayer1().equals(getWinner()))
			return GoPiece.getPlayer1IconPlaying();
		return GoPiece.getPlayer1Icon();
	}
	@Override
	public BufferedImage getPlayer2Icon() {
		if (getWinner() == null && !isPlayer1Turn())
			return GoPiece.getPlayer2IconPlaying();
		if (getPlayer2().equals(getWinner()))
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
		if (isPlayerIsSpectating())
			return;
		int movingTo = getLinearCoordinate(coordinates[0], coordinates[1]);
		if (makeMove(getInitialBoard(), isPlayer1Turn(), 0, 0, movingTo, movingTo) == null)
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
		if (isPlayerIsSpectating())
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
		if (isPlayerIsSpectating())
			return;
		int movingTo = getLinearCoordinate(coordinates[0], coordinates[1]);
		if (makeMove(getInitialBoard(), isPlayer1Turn(), 0, 0, movingTo, movingTo) == null)
			return;
		// first remove piece shadow
		if (Game.screen instanceof GameScreen)
			((GameScreen) Game.screen).removePieceShadow();
		
		// get move coordinates in 1D
		int move = getLinearCoordinate(coordinates[0], coordinates[1]);
		
		// send packet
		Game.sendMove(move, move);
		
		// set player turn false if online
		if (Game.isOnline())
			setPlayerTurn(false);
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

	/**
	 * @return the player1Score
	 */
	public int getPlayer1Score() {
		return player1Score;
	}

	/**
	 * @param player1Score the player1Score to set
	 */
	public void setPlayer1Score(int player1Score) {
		this.player1Score = player1Score;
	}

	/**
	 * @return the player2Score
	 */
	public int getPlayer2Score() {
		return player2Score;
	}

	/**
	 * @param player2Score the player2Score to set
	 */
	public void setPlayer2Score(int player2Score) {
		this.player2Score = player2Score;
	}

	/**
	 * Returns the board and sets the old board
	 * @Override
	 */
	public char[][] initializeBoard(String board) {
		// get length of sides
		int arrayLength = (int) Math.sqrt(board.length());
		
		// build 2D board
		char[][] board2D = new char[arrayLength][arrayLength];
		for (int i = 0; i < board2D.length; i++)
			for (int j = 0; j < board2D.length; j++)
				board2D[i][j] = (char) ((board.charAt(i*arrayLength + j) - '0') % 3 + '0');
		
		setInitialBoard(board);
		return board2D;
	}

	/**
	 * @param board raw board data for a game of Go
	 * @return the last board orientation in a game of Go given the raw board data
	 */
	public static String retrieveCurrentBoard(String board) {
		String currBoard = "";
		for (char c : board.toCharArray())
			currBoard += (char) ((c - '0') % 3 + '0');
		return currBoard;
	}

	/**
	 * @param board raw board data for a game of Go
	 * @return the last board orientation in a game of Go given the raw board data
	 */
	public static String retrieveOldBoard(String board) {
		String oldBoard = "";
		for (char c : board.toCharArray())
			oldBoard += (char) ((c - '0') / 3 + '0');
		return oldBoard;
	}

	/**
	 * @param newerBoard newerBoard.length() = olderBoard.length()
	 * @param olderBoard has piece positional data as a 1D String
	 * @return a 1D board of length newerBoard.length(), olderBoard positions are by multiple of 3
	 */
	private static String combineBoards(String newerBoard, String olderBoard) {
		String combinedBoard = "";
		for (int i = 0; i < newerBoard.length(); i++)
			combinedBoard += (char) (newerBoard.charAt(i) + (olderBoard.charAt(i) - '0') * 3);
		return combinedBoard;
	}

	public String getInitialBoard() {
		return initialBoard;
	}

	public void setInitialBoard(String initialBoard) {
		this.initialBoard = initialBoard;
	}
}
