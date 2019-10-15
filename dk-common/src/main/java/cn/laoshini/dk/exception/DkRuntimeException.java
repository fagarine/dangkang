package cn.laoshini.dk.exception;

/**
 * @author fagarine
 */
public class DkRuntimeException extends RuntimeException {

    private String errorKey;

    public DkRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DkRuntimeException(String errorKey, String message) {
        super(message);
        this.errorKey = errorKey;
    }

    public DkRuntimeException(String errorKey, String message, Throwable cause) {
        super(message, cause);
        this.errorKey = errorKey;
    }

    public String getErrorKey() {
        return errorKey;
    }

    public void setErrorKey(String errorKey) {
        this.errorKey = errorKey;
    }
}
