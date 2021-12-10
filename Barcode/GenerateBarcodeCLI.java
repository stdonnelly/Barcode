package Barcode;

import java.util.InputMismatchException;
import java.util.Scanner;

public class GenerateBarcodeCLI {

    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);
        BarcodeGenerator.BarcodeType barcodeType = null;
        long numberToEncode;

        // Determine what barcode type should be generated
        System.out.println("What type of barcode should be generated?");
        System.out.println("1. UPC-A");
        System.out.println("2. UPC-E");
        System.out.println("3. EAN-13");
        System.out.println("4. EAN-8");

        // Loop until a valid barcode type is entered
        do {
            try {
                switch (scan.nextInt()) {
                    case 1:
                        barcodeType = BarcodeGenerator.BarcodeType.UPC_A;
                        break;
                    case 2:
                        barcodeType = BarcodeGenerator.BarcodeType.UPC_E;
                        break;
                    case 3:
                        barcodeType = BarcodeGenerator.BarcodeType.EAN_13;
                        break;
                    case 4:
                        barcodeType = BarcodeGenerator.BarcodeType.EAN_8;
                        break;
                    default:
                        System.out.println("That option has not been implemented, please try a different option.");
                }
            } catch (InputMismatchException e) {
                System.out.println("You must input a number.");
                scan.nextLine(); // Throw away remaining input
            }
        } while (barcodeType == null);

        // Get the numbers to encode
        System.out.println("What is the number you wish to encode?");
        numberToEncode = scan.nextLong();
        // Throw away anything else on that line (especially the newline)
        scan.nextLine();

        try {
            BarcodeGenerator.generate(barcodeType, numberToEncode, false);
        } catch (CheckDigitException exc) {
            System.out.println("Invalid check digit.");
            System.out.println("Expected \"" + exc.getValidCheckDigit() + "\"");
        }

        scan.close();
    }

}
