package cn.laoshini.dk.exception;

/**
 * @author fagarine
 */
public class CacheException extends DkRuntimeException {
    public CacheException(String errorKey, String message) {
        super(errorKey, message);
    }
}
