package cn.laoshini.dk.exception;

import cn.laoshini.dk.constant.GameCodeEnum;

/**
 * @author fagarine
 */
public class MessageException extends DkRuntimeException {

    private GameCodeEnum errorCode;

    public MessageException(GameCodeEnum errorCode, String errorKey, String message) {
        super(errorKey, message);
        this.errorCode = errorCode;
    }

    public MessageException(GameCodeEnum errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public MessageException(GameCodeEnum errorCode, String errorKey, String message, Throwable cause) {
        super(errorKey, message, cause);
        this.errorCode = errorCode;
    }

    public GameCodeEnum getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(GameCodeEnum errorCode) {
        this.errorCode = errorCode;
    }
}
