package scifair;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * 
 * Data structure and utility holding all needed for LNE.
 * 
 * @author Jonathan Ni
 * @since 01/2013
 *
 */

public class CUtility {

	private CImage image;
	private BufferedImage finalimg, red, blue, green;
	private File in;

	public CUtility(File in) {
		setIn(in);
	}

	public CUtility(CImage cImage) {
		setImage(cImage);
	}

	public CImage getImage() {
		return image;
	}

	public void setImage(CImage image) {
		this.image = image;
	}

	public static BufferedImage copy(BufferedImage first) {
		BufferedImage copy = new BufferedImage(first.getWidth(),
				first.getHeight(), first.getType());
		copy.setData(first.getData());

		return copy;
	}

	public BufferedImage getFinalimg() {
		return finalimg;
	}

	public void setFinalimg(BufferedImage finalimg) {
		this.finalimg = finalimg;
	}

	public File getIn() {
		return in;
	}

	public void setIn(File in) {
		this.in = in;
	}

	public BufferedImage getRed() {
		return red;
	}

	public BufferedImage getBlue() {
		return blue;
	}

	public BufferedImage getGreen() {
		return green;
	}
}
