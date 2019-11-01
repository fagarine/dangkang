package cn.laoshini.dk.exception;

import cn.laoshini.dk.constant.GameCodeEnum;

/**
 * @author fagarine
 */
public class MessageException extends DkRuntimeException {

    private GameCodeEnum gameCode;

    private int errorCode;

    public MessageException(GameCodeEnum gameCode, String errorKey, String message) {
        super(errorKey, message);
        this.gameCode = gameCode;
    }

    public MessageException(GameCodeEnum gameCode, String message, Throwable cause) {
        super(message, cause);
        this.gameCode = gameCode;
    }

    public MessageException(GameCodeEnum gameCode, String errorKey, String message, Throwable cause) {
        super(errorKey, message, cause);
        this.gameCode = gameCode;
    }

    public MessageException(int errorCode, String message) {
        super("game.message.exception", message);
        this.errorCode = errorCode;
    }

    public GameCodeEnum getGameCode() {
        return gameCode;
    }

    public void setGameCode(GameCodeEnum gameCode) {
        this.gameCode = gameCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
