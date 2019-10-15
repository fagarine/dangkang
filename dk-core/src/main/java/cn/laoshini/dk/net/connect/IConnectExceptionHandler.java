package cn.laoshini.dk.net.connect;

/**
 * 客户端连接异常处理接口
 *
 * @author fagarine
 */
@FunctionalInterface
public interface IConnectExceptionHandler<S> {

    /**
     * 执行连接异常逻辑
     *
     * @param session 连接关联的会话对象
     * @param cause 异常原因
     */
    void onException(S session, Throwable cause);
}
