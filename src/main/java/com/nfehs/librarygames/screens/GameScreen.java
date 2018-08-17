package com.nfehs.librarygames.screens;

import java.awt.AlphaComposite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.nfehs.librarygames.Game;
import com.nfehs.librarygames.games.Piece;
import com.nfehs.librarygames.games.Tile;
import com.nfehs.librarygames.games.go.pieces.Stone;

/**
 * This class handles the game being played
 * 
 * @author Patrick Kenney, Syed Quadri
 * @date 6/14/2018
 */

public class GameScreen extends Screen {
	private static final double imageTileSize = 100;
	private static double boardSize;
	
	private double scale;
	private double screenTileSize;
	private int topLeftX;
	private int topLeftY;

	private JLabel title;
	private JButton back;
	private JButton pass;
	private JButton resign;
	
	
	private JLayeredPane pane;
	private JLabel[][] board;
	private JLabel[][] pieces;
	// used in Go games
	private JLabel shadowPiece;
	
	private JPanel gameInfo;
	private JLabel moveCount;
	private JLabel player1Icon;
	private JLabel player2Icon;
	private JLabel player1User;
	private JLabel player2User;
	
	private JPanel capturedPieces;
	private JLabel captured;
	private JLabel[] player1Captured;
	private JLabel[] player2Captured;
	private JLabel[] player1CapturedNumber;
	private JLabel[] player2CapturedNumber;
	
	private JPanel chatInterface;
	private JTextArea chatBox;
	private JTextField chat;

	public GameScreen() {
		super(false);
		
		// get the scale for tiles (all images are 50pixels), tile size, and top left coordinates
		int rowLength = Game.getBoardGame().getBoard().length;
		setScreenTileSize((int) (getBoardSize() / rowLength));
		setScale(getScreenTileSize() / imageTileSize);
		//System.out.println(getScale());
		setTopLeftY((int) Game.screenSize.getHeight() / 25);
		setTopLeftX((int) (Game.screenSize.getWidth() / 2 - (getScreenTileSize() * Game.getBoardGame().getBoard().length / 2)));
		
		title = new JLabel(Game.getBoardGame().getGameTitle());
		Game.mainWindow.add(title);
		title.setBounds(getTopLeftX(), getTopLeftY() - 20, 250, 15);
		
		back = new JButton("RETURN");
		Game.mainWindow.add(back);
		back.setBounds(getTopLeftX(), getTopLeftY() + (int) getBoardSize() + 10, 150, 30);
		back.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("RETURN TO GAMES LIST CLICKED");
				Game.openActiveGamesScreen();
			}
		});
		
		resign = new JButton("RESIGN");
		Game.mainWindow.add(resign);
		resign.setBounds(getTopLeftX() + (int) getBoardSize() - 150, getTopLeftY() + (int) getBoardSize() + 10, 150, 30);
		resign.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("RESIGN CLICKED");
				// send resignation packet to server TODO
			}
		});

		shadowPiece = new JLabel();
		pass = new JButton("PASS");
		// if playing go, set the icon for shadow piece and define the pass button
		if (Game.getBoardGame().getGameType() < 3) {
			shadowPiece.setIcon(new ImageIcon(getProperImage(Stone.getPiece(Game.getBoardGame().getGameType(), Game.getBoardGame().isPlayer1()), .75f)));
			
			Game.mainWindow.add(pass);
			pass.setBounds((int) (Game.screenSize.getWidth() / 2 - 75), getTopLeftY() + (int) getBoardSize() + 10, 150, 30);
			pass.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("PASS CLICKED");
					// send move pass to server
					Game.sendMove(-1, -1);
				}
			});
		}
		
		
		
		gameInfo = new JPanel();
		Game.mainWindow.add(gameInfo);
		int panelWidth = (int) (Game.screenSize.getWidth() - getTopLeftX() - (int) getBoardSize()) * 9 / 10;
		gameInfo.setBounds(	getTopLeftX() + (int) getBoardSize() + panelWidth / 20,
							getTopLeftY() + (int) getBoardSize() / 8, panelWidth, 250);
		gameInfo.setLayout(null);
		
		moveCount = new JLabel();
		gameInfo.add(moveCount);
		moveCount.setBounds(25, 25, 300, 50);
		moveCount.setFont(new Font("Serif", Font.PLAIN, 50));
		
		player1Icon = new JLabel();
		gameInfo.add(player1Icon);
		player1Icon.setBounds(25, 100, 50, 50);
		
		player1User = new JLabel(Game.getBoardGame().getPlayer1());
		gameInfo.add(player1User);
		player1User.setBounds(100, 100, 300, 50);
		player1User.setFont(new Font("Serif", Font.PLAIN, 50));
		
		player2Icon = new JLabel();
		gameInfo.add(player2Icon);
		player2Icon.setBounds(25, 175, 50, 50);
		
		player2User = new JLabel(Game.getBoardGame().getPlayer2());
		gameInfo.add(player2User);
		player2User.setBounds(100, 175, 300, 50);
		player2User.setFont(new Font("Serif", Font.PLAIN, 50));
		
		
		
		capturedPieces= new JPanel();
		Game.mainWindow.add(capturedPieces);
		capturedPieces.setBounds(	getTopLeftX() + (int) getBoardSize() + panelWidth / 20,
							getTopLeftY() + (int) getBoardSize() / 2, panelWidth, 250);
		capturedPieces.setLayout(null);
		
		captured = new JLabel("Captured Pieces: ");
		capturedPieces.add(captured);
		captured.setBounds(25, 25, panelWidth - 50, 50);
		captured.setFont(new Font("Serif", Font.PLAIN, 50));
		
		
		
		// fully define player captured pieces here TODO
		// do number in update
		
		chatInterface = new JPanel();
		Game.mainWindow.add(chatInterface);
		chatInterface.setBounds(panelWidth / 20, getTopLeftY(), panelWidth, (int) getBoardSize() * 7 / 8);
		chatInterface.setLayout(null);
		
		chatBox = new JTextArea("");
		chatInterface.add(chatBox);
		chatBox.setBounds(5, 5, chatInterface.getWidth() - 10, chatInterface.getHeight() - 40);
		chatBox.setEditable(false);
		chatBox.setFont(new Font("Serif", Font.PLAIN, 15));
		//chatBox.setMargin(new Insets(chatBox.getHeight()-15, 0, 0, 0));
		
		chat = new JTextField();
		chatInterface.add(chat);
		chat.setBounds(5, chatInterface.getHeight() - 35, chatInterface.getWidth() - 10, 30);
		chat.setFont(new Font("Serif", Font.PLAIN, 15));
		chat.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == '\n')
					Game.sendChat(chat.getText());
			}
		});
		
		
		
		pane = new JLayeredPane();
		pane.setBounds(getTopLeftX(), getTopLeftY(), ((int) getScreenTileSize())*rowLength, ((int) getScreenTileSize())*rowLength);
		pane.setOpaque(false);
		Game.mainWindow.add(pane);
		
		// add board to screen and update pieces
		pieces = new JLabel[rowLength][rowLength];
		addBoard();
		updateBoard();

		Game.mainWindow.repaint();
	}
	
	/**
	 * This method adds tiles of board
	 * Should only be called by constructor
	 */
	private void addBoard() {
		Tile[][] tiles = Game.getBoardGame().getTiles();
		board = new JLabel[tiles.length][tiles.length];
		
		for (int i = 0; i < board.length; i++)
			for (int j = 0; j < board.length; j++) {
				board[i][j] = new JLabel(new ImageIcon(tiles[i][j].getTile()));
				pane.add(board[i][j], JLayeredPane.FRAME_CONTENT_LAYER);
				
				board[i][j].setBounds((int) (j*getScreenTileSize()), (int) (i*getScreenTileSize()), (int) getScreenTileSize(), (int) getScreenTileSize());
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
	 * Returns a proper image in transparency
	 * @param img
	 * @param transparent
	 * @return
	 */
	private BufferedImage getProperImage(BufferedImage img, float transparent) {
		BufferedImage properImage = new BufferedImage((int) getScreenTileSize(), (int) getScreenTileSize(), BufferedImage.TYPE_INT_ARGB);
		
		// change transparency
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparent);
		
		// draw image
		Graphics2D g2d = properImage.createGraphics();
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
		if (Game.getBoardGame().isPlayerTurn())
			pass.setEnabled(true);
		else
			pass.setEnabled(false);
		Piece[][] gamePieces = Game.getBoardGame().getPieces();
		
		for (int i = 0; i < pieces.length; i++)
			for (int j = 0; j < pieces.length; j++) {
				if (pieces[i][j] != null)
					pane.remove(pieces[i][j]);
				if (gamePieces[i][j] != null) {
					pieces[i][j] = new JLabel(new ImageIcon(gamePieces[i][j].getPiece()));
					pane.add(pieces[i][j], JLayeredPane.DEFAULT_LAYER);
					
					pieces[i][j].setBounds((int) (j*getScreenTileSize()), (int) (i*getScreenTileSize()), (int) getScreenTileSize(), (int) getScreenTileSize());
					pieces[i][j].addMouseListener(new MouseAdapter() {
						public void mouseClicked(MouseEvent e) {
							Game.getBoardGame().handleMouseClickPiece(getCoordinates((JLabel) e.getSource()));
						}
						public void mouseEntered(MouseEvent e) {
							Game.getBoardGame().handleMouseEnterPiece(getCoordinates((JLabel) e.getSource()));
						}
						public void mouseExited(MouseEvent e) {
							Game.getBoardGame().handleMouseLeavePiece();
						}
						private int[] getCoordinates (JLabel piece) {
							int[] coordinates = new int[2];
							for (int i = 0; i < pieces.length; i++)
								for (int j = 0; j < pieces.length; j++)
									if (piece.equals(pieces[i][j])) {
										coordinates[0] = i;
										coordinates[1] = j;
									}
							return coordinates;
						}
					});
				}
			}
		// highlight last move
		int lastMove = Game.getBoardGame().getLastMove();
		if (lastMove > -1)
			pieces[lastMove / pieces.length][lastMove % pieces.length].setIcon(
					new ImageIcon(gamePieces[lastMove / pieces.length][lastMove % pieces.length].getLastMovePiece()));
		
		// update game info panel
		moveCount.setText("Move: " + (Game.getBoardGame().getMoves() + 1));
		player1Icon.setIcon(new ImageIcon(Game.getBoardGame().getPlayer1Icon()));
		player2Icon.setIcon(new ImageIcon(Game.getBoardGame().getPlayer2Icon()));
		
		// TODO handle captured pieces update
		
		// handle a pass
		if (lastMove == -1) {
			new Thread (new Runnable () {
				public void run() {
					try {
						JLabel pass = new JLabel((Game.getBoardGame().isPlayer1Turn() 
								? Game.getBoardGame().getPlayer2() : Game.getBoardGame().getPlayer1())
								+ "   Passed", SwingConstants.CENTER);
						pane.add(pass, JLayeredPane.POPUP_LAYER);
						pass.setBounds(0, (int) getBoardSize() / 2 - (int) getScreenTileSize() / 2,
										(int) getBoardSize(), (int) getScreenTileSize());
						pass.setFont(new Font("Serif", Font.PLAIN, 50));
						pass.setOpaque(true);
						
						Thread.sleep(1500);
						pane.remove(pass);
						pane.repaint();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
		
		pane.repaint();
		gameInfo.repaint();
	}
	
	/**
	 * Updates the game chat
	 * @param newText
	 * @param senderKey
	 */
	public void updateChat(String newText, String senderKey) {
		// update chat text
		chatBox.setText(chatBox.getText() + "\n" + newText);
		
		// if the user sent the text, delete the users current text
		if (senderKey.equals(Game.getPlayer().getUser_key()))
			chat.setText("");
		
		chatInterface.repaint();
	}
	
	/**
	 * Used in Go games, this will show a shadow of where a piece would be placed on click
	 * @param piece
	 * @param x
	 * @param y
	 */
	public void displayPieceShadow(int x, int y) {
		// adds shadow piece to screen
		pane.add(shadowPiece, JLayeredPane.PALETTE_LAYER);
		
		shadowPiece.setBounds((int) (y*getScreenTileSize()), (int) (x*getScreenTileSize()), (int) getScreenTileSize(), (int) getScreenTileSize());
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
		Game.mainWindow.remove(pass);
		Game.mainWindow.remove(resign);
		Game.mainWindow.remove(gameInfo);
		Game.mainWindow.remove(capturedPieces);
		Game.mainWindow.remove(chatInterface);
		
		back = null;
		title = null;
		pane = null;
		board = null;
		shadowPiece = null;
		pass = null;
		resign = null;
		gameInfo = null;
		capturedPieces = null;
		chatInterface = null;
		
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

	/**
	 * @return the boardSize
	 */
	public static double getBoardSize() {
		return boardSize;
	}

	/**
	 * @param boardSize the boardSize to set
	 */
	public static void setBoardSize(double boardSize) {
		GameScreen.boardSize = boardSize;
	}

	public static double getImagetilesize() {
		return imageTileSize;
	}

}
