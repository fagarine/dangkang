package cn.laoshini.dk.net.msg;

/**
 * 消息分发、消息调度处理
 *
 * @param <S> 对应客户端连接的会话类型
 * @param <M> 消息类型
 * @author fagarine
 */
public interface IMessageDispatcher<S, M> {

    /**
     * 执行消息分发，该方法没有返回值，如果用户想要立即执行逻辑并返回，需要自己使用会话对象封装一下
     *
     * @param session 会话对象
     * @param message 消息
     */
    void dispatch(S session, M message);
}
