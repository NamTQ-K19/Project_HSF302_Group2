package hsf302.se2033jv.project_hsf302_group2.common.exception;

public class TableNotAvailableException extends ReservationException {

    public TableNotAvailableException(String message) {
        super(message);
    }

    public TableNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}