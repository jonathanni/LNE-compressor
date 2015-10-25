package scifair;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;

/**
 * 
 * Compressor for the image.
 * 
 * @author Jonathan Ni
 * @since 01/2013
 *
 */

public class CCompressor extends CUtility {

	private byte[] redPart, greenPart, bluePart;
	private int width, height;

	private long lastTime = 0L; // nano
	private long lastSize = 0L; // bytes
	private int encodeSize = 0;

	public CCompressor(CImage cImage) {
		super(cImage);

		CImage image = getImage();

		byte[] localRawData = image.getRawImageData();

		width = image.getRawImage().getWidth();
		height = image.getRawImage().getHeight();

		redPart = LNEUtils.getArrayPeriodic(localRawData, 2, 3);
		greenPart = LNEUtils.getArrayPeriodic(localRawData, 1, 3);
		bluePart = LNEUtils.getArrayPeriodic(localRawData, 0, 3);
	}

	private byte[] repeatEncode(CTriple[] triples) {
		ArrayList<Byte> encoder = new ArrayList<Byte>();
		ArrayList<Integer> encodeIndices = new ArrayList<Integer>();
		int j = 1;

		for (int i = 0; i < triples.length; i++) {
			if (i == 0 || triples[i].repeat != 1 || j == Short.MAX_VALUE) {
				encoder.add((byte) triples[i].repeat);
				encodeIndices.add(i);
				j = 1;
				continue;
			}
			j++;
		}

		ByteBuffer fill = ByteBuffer.allocate(encoder.size()
				+ encodeIndices.size() * 2);

		encodeSize = encoder.size();
		for (int i = 0; i < encoder.size() - 1; i++)
			fill.putShort(
					(short) (encodeIndices.get(i + 1) - encodeIndices.get(i)))
					.put(encoder.get(i));

		fill.putShort((short) 0).put(encoder.get(encoder.size() - 1));

		return fill.array();
	}

	public CCompressor compressLNE(String file) throws IOException {

		long time = System.nanoTime();

		CTriple[] encodeBlue = LNEUtils.encodeLNE(bluePart, width, height);
		CTriple[] encodeGreen = LNEUtils.encodeLNE(greenPart, width, height);
		CTriple[] encodeRed = LNEUtils.encodeLNE(redPart, width, height);

		File tfile = new File(file);
		GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(tfile));

		out.write(ByteBuffer.allocate(4).putShort((short) width)
				.putShort((short) height).array());

		out.write(ByteBuffer.allocate(12).putInt(encodeBlue.length)
				.putInt(encodeGreen.length).putInt(encodeRed.length).array());

		// BGR
		byte[] encodeBlueData = repeatEncode(encodeBlue);
		out.write(ByteBuffer.allocate(4).putInt(encodeSize).array());
		out.write(encodeBlueData);

		for (CTriple i : encodeBlue)
			out.write(ByteBuffer.allocate(3).putShort((short) i.len)
					.put((byte) i.height).array());

		byte[] encodeGreenData = repeatEncode(encodeGreen);
		out.write(ByteBuffer.allocate(4).putInt(encodeSize).array());
		out.write(encodeGreenData);

		for (CTriple i : encodeGreen)
			out.write(ByteBuffer.allocate(3).putShort((short) i.len)
					.put((byte) i.height).array());

		byte[] encodeRedData = repeatEncode(encodeRed);
		out.write(ByteBuffer.allocate(4).putInt(encodeSize).array());
		out.write(encodeRedData);

		for (CTriple i : encodeRed)
			out.write(ByteBuffer.allocate(3).putShort((short) i.len)
					.put((byte) i.height).array());

		out.close();

		setLastTime(System.nanoTime() - time);
		setLastSize(tfile.length());

		return this;
	}

	public CCompressor compressJPEG(String file) throws IOException {

		BufferedImage out = new BufferedImage(width, height,
				BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = out.getGraphics();

		g.drawImage(getImage().getRawImage(), 0, 0, null);

		File output = new File(file);
		FileOutputStream outputstream = new FileOutputStream(output);

		long time = System.nanoTime();

		try {
			ImageIO.write(out, "jpg", outputstream);
		} catch (IOException e) {
			e.printStackTrace();
		}

		setLastTime(System.nanoTime() - time);
		setLastSize(output.length());

		return this;
	}

	public CCompressor compressPNG(String file) throws IOException {

		BufferedImage out = new BufferedImage(width, height,
				BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = out.getGraphics();

		g.drawImage(getImage().getRawImage(), 0, 0, null);

		File output = new File(file);
		FileOutputStream outputstream = new FileOutputStream(output);

		long time = System.nanoTime();

		ImageIO.write(out, "png", outputstream);

		setLastTime(System.nanoTime() - time);
		setLastSize(output.length());

		return this;
	}

	public CCompressor compressGIF(String file) throws IOException {

		BufferedImage out = new BufferedImage(width, height,
				BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = out.getGraphics();

		g.drawImage(getImage().getRawImage(), 0, 0, null);

		File output = new File(file);
		FileOutputStream outputstream = new FileOutputStream(output);

		long time = System.nanoTime();

		ImageIO.write(out, "gif", outputstream);

		setLastTime(System.nanoTime() - time);
		setLastSize(output.length());

		return this;
	}

	public CCompressor compressBMP(String file) throws IOException {

		BufferedImage out = new BufferedImage(width, height,
				BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = out.getGraphics();

		g.drawImage(getImage().getRawImage(), 0, 0, null);

		File output = new File(file);
		FileOutputStream outputstream = new FileOutputStream(output);

		long time = System.nanoTime();

		ImageIO.write(out, "bmp", outputstream);

		setLastTime(System.nanoTime() - time);
		setLastSize(output.length());

		return this;
	}

	public CCompressor compressRLE(String file) {

		try {

			File output = new File(file);
			BufferedOutputStream out = new BufferedOutputStream(
					new FileOutputStream(output));

			long time = System.nanoTime();

			RLE(out, redPart);
			RLE(out, greenPart);
			RLE(out, bluePart);

			setLastTime(System.nanoTime() - time);
			setLastSize(output.length());

			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return this;
	}

	private void RLE(BufferedOutputStream stream, byte[] data)
			throws IOException {
		boolean last = false;
		byte remember = data[0];
		byte run = 0;

		boolean[] bdata = byteArrayToBooleanArray(data);

		for (boolean i : bdata) {
			if (i != last || run == 255) {
				stream.write(run);
				stream.write(remember);
			} else
				run++;
		}

		stream.write(1);
		stream.write(data[data.length - 1]);
	}

	private boolean[] byteArrayToBooleanArray(byte[] in) {
		boolean[] out = new boolean[in.length * 8];

		for (int i = 0; i < in.length; i++) {
			for (int j = 0; j < 8; j++)
				out[i * 8 + j] = (in[i] & 128) != 0;
		}

		return out;
	}

	public long getLastTime() {
		return lastTime;
	}

	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}

	public long getLastSize() {
		return lastSize;
	}

	public void setLastSize(long lastSize) {
		this.lastSize = lastSize;
	}
}
