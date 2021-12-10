package Barcode;

public class CheckDigitException extends IllegalArgumentException {

    private int validCheckDigit;
    
    /**
     * Constructor with no arguments
     */
    public CheckDigitException() {
        super();
    }

    /**
     * Constructor with string argument
     * @param s String passed to IllegalArgumentException constructor
     */
    public CheckDigitException(String s) {
        super(s);
    }

    /**
     * Constructor with suggestion for valid check digit
     * @param validCheckDigit the number the check digit should be
     */
    public CheckDigitException(int validCheckDigit) {
        super(Integer.toString(validCheckDigit));

        this.validCheckDigit = validCheckDigit;
    }

    /**
     * Getter for validCheckDigit
     * @return validCheckDigit
     */
    public int getValidCheckDigit() {
        return validCheckDigit;
    }
}
