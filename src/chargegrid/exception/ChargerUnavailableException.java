package chargegrid.exception;

public class ChargerUnavailableException extends ChargeGridException {
    private static final long serialVersionUID = 1L;

    public ChargerUnavailableException(String message) { super(message); }
}
