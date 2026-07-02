// common/exception/ResourceNotFoundException.java
package hsf302.se2033jv.project_hsf302_group2.common.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}