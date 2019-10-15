package cn.laoshini.dk.exception;

/**
 * @author fagarine
 */
public class BusinessException extends DkRuntimeException {
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(String errorKey, String message) {
        super(errorKey, message);
    }

    public BusinessException(String errorKey, String message, Throwable cause) {
        super(errorKey, message, cause);
    }
}
