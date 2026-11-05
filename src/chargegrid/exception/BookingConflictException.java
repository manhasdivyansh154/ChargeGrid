package chargegrid.exception;

public class BookingConflictException extends ChargeGridException {
    private static final long serialVersionUID = 1L;

    public BookingConflictException(String message) { super(message); }
}
