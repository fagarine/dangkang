package cn.laoshini.dk.net.connect;

/**
 * 客户端连接关闭时的处理接口
 *
 * @author fagarine
 */
@FunctionalInterface
public interface IConnectClosedHandler<S> {

    /**
     * 执行连接关闭时的操作
     *
     * @param session 连接对应的会话对象
     */
    void onDisconnected(S session);
}
