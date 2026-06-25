// common/exception/BusinessException.java
package hsf302.se2033jv.project_hsf302_group2.common.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}