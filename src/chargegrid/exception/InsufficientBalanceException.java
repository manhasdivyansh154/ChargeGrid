package chargegrid.exception;

public class InsufficientBalanceException extends ChargeGridException {
    private static final long serialVersionUID = 1L;

    public InsufficientBalanceException(String message) { super(message); }
}
