package hsf302.se2033jv.project_hsf302_group2.common.exception;

public class DepositPaymentFailedException extends ReservationException {

    public DepositPaymentFailedException(String message) {
        super(message);
    }

    public DepositPaymentFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}