package scifair;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * 
 * Test display, often unused.
 * 
 * @author Jonathan Ni
 * @since 01/2013
 *
 */

public class CDisplay extends JFrame implements MouseMotionListener,
		KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private BufferedImage[] imgs;

	public BufferedImage[] getImgs() {
		return imgs;
	}

	public void setImgs(BufferedImage[] imgs) {
		this.imgs = imgs;
	}

	private int width, height, pixind;

	public CDisplay(int width, int height) {
		super();
		setSize(width * 3, height * 2);
		setUndecorated(true);
		this.width = width;
		this.height = height;
		addMouseMotionListener(this);
		addKeyListener(this);
	}

	public void display() {
		setVisible(true);
	}

	@Override
	public void paint(Graphics g) {

		g.clearRect(0, 0, width * 3, height * 2);

		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 3; j++)
				if (imgs[i * 3 + j] != null)
					g.drawImage(imgs[i * 3 + j], j * width, i * height, null);

		updateIndex(g);

		g.dispose();
	}

	private void updateIndex(Graphics g) {

		Point mouse = MouseInfo.getPointerInfo().getLocation();
		SwingUtilities.convertPointFromScreen(mouse, this);
		int x = mouse.x % width;
		int y = mouse.y % height;

		pixind = y * width + x;

		g.setColor(Color.white);
		g.fillRect(width * 2, 0, width, height);
		g.setColor(Color.black);
		g.drawString("pixel index: " + pixind, width * 2, 100);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		updateIndex(this.getGraphics());
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		updateIndex(this.getGraphics());
	}

	@Override
	public void keyTyped(KeyEvent e) {
		char key = e.getKeyChar();
		if (key == 'c')
			this.dispose();
		e.consume();
	}

	@Override
	public void keyPressed(KeyEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {

	}
}
