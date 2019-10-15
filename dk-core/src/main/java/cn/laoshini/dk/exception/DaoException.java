package cn.laoshini.dk.exception;

/**
 * @author fagarine
 */
public class DaoException extends DkRuntimeException {
    public DaoException(String errorKey, String message) {
        super(errorKey, message);
    }

    public DaoException(String errorKey, String message, Throwable cause) {
        super(errorKey, message, cause);
    }
}
