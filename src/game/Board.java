package game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.Timer;

import game.Shape.TileShape;

public class Board extends JPanel implements KeyListener {
	
	private Shape currentShape;
	private int xcase, ycase;
	private Timer timer;
	private Vector<Vector<Pair<Boolean, Color>>> gBoard;
	
	enum Move {
		LEFT,
		RIGHT,
		R_LEFT,
		R_RIGHT,
		DOWN;
	}
	
	public Board() {
		super();
		// The grid of the game
		xcase = 10;
		ycase = 20;
		gBoard = new Vector<>();
		// Init rows
		for (int i=0; i<ycase; i++) {
			gBoard.add(new Vector<>());
			// Init columns
			for (int j=0; j<xcase; j++) {
				gBoard.get(i).add(new Pair<>(false, Color.white));
			}
		}
		currentShape = generateShape();
		timer = new Timer(500, new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (canMove(Move.DOWN)) {
					currentShape.moveDown();
				} else {
					updateGameBoard();
					currentShape = generateShape();
				}
				repaint();
			}
		});
		addKeyListener(this);
		setFocusable(true);
	}
	
	private Shape generateShape() {
		Shape s;
		Random r = new Random();
		switch (r.nextInt(7)) {
		case 0:
			s = new Shape(Color.cyan, TileShape.ZTile);
			break;
		case 1:
			s = new Shape(Color.red, TileShape.STile);
			break;
		case 2:
			s = new Shape(Color.magenta, TileShape.JTile);
			break;
		case 3:
			s = new Shape(Color.green, TileShape.LTile);
			break;
		case 4:
			s = new Shape(Color.orange, TileShape.TTile);
			break;
		case 5:
			s = new Shape(Color.yellow, TileShape.LineTile);
			break;
		case 6:
			s = new Shape(Color.cyan, TileShape.SquareTile);
			break;
		default:
			s = new Shape(Color.cyan, TileShape.SquareTile);
			break;
		}
		// Set the initial position of the generated shape
		// x = (xcase - TileShape xcoord size)/2
		// y = 0
		s.setPos((xcase-s.getTileShapeCoords().length)/2, 0);
		return s;
	}
	
	private void updateGameBoard() {
		Color c = currentShape.getColor();
		int xpos = currentShape.getXPos();
		int ypos = currentShape.getYPos();
		boolean[][] coords = currentShape.getTileShapeCoords();
		BitSet shapeRowBS = new BitSet(Shape.GRID_SIZE);
		for (int i=0; i<Shape.GRID_SIZE; i++) {
			/*
			 * Get the line of the current shape
			 * Ex.
			 * 0100
			 * 1110 <- curLine
			 * 0000
			 * 0000 
			 */
			shapeRowBS.set(0, coords[i][0]);
			shapeRowBS.set(1, coords[i][1]);
			shapeRowBS.set(2, coords[i][2]);
			shapeRowBS.set(3, coords[i][3]);
			
			if (!shapeRowBS.isEmpty()) {
				for (int change=0; change<Shape.GRID_SIZE; change++) {
					if (shapeRowBS.get(change)) {
						gBoard.get(ypos+i).get(xpos+change).key = shapeRowBS.get(change);
						gBoard.get(ypos+i).get(xpos+change).value = c;
					}	
				}
			}
		}
	}
	
	private boolean canMove(Move move) {
		boolean ret = true;
		
		int xpos = currentShape.getXPos();
		int ypos = currentShape.getYPos();
		boolean[][] coords = currentShape.getTileShapeCoords();
		
		switch (move) {
		case LEFT:
			xpos--;
			break;
		case RIGHT:
			xpos++;
			break;
		case R_LEFT:
			break;
		case R_RIGHT:
			break;
		case DOWN:
			ypos++;
			break;
		default:
			break;
		}
		
		// Test if we can move
		BitSet shapeRowBS = new BitSet(Shape.GRID_SIZE);
		BitSet boardRowBS = new BitSet(Shape.GRID_SIZE);
		for (int i=0; i<Shape.GRID_SIZE && ret==true; i++) {
			/*
			 * Get the line of the current shape
			 * Ex.
			 * 0100
			 * 1110 <- curLine
			 * 0000
			 * 0000 
			 */
			shapeRowBS.set(0, coords[i][0]);
			shapeRowBS.set(1, coords[i][1]);
			shapeRowBS.set(2, coords[i][2]);
			shapeRowBS.set(3, coords[i][3]);
			
			if (!shapeRowBS.isEmpty()) {
				if (move == Move.DOWN) {
					// Do AND for each line of the current shape and invert result
					// If the bitset is not 0, you can't move
					if (ypos+i < ycase) {
						boardRowBS.clear();
						Vector<Pair<Boolean, Color>> vRow = gBoard.get(ypos+i);
						for (int vr=0; vr<Shape.GRID_SIZE; vr++) {
							if (0 <= xpos+vr && xpos+vr < xcase) {
								boardRowBS.set(vr, vRow.get(xpos+vr).key);
							}
						}
						shapeRowBS.and(boardRowBS);
						ret = shapeRowBS.isEmpty();
					} else {
						ret = false;
					}
				} else {
					int left = shapeRowBS.nextSetBit(0);
					int right = shapeRowBS.previousSetBit(Shape.GRID_SIZE-1);
					// Check border
					ret = (0 <= xpos+left) && (xpos+right < xcase);
					// Check other shapes
					if (ret) {
						Vector<Pair<Boolean, Color>> vRow = gBoard.get(ypos+i);
						for (int vr=0; vr<Shape.GRID_SIZE; vr++) {
							if (0 <= xpos+vr && xpos+vr < xcase) {
								boardRowBS.set(vr, vRow.get(xpos+vr).key);
							}
						}
						shapeRowBS.and(boardRowBS);
						ret = shapeRowBS.isEmpty();
					}
				}
			}
		}
		
		return ret;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		int wcase = getWidth()/xcase;
		int hcase = getHeight()/ycase;
		int squared = wcase < hcase ? wcase : hcase;
		int xmax = xcase*squared;
		int ymax = ycase*squared;
		
		// Draw the board rect
		g.setColor(Color.black);
		g.drawRect(1, 1, xmax, ymax);
		g.setColor(Color.darkGray);
		g.fillRect(1, 1, xmax, ymax);
		// Draw the grid
		g.setColor(Color.lightGray);
		for (int i=1; i<xcase; i++) {
			// Vertical grid
			g.drawLine(i*squared, 0, i*squared, ymax);
			for (int j=1; j<ycase; j++) {
				// Horizontal grid
				g.drawLine(0, j*squared, xmax, j*squared);
			}
		}
		
		// Draw the controllable shape
		g.setColor(currentShape.getColor());
		int xshape = currentShape.getXPos();
		int yshape = currentShape.getYPos();
		int padding=4, arc=10;
		boolean[][] shape_coords = currentShape.getTileShapeCoords();
		for (int i=0; i<Shape.GRID_SIZE; i++) {
			for (int j=0; j<Shape.GRID_SIZE; j++) {
				if (shape_coords[j][i]) {
					g.fillRoundRect((xshape+i)*squared+padding,
							(yshape+j)*squared+padding,
							squared-padding-2,
							squared-padding-2,
							arc, arc);
				}
			}
		}
		
		// Draw the gBoard
		for (int i=0; i<ycase; i++) {
			for (int j=0; j<xcase; j++) {
				Pair<Boolean, Color> p = gBoard.get(i).get(j);
				if (p.key) {
					g.setColor(p.value);
					g.fillRoundRect(j*squared+padding,
							i*squared+padding,
							squared-padding-2,
							squared-padding-2,
							arc, arc);
				}
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (timer.isRunning()) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
				if (canMove(Move.LEFT))
					currentShape.moveLeft();
				break;
			case KeyEvent.VK_RIGHT:
				if (canMove(Move.RIGHT))
					currentShape.moveRight();
				break;
			case KeyEvent.VK_UP:
				if (canMove(Move.R_LEFT))
					currentShape.rotateLeft();
				break;
			case KeyEvent.VK_DOWN:
				if (canMove(Move.R_RIGHT))
					currentShape.rotateRight();
				break;
			case KeyEvent.VK_P:
				timer.stop();
				break;
			default:
				break;
			}	
		} else {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_P:
				timer.start();
				break;
			default:
				break;
			}
		}
		repaint();
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}	

}