package Barcode;

/**
 * Stores bits as an array of bytes with no padding
 */
public class CompressedBytes {

    private int bitCount;
    private byte[] bytes;

    /**
     * Constructor
     * 
     * @param length length of the array in bits
     */
    public CompressedBytes(int length) {
        bitCount = 0;

        // Get the length in bytes
        length = length / 8 + ((length % 8 == 0) ? 0 : 1);
        bytes = new byte[length];
    }

    /**
     * Getter for bytes
     * 
     * @return bytes array
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * Adds the bits to the end of the array in big-endian order
     * 
     * @param bits   input as a long
     * @param length length of the input in bits
     */
    public void addBits(long bits, int length) {

        // Skip the first bits if length is longer than can be stored in a long
        if (length > 64) {
            bitCount += length - 64;
        }

        // Add each bit in the long (highest-order bit first)
        for (long cursor = 1l << length - 1; cursor != 0; cursor = cursor >>> 1) {
            
            // Get the byte location and the location within the byte
            int byteLoc = bitCount >> 3;
            byte bitMask = (byte) (0b10000000 >> (bitCount & 0b111));

            // Check if there is a bit under the cursor
            if ((bits & cursor) != 0) {
                bytes[byteLoc] = (byte) (bytes[byteLoc] | bitMask);
            }

            bitCount++;
        }
    }

    /**
     * Getter for bitCount
     * 
     * @return the bit count
     */
    public int getBitCount() {
        return bitCount;
    }

    /**
     * Getter for the length of the byte array
     * 
     * @return the length of the byte array
     */
    public int getByteCount() {
        return bytes.length;
    }

    /**
     * Sets one bit of the byte array
     * 
     * @param location the bit location to set (bits within byte is left to right)
     * @param bit      true to set bit to 1, false to set bit to 0
     */
    public void setBit(int location, boolean bit) {
        int byteLoc = location >> 3;
        byte bitMask = (byte) (0b10000000 >> (location & 0b111));

        if (bit) {
            bytes[byteLoc] = (byte) (bytes[byteLoc] | bitMask);
        } else {
            bytes[byteLoc] = (byte) (bytes[byteLoc] & ~bitMask);
        }
    }

    /**
     * Replaces a continuous set of bits
     * 
     * @param replacementBits the bits to be used for replacement
     * @param startBit        the first bit to replace
     * @param length          the length of the replacement bits
     */
    public void setBits(long replacementBits, int startBit, int length) {

        // Basically addBits() without updating bitCount (unless we go past bitcount)

        // Skip the first bits if length is longer than can be stored in a long
        if (length < 64) {
            startBit += length - 64;
        }

        // Add each bit in the long (highest-order bit first)
        for (long cursor = 1l << length - 1; cursor != 0; cursor = cursor >>> 1) {
            // Check if there is a bit under the cursor

            int byteLoc = startBit >> 3;
            byte bitMask = (byte) (0b10000000 >> (startBit & 0b111));

            if ((replacementBits & cursor) != 0) {
                bytes[byteLoc] = (byte) (bytes[byteLoc] | bitMask);
            } else {
                bytes[byteLoc] = (byte) (bytes[byteLoc] & ~bitMask);
            }

            startBit++;
        }
    }

    /**
     * Prints the bits as a string of 1s and 0s
     */
    @Override
    public String toString() {
        char[] bits = new char[bitCount];

        for (int i = 0; i < bitCount; i++) {
            int byteLoc = i >> 3;
            byte bitMask = (byte) (0b10000000 >> (i & 0b111));

            // Put the bit under the cursor into bits[i]
            bits[i] = ((bytes[byteLoc] & bitMask) != 0) ? '1' : '0';
        }

        return String.valueOf(bits); 
    }

    
}
