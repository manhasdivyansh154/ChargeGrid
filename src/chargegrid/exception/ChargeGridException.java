package chargegrid.exception;

/** Base class for all business-rule errors in ChargeGrid. */
public class ChargeGridException extends Exception {
    private static final long serialVersionUID = 1L;

    public ChargeGridException(String message) { super(message); }
    public ChargeGridException(String message, Throwable cause) { super(message, cause); }
}
