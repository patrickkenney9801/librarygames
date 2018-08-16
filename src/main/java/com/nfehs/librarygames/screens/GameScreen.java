package com.nfehs.librarygames.screens;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import com.nfehs.librarygames.Game;
import com.nfehs.librarygames.games.Tile;

/**
 * This class handles the game being played
 * 
 * @author Patrick Kenney, Syed Quadri
 * @date 6/14/2018
 */

public class GameScreen extends Screen {
	private static final double imageTileSize = 100;
	private double scale;
	private double screenTileSize;
	private int topLeftX;
	private int topLeftY;

	private JLabel title;
	private JButton back;
	
	private JLabel[][] board;

	public GameScreen() {
		super(false);
		
		title = new JLabel(Game.getBoardGame().getGameTitle());
		Game.mainWindow.add(title);
		
		back = new JButton("RETURN");
		Game.mainWindow.add(back);
		back.setBounds(0, 40, 150, 30);
		back.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("RETURN TO GAMES LIST CLICKED");
				Game.openActiveGamesScreen();
			}
		});
		
		// get the scale for tiles (all images are 50pixels), tile size, and top left coordinates
		setScreenTileSize((int) ((Game.screenSize.getHeight() * 4 / 5) / Game.getBoardGame().getBoard().length));
		setScale(getScreenTileSize() / imageTileSize);
		//System.out.println(getScale());
		setTopLeftY((int) Game.screenSize.getHeight() / 10);
		setTopLeftX((int) (Game.screenSize.getWidth() / 2 - (getScreenTileSize() * Game.getBoardGame().getBoard().length / 2)));
		
		// add board to screen
		addBoard();
		
		Game.mainWindow.repaint();
	}
	
	/**
	 * This method adds tiles of board
	 * Should only be called by constructor
	 */
	private void addBoard() {
		System.out.println(getTopLeftX() + ", " + getTopLeftY() + ": " + getScreenTileSize());
		
		Tile[][] tiles = Game.getBoardGame().getTiles();
		board = new JLabel[tiles.length][tiles.length];
		
		for (int i = 0; i < board.length; i++)
			for (int j = 0; j < board.length; j++) {
				board[i][j] = new JLabel(new ImageIcon(getProperImage(tiles[i][j].getTile(), tiles[i][j].getRotations())));
				Game.mainWindow.add(board[i][j]);
				board[i][j].setBounds((int) (getTopLeftX() + i*getScreenTileSize()), (int) (getTopLeftY() + j*getScreenTileSize()),
										(int) getScreenTileSize(), (int) getScreenTileSize());
				board[i][j].addMouseListener(new MouseListener() {
					public void mouseClicked(MouseEvent e) {
						Game.getBoardGame().handleMouseClickTile(getCoordinates((JLabel) e.getSource()));
					}
					public void mouseEntered(MouseEvent e) {
						Game.getBoardGame().handleMouseEnterTile(getCoordinates((JLabel) e.getSource()));
					}
					public void mouseExited(MouseEvent e) {
						Game.getBoardGame().handleMouseLeaveTile(getCoordinates((JLabel) e.getSource()));
					}
					public void mousePressed(MouseEvent e) {}
					public void mouseReleased(MouseEvent e) {}
					
					private int[] getCoordinates (JLabel tile) {
						int[] coordinates = new int[2];
						for (int i = 0; i < board.length; i++)
							for (int j = 0; j < board.length; j++)
								if (tile.equals(board[i][j])) {
									coordinates[0] = i;
									coordinates[1] = j;
								}
						return coordinates;
					}
				});
			}
		Game.mainWindow.repaint();
	}
	
	/**
	 * Returns a proper image in size and rotation
	 * @param img
	 * @param rotations
	 * @return
	 */
	private BufferedImage getProperImage(BufferedImage img, int rotations) {
		BufferedImage properImage = new BufferedImage((int) getScreenTileSize(), (int) getScreenTileSize(), BufferedImage.TYPE_INT_ARGB);
		AffineTransform at = new AffineTransform();
		
		// rotate image about center
		at.rotate(Math.PI/2 * -rotations, getScreenTileSize() / 2, getScreenTileSize() / 2);
		// scale image to right size
		at.scale(getScale(), getScale());
		
		// draw image
		Graphics2D g2d = properImage.createGraphics();
		g2d.setTransform(at);
		g2d.drawImage(img, 0, 0, null);
		g2d.dispose();
		return properImage;
	}
	
	/**
	 * This method updates pieces on the board as well as other information
	 * that changes during game play
	 */
	public void updateBoard() {
		// TODO
	}
	
	/**
	 * Removes board from screen
	 */
	private void removeBoardAndPieces() {
		for (JLabel[] row : board)
			for (JLabel tile : row)
				Game.mainWindow.remove(tile);
		// TODO
	}

	@Override
	public void exit() {
		exitParentGUI();
		
		Game.mainWindow.remove(back);
		Game.mainWindow.remove(title);
		
		back = null;
		title = null;
		
		removeBoardAndPieces();
		board = null;
		
		Game.mainWindow.repaint();
	}

	public double getScale() {
		return scale;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}

	public double getScreenTileSize() {
		return screenTileSize;
	}

	public void setScreenTileSize(double screenTileSize) {
		this.screenTileSize = screenTileSize;
	}

	public int getTopLeftX() {
		return topLeftX;
	}

	public void setTopLeftX(int topLeftX) {
		this.topLeftX = topLeftX;
	}

	public int getTopLeftY() {
		return topLeftY;
	}

	public void setTopLeftY(int topLeftY) {
		this.topLeftY = topLeftY;
	}

}
