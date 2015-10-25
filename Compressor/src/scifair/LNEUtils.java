package scifair;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 
 * Background LNE Utilities for the compressor and inflator.
 * 
 * @author Jonathan Ni
 * @since 01/2013
 *
 */

public class LNEUtils {

	private static int tol = 31; // initial values
	private static int tolLine = 27;

	private static void preAnalyze(byte[] stream, int width, int height) {

		int[] discreteDerivative = new int[stream.length];
		int[] discrete2ndDerivative = new int[stream.length];

		for (int i = 0; i < stream.length - 1; i++)
			discreteDerivative[i] = (stream[i + 1] & 0xFF) - (stream[i] & 0xFF);

		discreteDerivative[stream.length - 1] = discreteDerivative[stream.length - 2];

		for (int i = 0; i < stream.length - 1; i++)
			discrete2ndDerivative[i] = Math.abs(discreteDerivative[i + 1]
					- discreteDerivative[i]);

		Arrays.sort(discrete2ndDerivative);

		tol = median(Arrays.copyOfRange(discrete2ndDerivative,
				discreteDerivative.length / 2, discreteDerivative.length));
		tolLine = (int) Math.round((6.5 / 9.0) * tol);

		// Testing, arbitrary
		
		//tol = 5;
		//tolLine = 3;
	}

	protected static CTriple[] encodeLNE(byte[] stream, int width, int height) {

		preAnalyze(stream, width, height);

		ArrayList<CTriple> log = new ArrayList<CTriple>();

		float islope = -99999;
		float accum = stream[0];
		ArrayList<CTriple> indexlist = new ArrayList<CTriple>();
		boolean[] visited = new boolean[width * height];
		int i = 0, j = 0;
		boolean first = true;

		main: while (i < stream.length - 1) {

			if (visited[i]) {
				while (i != stream.length - 1 && visited[i])
					i++;
				accum = stream[i] & 0xFF;
				islope = -99999;
				j = 1;
				first = true;
				continue main;
			}

			if (first
					|| i == stream.length - 2
					|| visited[i + 1]
					|| Math.abs(islope + accum - (stream[i + 1] & 0xFF)) > tol
					|| stddev(stream, indexlist.get(indexlist.size() - 1).len,
							j, indexlist.get(indexlist.size() - 1).height,
							(int) accum) > tolLine) {

				if (visited[i + 1])
					visited[i] = true;

				islope = (stream[i + 1] & 0xFF) - (stream[i] & 0xFF);

				if (first) {
					first = false;
					indexlist.add(new CTriple(i, stream[i] & 0xFF, 1));
					j = 1;
					visited[i] = true;
					i++;
					continue main;
				}

				int k = 1;
				while (k <= Byte.MAX_VALUE
						&& j <= width
						&& indexlist.get(indexlist.size() - 1).len + k * width
								+ j < width * height
						&& stddev(stream,
								indexlist.get(indexlist.size() - 1).len + k
										* width, j,
								indexlist.get(indexlist.size() - 1).height,
								(int) accum) < tolLine)
					k++;

				CTriple old = new CTriple(indexlist.get(indexlist.size() - 1));
				old.repeat = k;
				if (k * j > 1) {
					indexlist.set(indexlist.size() - 1, old);
					for (int l = 0; l < k; l++)
						Arrays.fill(visited,
								indexlist.get(indexlist.size() - 1).len + l
										* width,
								indexlist.get(indexlist.size() - 1).len + l
										* width + j, true);
				} else {
					Arrays.fill(visited,
							indexlist.get(indexlist.size() - 1).len,
							indexlist.get(indexlist.size() - 1).len + j, true);
				}

				indexlist.add(new CTriple(i, stream[i] & 0xFF, 1));
				j = 1;
			} else {
				accum += islope;
				accum = (accum * 2 + (stream[i + 1] & 0xFF)) / 3.0f;
				j++;
			}

			i++;
		}

		indexlist.add(new CTriple(stream.length - 1,
				stream[stream.length - 1] & 0xFF, (byte) 1));// tail

		Arrays.fill(visited, false);

		for (i = 0; i < indexlist.size() - 1; i++) {
			CTriple copy = new CTriple(indexlist.get(i));
			if (visited[indexlist.get(i).len + 1]) {
				copy.len = 1;
				log.add(copy);
				visited[indexlist.get(i).len] = true;
				continue;
			}

			int length = indexlist.get(i + 1).len - indexlist.get(i).len;
			float init = indexlist.get(i).height;

			for (int l = 0; l < indexlist.get(i).repeat; l++)
				Arrays.fill(visited, indexlist.get(i).len + l * width,
						indexlist.get(i + 1).len + l * width, true);

			if (length > Short.MAX_VALUE) {
				float slope = (indexlist.get(i + 1).height - indexlist.get(i).height)
						/ (length / 1.0f);
				while (length > Short.MAX_VALUE) {
					length -= Short.MAX_VALUE;
					log.add(new CTriple(Short.MAX_VALUE, (int) init,
							copy.repeat));
					init += slope * Short.MAX_VALUE;
				}
			}

			log.add(new CTriple(length, (int) init, copy.repeat));
		}

		log.add(new CTriple(1, indexlist.get(indexlist.size() - 1).height,
				(byte) 1));

		CTriple[] ret = new CTriple[log.size()];
		log.toArray(ret);
		return ret;
	}

	private static float stddev(byte[] stream, int start, int length,
			int startval, int endval) {
		float stddev = 0;

		float slope = (endval - startval) / (length * 1f);
		float accum = startval;

		for (int i = start; i < start + length; i++) {
			stddev += Math.abs(accum - (stream[i] & 0xFF));
			accum += slope;
		}

		stddev /= length;

		return stddev;
	}

	protected static float stddev(byte[] s1, byte[] s2) {
		if (s1.length != s2.length) {
			System.out.println("data lengths unequal!");
			return -1;
		}

		float stddev = 0;

		for (int i = 0; i < s1.length; i++)
			stddev += Math.abs((s2[i] & 0xFF) - (s1[i] & 0xFF));

		stddev /= (s1.length * 1f);

		return stddev;
	}

	protected static Color[] decodeLNE(CTriple[] encode, int width, int height,
			int index) {
		Color[] colors = new Color[width * height];

		ArrayList<Integer> indices = new ArrayList<Integer>();

		for (int i = 0; i < colors.length; i++)
			colors[i] = new Color(0, 0, 0);

		boolean[] visited = new boolean[width * height];
		int indexcum = 0;

		for (int i = 0; i < encode.length - 1; i++) {
			if (visited[indexcum]) {
				while (indexcum < width * height && visited[indexcum])
					indexcum++;
			}

			int index1 = indexcum;
			int index2 = indexcum + encode[i].len;

			float accum = encode[i].height;
			float slope = (encode[i + 1].height - accum)
					/ ((index2 - index1) / 1.0f);

			for (int j = index1; j < index2; j++) {
				for (int k = 0; k < encode[i].repeat; k++) {
					int num = constrain(0, 255, (int) Math.round(accum));
					colors[j + k * width] = new Color(index == 0 ? num : 0,
							index == 1 ? num : 0, index == 2 ? num : 0);
					visited[j + k * width] = true;
				}
				accum += slope;
			}
			indices.add(indexcum);
			indexcum += encode[i].len;
		}

		colors[colors.length - 1] = new Color(constrain(0, 255,
				encode[encode.length - 1].height));

		return colors;
	}

	protected static float rangeMedian(int s1, int s2, int length, byte[] array) {
		int accum = 0;
		for (int i = 0; i < length; i++)
			accum += Math.abs(array[s2 + i] - array[s1 + i]);
		return accum / (length / 1.0f);
	}

	protected static int constrain(int lower, int higher, int number) {
		return (number < lower ? lower : (number > higher ? higher : number));
	}

	protected static Color[] colorCombine(Color[] colorsRed,
			Color[] colorsGreen, Color[] colorsBlue) throws Exception {
		if (colorsRed.length != colorsGreen.length
				|| colorsRed.length != colorsBlue.length
				|| colorsGreen.length != colorsBlue.length)
			throw new Exception(
					"Color array length must be the same for all 3 arrays.");

		Color[] combination = new Color[colorsRed.length];

		for (int i = 0; i < combination.length; i++) {

			combination[i] = new Color(colorsRed[i].getRed(),
					colorsGreen[i].getGreen(), colorsBlue[i].getBlue());
		}

		return combination;
	}

	protected static int[] simplify(int a, int b) {
		int gcf = euclidgcf(a, b);
		return new int[] { a / gcf, b / gcf };
	}

	protected static int euclidgcf(int a, int b) {
		int gcf = 0;

		while (b != 0) {
			gcf = b;
			b = a % b;
			a = gcf;
		}

		return gcf;
	}

	protected static byte[] concat(byte[] first, byte[] second) {
		byte[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	protected static byte pixelAverage(byte[] a) {
		long sum = 0;

		for (byte i : a)
			sum += i + 128;

		return (byte) (sum / (a.length / 1.0) - 128);
	}

	protected static byte median(byte[] a) {
		if (a.length % 2 == 1)
			return a[(int) Math.floor(a.length / 2.0)];
		return (byte) ((a[a.length / 2] + a[a.length / 2 - 1]) / 2);
	}

	protected static int median(int[] a) {
		if (a.length % 2 == 1)
			return a[(int) Math.floor(a.length / 2.0)];
		return (a[a.length / 2] + a[a.length / 2 - 1]) / 2;
	}

	protected static byte[] average(byte[] a, byte[] b) {
		byte[] average = new byte[a.length];

		for (int i = 0; i < average.length; i++)
			average[i] = (byte) ((a[i] + b[i]) / 2);

		return average;
	}

	protected static double averageDeviation(byte[] a, byte[] b) {
		int len = a.length;
		long sum = 0;

		for (int i = 0; i < len; i++)
			sum += Math.abs(a[i] - b[i]);

		return sum / (len / 1.0f);
	}

	protected static double averageDeviationConst(byte[] a, byte b) {
		int len = a.length;
		long sum = 0;

		for (int i = 0; i < len; i++)
			sum += Math.abs(a[i] - b);

		return sum / (len / 1.0f);
	}

	protected static int[] unrollArrayPeriodic(byte[] array) {
		int[] newA = new int[array.length];

		for (int i = 0; i < newA.length; i++) {
			newA[i] = (0xFF << 24) | ((array[i] & 0xFF) << 16)
					| ((array[i] & 0xFF) << 8) | (array[i] & 0xFF);
		}

		return newA;
	}

	protected static byte[] getArrayPeriodic(byte[] array, int offset,
			int period) {

		byte[] partial = new byte[array.length / 3];

		for (int i = 0; i < partial.length; i++)
			partial[i] = array[i * period + offset];

		return partial;
	}
}
