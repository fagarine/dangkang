package cn.laoshini.dk.client;

import cn.laoshini.dk.exception.DkRuntimeException;

/**
 * 当康游戏客户端异常
 *
 * @author fagarine
 */
public class ClientException extends DkRuntimeException {

    public ClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientException(String message) {
        super("client.exception.key", message);
    }
}
