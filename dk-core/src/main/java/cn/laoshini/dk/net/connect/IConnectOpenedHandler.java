package cn.laoshini.dk.net.connect;

/**
 * 客户端连接建立时的处理接口
 *
 * @author fagarine
 */
@FunctionalInterface
public interface IConnectOpenedHandler<S> {

    /**
     * 连接建立时的处理
     *
     * @param session 连接对应的会话对象
     */
    void onConnected(S session);
}
