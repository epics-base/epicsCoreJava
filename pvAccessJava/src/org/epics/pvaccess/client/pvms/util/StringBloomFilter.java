/**
 * 
 */
package org.epics.pvaccess.client.pvms.util;

import java.nio.charset.Charset;

import org.epics.pvdata.misc.BitSet;

/**
 * This class implements a String Bloom filter.
 * Google CityHash64 (seed-less) hash function is being used
 * 
 * Read http://en.wikipedia.org/wiki/Bloom_filter for more details.
 * @author msekoranja
 */
public class StringBloomFilter {

	private final BitSet bitSet;
	private final int k;
	private final int m;
	
	// statistics
	private int elements = 0;

	private static final double LN2 = Math.log(2);
	private static final double LN2_SQR = Math.pow(Math.log(2), 2);

	public StringBloomFilter(int c, int n) {
		k = (int) Math.floor(c * LN2); // optimal k = c * ln(2)
		m = ((((c * n) - 1) / 64) + 1) * 64; // round to 64-bit 
		bitSet = new BitSet(m);
	}

	public StringBloomFilter(double p, int n) {
		this((int) Math.ceil(-Math.log(p) / LN2_SQR), n); // c = -ln(p)/ln(2)^2
	}

	/**
	 * See "Less Hashing, Same Performance: Building a Better Bloom Filter" by Adam Kirsch and
	 * Michael Mitzenmacher on how to use (enhanced) double hashing on Bloom filters.
	 */
	public void add(String str) {
		
		byte[] byteArray = getBytes(str);
		long hash64 = CityHash64.cityHash64(byteArray, 0, byteArray.length);
		
		int h1 = (int) hash64;
		int h2 = (int) (hash64 >>> 32);
		// or use two 32-bit hashes with different (static) seeds
		for (int i = 0; i < k; i++) {
			// double hashing
			int nextHash = h1 + i * h2;

			// Math.abs returns negative for Integer.MIN_VALUE
			if (nextHash < 0)
				nextHash = ~nextHash;

			// set bit
			bitSet.set(nextHash % m);
		}
		elements++;
	}
	
	private static final Charset utf8Charset = Charset.forName("UTF-8");

    private byte[] getBytes(String str)
    {
    	return str.getBytes(utf8Charset);
    }
    
	public boolean contains(String str) {
		
		byte[] byteArray = getBytes(str);
		long hash64 = CityHash64.cityHash64(byteArray, 0, byteArray.length);
		
		int h1 = (int) hash64;
		int h2 = (int) (hash64 >>> 32);
		// or use two 32-bit hashes with different (static) seeds
		for (int i = 0; i < k; i++) {
			// double hashing
			int nextHash = h1 + i * h2;

			// Math.abs returns negative for Integer.MIN_VALUE
			if (nextHash < 0)
				nextHash = ~nextHash;

			// set bit
			if (!bitSet.get(nextHash % m))
				return false;
		}
		
		return true;

	}
	
	/**
	 * Reset filter.
	 * Clears filter bit-set and sets number of elements to 0.
	 */
	public void reset()
	{
		bitSet.clear();
		elements = 0;
	}

	/**
	 * Get false positive probability of a filter (depending on current element count).
	 * @return false positive probability.
	 */
	public double p()
	{
		return Math.pow(1 - Math.exp(-k*elements/(double)m), k);
	}

	/**
	 * Get number of hash functions.
	 * @return number of hash functions.
	 */
	public int k() {
		return k;
	}

	/**
	 * Get filter size in bits.
	 * @return filter size in bits.
	 */
	public int m() {
		return m;
	}

	/**
	 * Get number of inserted elements.
	 * @return number of inserted elements.
	 */
	public int elements() {
		return elements;
	}
	
	/**
	 * Get filter bit-set.
	 * @return filter bit-set.
	 */
	public BitSet bitSet() {
		return bitSet;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString() + " = { k = " + k + ", m = " + m + ", elements = " + elements + " -> p = " + p() + "}";
	}
	
}
