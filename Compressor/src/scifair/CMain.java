package scifair;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * 
 * Test class.
 * 
 * @author Jonathan Ni
 * @since 01/2013
 *
 */

public class CMain {

	static BufferedImage b, g;

	static {
		try {
			b = ImageIO
					.read(CMain.class.getResource("../images/sample_04.png"));
			g = new BufferedImage(b.getWidth(), b.getHeight(),
					BufferedImage.TYPE_3BYTE_BGR);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		CImage image = new CImage("../images/sample_04.png");
		CCompressor c = new CCompressor(image).compressLNE("sample.lne");
		CInflater d = new CInflater(new File("sample.lne"));
		d.inflateLNE(null);
		g = d.getImage().getRawImage();

		BufferedImage out = new BufferedImage(b.getWidth() * 2, b.getHeight(),
				BufferedImage.TYPE_3BYTE_BGR);
		Graphics gr = out.getGraphics();

		gr.drawImage(b, 0, 0, null);
		gr.drawImage(g, b.getWidth(), 0, null);
		gr.dispose();

		ImageIO.write(out, "png", new File("file.png"));

	}

}
