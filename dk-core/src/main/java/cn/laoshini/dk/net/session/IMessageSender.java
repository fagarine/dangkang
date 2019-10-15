package cn.laoshini.dk.net.session;

/**
 * 消息发送接口定义类
 *
 * @param <S> 对应客户端连接的会话类型
 * @param <M> 消息类型
 * @author fagarine
 */
@FunctionalInterface
public interface IMessageSender<S, M> {

    /**
     * 发送消息
     *
     * @param session 消息所属的会话对象
     * @param msg 消息
     */
    void send(S session, M msg);

    /**
     * 创建并返回一个负责发送消息的默认实现对象
     *
     * @param coreThreads 默认实现使用了线程池，传入线程池的核心线程数
     * @return 返回一个负责发送消息的实现对象
     */
    static IMessageSender defaultSender(int coreThreads) {
        return new DefaultMessageSender<>(coreThreads);
    }
}
