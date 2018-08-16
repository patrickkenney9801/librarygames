package com.nfehs.librarygames.screens;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;

import com.nfehs.librarygames.Game;
import com.nfehs.librarygames.games.Piece;
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
	
	private JLayeredPane pane;
	private JLabel[][] board;
	
	// used in Go games
	private JLabel shadowPiece;

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
		int rowLength = Game.getBoardGame().getBoard().length;
		setScreenTileSize((int) ((Game.screenSize.getHeight() * 4 / 5) / rowLength));
		setScale(getScreenTileSize() / imageTileSize);
		//System.out.println(getScale());
		setTopLeftY((int) Game.screenSize.getHeight() / 10);
		setTopLeftX((int) (Game.screenSize.getWidth() / 2 - (getScreenTileSize() * Game.getBoardGame().getBoard().length / 2)));

		shadowPiece = new JLabel();
		pane = new JLayeredPane();
		pane.setBounds(getTopLeftX(), getTopLeftY(), ((int) getScreenTileSize())*rowLength, ((int) getScreenTileSize())*rowLength);
		pane.setOpaque(false);
		Game.mainWindow.add(pane);
		
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
				board[i][j] = new JLabel(new ImageIcon(getProperImage(tiles[i][j].getTile(), tiles[i][j].getRotations(), 1)));
				pane.add(board[i][j], JLayeredPane.FRAME_CONTENT_LAYER);
				
				board[i][j].setBounds((int) (i*getScreenTileSize()), (int) (j*getScreenTileSize()), (int) getScreenTileSize(), (int) getScreenTileSize());
				board[i][j].addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						Game.getBoardGame().handleMouseClickTile(getCoordinates((JLabel) e.getSource()));
					}
					public void mouseEntered(MouseEvent e) {
						Game.getBoardGame().handleMouseEnterTile(getCoordinates((JLabel) e.getSource()));
					}
					public void mouseExited(MouseEvent e) {
						Game.getBoardGame().handleMouseLeaveTile();
					}
					
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
		pane.repaint();
	}
	
	/**
	 * Returns a proper image in size and rotation
	 * @param img
	 * @param rotations
	 * @param transparent
	 * @return
	 */
	private BufferedImage getProperImage(BufferedImage img, int rotations, float transparent) {
		BufferedImage properImage = new BufferedImage((int) getScreenTileSize(), (int) getScreenTileSize(), BufferedImage.TYPE_INT_ARGB);
		AffineTransform at = new AffineTransform();
		
		// rotate image about center
		at.rotate(Math.PI/2 * rotations, getScreenTileSize() / 2, getScreenTileSize() / 2);
		// scale image to right size
		at.scale(getScale(), getScale());
		
		// change transparency
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparent);
		
		// draw image
		Graphics2D g2d = properImage.createGraphics();
		g2d.setTransform(at);
		g2d.setComposite(ac);
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
	 * Used in Go games, this will show a shadow of where a piece would be placed on click
	 * @param piece
	 * @param x
	 * @param y
	 */
	public void displayPieceShadow(Piece piece, int x, int y) {
		// set shadow pieces icon
		shadowPiece.setIcon(new ImageIcon(getProperImage(piece.getPiece(), 0, .75f)));
		pane.add(shadowPiece, JLayeredPane.DEFAULT_LAYER);
		
		shadowPiece.setBounds((int) (x*getScreenTileSize()), (int) (y*getScreenTileSize()), (int) getScreenTileSize(), (int) getScreenTileSize());
		pane.repaint();
	}
	
	/**
	 * Used in Go games, this will remove a shadow piece
	 */
	public void removePieceShadow() {
		pane.remove(shadowPiece);
		
		pane.repaint();
	}

	@Override
	public void exit() {
		exitParentGUI();
		
		Game.mainWindow.remove(back);
		Game.mainWindow.remove(title);
		Game.mainWindow.remove(pane);
		
		back = null;
		title = null;
		pane = null;
		board = null;
		shadowPiece = null;
		
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
