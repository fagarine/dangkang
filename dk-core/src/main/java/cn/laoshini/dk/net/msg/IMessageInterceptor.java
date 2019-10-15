package cn.laoshini.dk.net.msg;

/**
 * 消息拦截器接口
 *
 * @param <S> 对应客户端连接的会话类型
 * @param <M> 消息类型
 * @author fagarine
 */
@FunctionalInterface
public interface IMessageInterceptor<S, M> {

    /**
     * 执行拦截检查操作
     *
     * @param session 会话对象
     * @param msg 消息
     * @return 返回是否要拦截消息
     */
    boolean check(S session, M msg);
}
