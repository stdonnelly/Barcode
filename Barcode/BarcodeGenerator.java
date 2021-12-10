package Barcode;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

public class BarcodeGenerator {

    public static enum BarcodeType {
        UPC_A,
        UPC_E,
        EAN_13,
        EAN_8
    }

    public static void generate(BarcodeType type, long numberToEncode, boolean ignoreCheckDigit)
            throws CheckDigitException {
        CompressedBytes cBytes = null;
        FileOutputStream fout = null;

        // A bunch of constants
        // L variables in UPC and EAN
        final byte[] L = { 0b0001101, 0b0011001, 0b0010011, 0b0111101, 0b0100011, 0b0110001, 0b0101111, 0b0111011,
                0b0110111, 0b0001011 };

        // G variables in EAN-13
        final byte[] G = { 0b0100111, 0b0110011, 0b0011011, 0b0100001, 0b0011101, 0b0111001, 0b0000101, 0b0010001,
                0b0001001, 0b0010111 };

        // R variables in UPC and EAN
        final byte[] R = { 0b1110010, 0b1100110, 0b1101100, 0b1000010, 0b1011100, 0b1001110, 0b1010000, 0b1000100,
                0b1001000, 0b1110100 };

        // Don't know if these are UPC or most barcodes
        final int BARCODE_HEIGHT = 78;
        final int PIXELS_PER_UNIT = 3000;
        final byte[] PNG_MAGIC_NUMBER = { (byte) 0x89, (byte) 'P', (byte) 'N', (byte) 'G', 0x0d, 0x0a, 0x1a, 0x0a };

        // Variables
        boolean noBarcode = false; // set to true if a barcode is not generated for any reason
        byte[] barcodeLine = null;
        byte[] digitsToEncode = null;

        switch (type) {
            case UPC_A:
            case EAN_13:
                // UPC-A or EAN-13
                // True where the digit is L in EAN-13
                final boolean[][] EAN13_FIRST_6 = { { true, true, true, true, true, true },
                        { true, true, false, true, false, false }, { true, true, false, false, true, false },
                        { true, true, false, false, false, true }, { true, false, true, true, false, false },
                        { true, false, false, true, true, false }, { true, false, false, false, true, true },
                        { true, false, true, false, true, false }, { true, false, true, false, false, true },
                        { true, false, false, true, false, true } };
                cBytes = new CompressedBytes(113);

                // Encode the quiet zone and start bits
                cBytes.addBits(0, 9);
                cBytes.addBits(0b101, 3);

                // Get the digits
                digitsToEncode = new byte[13];
                for (int i = 12; i >= 0; i--) {
                    digitsToEncode[i] = (byte) (numberToEncode % 10);

                    numberToEncode = numberToEncode / 10;
                }

                // Check the checkSum
                int checkSum = 0;
                for (int i = 0; i < 13; i++) {
                    // Multiply by 3 if it is in position 12, 10, ... , 1
                    // i.e. i = 1, 3, 5, ... , 11
                    checkSum += digitsToEncode[i] * ((i % 2) * 2 + 1);
                }
                checkSum %= 10;
                if (!ignoreCheckDigit && checkSum != 0) {
                    // Get the valid version of the checksum
                    // Get it using checksum - check digit
                    // (it was already added and needs to be removed to generate the correct one)
                    throw new CheckDigitException((10 - (checkSum - digitsToEncode[12])) % 10);
                }

                // Encode the first 6 digits
                int digit;
                for (digit = 1; digit < 7; digit++) {
                    // Check if L or R digits should be used
                    boolean LR = EAN13_FIRST_6[digitsToEncode[0]][digit - 1];
                    cBytes.addBits(LR ? L[digitsToEncode[digit]] : G[digitsToEncode[digit]], 7);
                }

                // Encode the middle bars
                cBytes.addBits(0b01010, 5);

                // Encode the last 6 digits
                for (; digit < 13; digit++) {
                    cBytes.addBits(R[digitsToEncode[digit]], 7);
                }

                // Remove the first digit if it was a zero
                if (digitsToEncode[0] == 0) {
                    digitsToEncode = Arrays.copyOfRange(digitsToEncode, 1, 13);
                }

                // Encode the end bits and another quiet zone
                cBytes.addBits(0b101, 3);
                cBytes.addBits(0, 9);
                break;
            // case UPC_E:
            // break;
            // case EAN_8:
            // break;
            default:
                System.out.println("That option is not implemented yet");
                noBarcode = true;
        }

        // Stop the program if the barcode was not generated
        if (noBarcode) {
            return;
        }

        // Make the entire image as a bitmap and compress it
        barcodeLine = cBytes.getBytes();
        int barcodeBitmapSize = (barcodeLine.length + 1) * BARCODE_HEIGHT;
        byte[] barcodeBitmap = new byte[barcodeBitmapSize];

        for (int i = 0; i < barcodeBitmapSize; i++) {
            // A line in the barcode bitmap is one byte longer
            int byteWithinLine = (i % (barcodeLine.length + 1)) - 1;

            // Skip the first byte in a line (filter method = 0)
            if (byteWithinLine >= 0) {
                barcodeBitmap[i] = barcodeLine[byteWithinLine];
            }
        }

        Deflater defl = new Deflater();
        defl.setInput(barcodeBitmap);
        defl.finish();
        byte[] deflatedBitmap = new byte[barcodeBitmapSize];

        // Overwrite the original bitmap with the compressed bitmap
        int deflatedSize = defl.deflate(deflatedBitmap);
        if (deflatedSize >= barcodeBitmapSize) {
            // Barcode bitmaps are highly compressable,
            // so this essentially will never happen
            System.out.println("Error:");
            System.out.println("Compressed bitmap is somehow larger than or the same size as the original bitmap.");
            System.out.println("Exiting now.");
            return;
        }

        System.out.println(deflatedSize);

        // Trim the array
        deflatedBitmap = Arrays.copyOf(deflatedBitmap, deflatedSize);

        // Actually write the png file
        // First get the name
        char[] encodedNumber = new char[digitsToEncode.length];
        for (int i = 0; i < digitsToEncode.length; i++) {
            // Get the digit as a character
            encodedNumber[i] = (char) (digitsToEncode[i] | 0b110000);
        }

        // Open the file
        try {
            fout = new FileOutputStream(String.copyValueOf(encodedNumber) + ".png");

            fout.write(PNG_MAGIC_NUMBER);

            CRC32 crc = new CRC32();
            byte[] byteToWrite;

            // IHDR Chunk
            fout.write(bigEndian(13));
            byteToWrite = "IHDR".getBytes(StandardCharsets.ISO_8859_1);
            crc.update(byteToWrite);
            fout.write(byteToWrite);
            // Width
            byteToWrite = bigEndian(cBytes.getBitCount());
            crc.update(byteToWrite);
            fout.write(byteToWrite);
            // Height
            byteToWrite = bigEndian(BARCODE_HEIGHT);
            crc.update(byteToWrite);
            fout.write(byteToWrite);
            // A little more data
            byteToWrite = new byte[] { (byte) 1, // Bit depth
                    (byte) 3, // Color type (indexed)
                    (byte) 0, // Compression method
                    (byte) 0, // Filter method
                    (byte) 0 // Interlace method = "no"
            };
            crc.update(byteToWrite);
            fout.write(byteToWrite);
            // Get crc for IHDR
            fout.write(bigEndian(crc.getValue()));
            crc.reset();

            // PLTE
            fout.write(bigEndian(6));
            byteToWrite = "PLTE".getBytes(StandardCharsets.ISO_8859_1);
            crc.update(byteToWrite);
            fout.write(byteToWrite);
            // color0 = white
            byteToWrite = new byte[] { (byte) -1, (byte) -1, (byte) -1 };
            crc.update(byteToWrite);
            fout.write(byteToWrite);
            // color1 = black
            byteToWrite = new byte[] { (byte) 0, (byte) 0, (byte) 0 };
            crc.update(byteToWrite);
            fout.write(byteToWrite);
            // Get crc
            fout.write(bigEndian(crc.getValue()));
            crc.reset();

            // pHYs
            fout.write(bigEndian(9));
            byteToWrite = "pHYs".getBytes(StandardCharsets.ISO_8859_1);
            crc.update(byteToWrite);
            fout.write(byteToWrite);
            // Pixels per unit, X axis
            byteToWrite = bigEndian(PIXELS_PER_UNIT);
            crc.update(byteToWrite);
            fout.write(byteToWrite);
            // Pixels per unit, Y axis
            // byteToWrite = bigEndian(PIXELS_PER_UNIT); // Already done
            crc.update(byteToWrite);
            fout.write(byteToWrite);
            // Unit specifier = meters
            crc.update(1);
            fout.write(1);
            // Get crc
            fout.write(bigEndian(crc.getValue()));
            crc.reset();

            // IDAT
            fout.write(bigEndian(deflatedSize));
            byteToWrite = "IDAT".getBytes(StandardCharsets.ISO_8859_1);
            crc.update(byteToWrite);
            fout.write(byteToWrite);
            // The actual data
            crc.update(deflatedBitmap);
            fout.write(deflatedBitmap);
            // Get crc
            fout.write(bigEndian(crc.getValue()));
            crc.reset();

            // IEND
            fout.write(bigEndian(0));
            byteToWrite = "IEND".getBytes(StandardCharsets.ISO_8859_1);
            crc.update(byteToWrite);
            fout.write(byteToWrite);
            // Get crc (probably always 0xae426082)
            fout.write(bigEndian(crc.getValue()));

            // Close the file
            fout.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Could not open output file for some reason:");
            System.out.println(ex);
        } catch (IOException ex) {
            System.out.println("Error writing file:");
            System.out.println(ex);
        }

    }

    private static byte[] bigEndian(long input) {
        byte[] output = new byte[4];

        for (int i = 3; i >= 0; i--) {
            output[i] = (byte) input;
            input = input >> 8;
        }

        return output;
    }
}
