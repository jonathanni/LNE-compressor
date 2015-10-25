package scifair;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;

/**
 * 
 * Inflator (uncompressor) for the image.
 * 
 * @author Jonathan Ni
 * @since 01/2013
 *
 */

public class CInflater extends CUtility {

	private int width, height;
	private BufferedInputStream in;
	private CTriple[] encodeBlue, encodeGreen, encodeRed;
	private BufferedImage r, g, b;
	private float avgDev = 0;

	public CInflater(File f) {
		super(f);
	}

	public CTriple[] feed(int length) throws IOException {

		CTriple[] end = new CTriple[length];

		byte[] lengths = new byte[length];
		byte[] tablesize = new byte[4];
		in.read(tablesize);
		int itablesize = ByteBuffer.wrap(tablesize).getInt();

		int add = 0;
		for (int i = 0; i < itablesize; i++) {
			byte[] index = new byte[2];
			in.read(index);
			lengths[add] = (byte) in.read();
			add += ByteBuffer.wrap(index).getShort();
		}

		for (int i = 0; i < lengths.length; i++)
			if (lengths[i] == 0)
				lengths[i] = 1;

		for (int i = 0; i < length; i++) {
			byte[] b = new byte[3];
			in.read(b);
			short len = ByteBuffer.wrap(Arrays.copyOfRange(b, 0, 2)).getShort();
			end[i] = new CTriple(len, b[2] & 0xFF, lengths[i] & 0xFF);
		}

		return end;
	}

	public CInflater inflateLNE(BufferedImage comparison) {

		try {
			in = new BufferedInputStream(new GZIPInputStream(
					new FileInputStream(getIn())));
			int line = (in.read() << 24 | in.read() << 16 | in.read() << 8 | in
					.read());

			width = (line & 0xFFFF0000) >>> 16;
			height = line & 0x0000FFFF;

			byte[] buf = new byte[12];
			in.read(buf);

			encodeBlue = feed(ByteBuffer.wrap(Arrays.copyOfRange(buf, 0, 4))
					.getInt());
			encodeGreen = feed(ByteBuffer.wrap(Arrays.copyOfRange(buf, 4, 8))
					.getInt());
			encodeRed = feed(ByteBuffer.wrap(Arrays.copyOfRange(buf, 8, 12))
					.getInt());

		} catch (IOException e) {
			e.printStackTrace();
		}

		Color[] colors = new Color[width * height];

		Color[] colorsBlue = LNEUtils.decodeLNE(encodeBlue, width, height, 2);
		Color[] colorsGreen = LNEUtils.decodeLNE(encodeGreen, width, height, 1);
		Color[] colorsRed = LNEUtils.decodeLNE(encodeRed, width, height, 0);

		try {
			in.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			colors = LNEUtils.colorCombine(colorsRed, colorsGreen, colorsBlue);
		} catch (Exception e) {
			e.printStackTrace();
		}

		BufferedImage recreation = new BufferedImage(width, height,
				BufferedImage.TYPE_3BYTE_BGR);
		Graphics gr = recreation.getGraphics();

		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++) {
				gr.setColor(colors[i * width + j]);
				gr.drawRect(j, i, 1, 1);
			}

		gr.setColor(Color.white);
		gr.setFont(new Font("Courier New", Font.BOLD, 72));
		gr.drawString("NEW", 0, 0);

		gr.dispose();

		setImage(new CImage(recreation));

		BufferedImage blueRec = new BufferedImage(width, height,
				BufferedImage.TYPE_3BYTE_BGR);
		BufferedImage greenRec = new BufferedImage(width, height,
				BufferedImage.TYPE_3BYTE_BGR);
		BufferedImage redRec = new BufferedImage(width, height,
				BufferedImage.TYPE_3BYTE_BGR);

		Graphics blueG = blueRec.getGraphics();
		Graphics greenG = greenRec.getGraphics();
		Graphics redG = redRec.getGraphics();

		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++) {
				Color orig = colors[i * width + j];
				blueG.setColor(new Color(0, 0, orig.getBlue()));
				greenG.setColor(new Color(0, orig.getGreen(), 0));
				redG.setColor(new Color(orig.getRed(), 0, 0));

				blueG.drawRect(j, i, 1, 1);
				greenG.drawRect(j, i, 1, 1);
				redG.drawRect(j, i, 1, 1);
			}

		blueG.dispose();
		greenG.dispose();
		redG.dispose();

		setR(redRec);
		setG(greenRec);
		setB(blueRec);

		if (comparison != null) {
			byte[] origdata = ((DataBufferByte) comparison.getRaster()
					.getDataBuffer()).getData();
			byte[] compdata = ((DataBufferByte) recreation.getRaster()
					.getDataBuffer()).getData();

			setAvgDev(LNEUtils.stddev(origdata, compdata));
		}

		return this;
	}

	public CInflater inflateJPEG(BufferedImage comparison) {

		BufferedImage rec = null;
		try {
			rec = ImageIO.read(getIn());
		} catch (IOException e) {
			e.printStackTrace();
		}

		int type = BufferedImage.TYPE_3BYTE_BGR;

		BufferedImage recreation = new BufferedImage(rec.getWidth(),
				rec.getHeight(), type);
		Graphics g = recreation.getGraphics();
		g.drawImage(rec, 0, 0, null);
		g.dispose();

		setImage(new CImage(recreation));

		if (comparison != null) {
			byte[] origdata = ((DataBufferByte) comparison.getRaster()
					.getDataBuffer()).getData();
			byte[] compdata = ((DataBufferByte) recreation.getRaster()
					.getDataBuffer()).getData();

			setAvgDev(LNEUtils.stddev(origdata, compdata));
		}

		return this;
	}

	public CInflater inflateGIF(BufferedImage comparison) {

		BufferedImage rec = null;
		try {
			rec = ImageIO.read(getIn());
		} catch (IOException e) {
			e.printStackTrace();
		}

		int type = BufferedImage.TYPE_3BYTE_BGR;

		BufferedImage recreation = new BufferedImage(rec.getWidth(),
				rec.getHeight(), type);
		Graphics g = recreation.getGraphics();
		g.drawImage(rec, 0, 0, null);
		g.dispose();

		setImage(new CImage(recreation));

		if (comparison != null) {
			byte[] origdata = ((DataBufferByte) comparison.getRaster()
					.getDataBuffer()).getData();
			byte[] compdata = ((DataBufferByte) recreation.getRaster()
					.getDataBuffer()).getData();

			setAvgDev(LNEUtils.stddev(origdata, compdata));
		}

		return this;
	}

	public BufferedImage getR() {
		return r;
	}

	public void setR(BufferedImage r) {
		this.r = r;
	}

	public BufferedImage getG() {
		return g;
	}

	public void setG(BufferedImage g) {
		this.g = g;
	}

	public BufferedImage getB() {
		return b;
	}

	public void setB(BufferedImage b) {
		this.b = b;
	}

	public float getAvgDev() {
		return avgDev;
	}

	public void setAvgDev(float avgDev) {
		this.avgDev = avgDev;
	}
}
