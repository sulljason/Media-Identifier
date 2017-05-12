
/**
 * A class for providing fast(ish) bitwise manipulation functions. 
 * @author 1n5aN1aC
 */
public class BitManip {
	/**
	 * Sets a particular bit in a long on or off
	 * @param theLong
	 * @param loc
	 * @param bool
	 * @return the new long, with the bit possibly set.
	 */
	public static final long setBitInLoc(long theLong, int loc, long set) {
		return theLong >>> loc << loc | theLong << (65L - loc) >>> (65L - loc) | set << loc - 1;
	}

	public static final long getBitInLoc(long theLong, int x) {
		return theLong << (64 - x) >>> 63;
	}

	/**
	 * Calculates the number of bits different in 2 longs passed in
	 * @param hash1 the first long to compare
	 * @param hash2 the second long to compare
	 * @return the number of bits that differ
	 */
	public static final int hamDistance(long hash1, long hash2) {
		return Long.bitCount(hash1 ^ hash2);
	}
	
	private static final String all0s = "0000000000000000000000000000000000000000000000000000000000000000";
	public static final String getLongAsBinaryString(long theLong) {
		char[] result = BitManip.all0s.toCharArray();
		for(int i = 64; i > 0; i--) {
			if(getBitInLoc(theLong, i) > 0L)
				result[64-i] = '1';
		}
		return new String(result);
	}
}
