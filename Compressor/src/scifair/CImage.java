package scifair;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * 
 * Wrapper for the BufferedImage.
 * 
 * @author Jonathan Ni
 * @since 01/2013
 *
 */

public class CImage {
	private BufferedImage rawImage;
	private String compressed = "";
	private byte[] rawImageData;

	public CImage(BufferedImage raw) {
		setRawImage(raw);
		setRawImageData(((DataBufferByte) this.rawImage.getRaster()
				.getDataBuffer()).getData());
	}

	public CImage(String string) throws IOException {
		BufferedImage first = ImageIO.read(CImage.class.getResource(string));
		BufferedImage second = new BufferedImage(first.getWidth(),
				first.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = second.getGraphics();
		g.drawImage(first, 0, 0, null);
		g.dispose();

		setRawImage(second);
		setRawImageData(((DataBufferByte) this.rawImage.getRaster()
				.getDataBuffer()).getData());
	}

	public BufferedImage getRawImage() {
		return rawImage;
	}

	public void setRawImage(BufferedImage rawImage) {
		this.rawImage = rawImage;
	}

	public String getCompressed() {
		return compressed;
	}

	public void setCompressed(String compressed) {
		this.compressed = compressed;
	}

	public byte[] getRawImageData() {
		return rawImageData;
	}

	public void setRawImageData(byte[] rawImageData) {
		this.rawImageData = rawImageData;
	}

}
