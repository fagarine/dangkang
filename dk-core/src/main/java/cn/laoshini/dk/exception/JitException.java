package cn.laoshini.dk.exception;

/**
 * @author fagarine
 */
public class JitException extends DkRuntimeException {
    public JitException(String message, Throwable cause) {
        super(message, cause);
    }

    public JitException(String errorKey, String message) {
        super(errorKey, message);
    }

    public JitException(String errorKey, String message, Throwable cause) {
        super(errorKey, message, cause);
    }
}
