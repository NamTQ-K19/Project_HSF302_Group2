package hsf302.se2033jv.project_hsf302_group2.common.exception;

public class ReservationNotFoundException extends ReservationException {

    public ReservationNotFoundException(String message) {
        super(message);
    }

    public ReservationNotFoundException(Integer reservationId) {
        super("Không tìm thấy đặt bàn với ID: " + reservationId);
    }
}