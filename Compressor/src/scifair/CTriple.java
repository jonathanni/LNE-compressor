package scifair;

/**
 * 
 * Data structure holding three values.
 * 
 * @author Jonathan Ni
 * @since 01/2013
 *
 */

public class CTriple {
	public int len;
	public int height;
	public int repeat;

	public CTriple(int len, int i, int repeat) {
		this.len = len;
		this.height = i;
		this.repeat = repeat;
	}

	public CTriple(CTriple cTriple) {
		this.len = cTriple.len;
		this.height = cTriple.height;
		this.repeat = cTriple.repeat;
	}

	public String toString() {
		return "{" + len + ", " + height + ", " + repeat + "}";
	}
}
