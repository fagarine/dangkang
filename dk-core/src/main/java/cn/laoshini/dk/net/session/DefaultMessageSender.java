package cn.laoshini.dk.net.session;

import cn.laoshini.dk.executor.AbstractOrderedWorker;
import cn.laoshini.dk.executor.OrderedQueuePoolExecutor;
import cn.laoshini.dk.util.LogUtil;

/**
 * 消息发送默认实现类
 *
 * @param <S> 对应客户端连接的会话类型
 * @param <M> 消息类型
 * @author fagarine
 */
final class DefaultMessageSender<S extends AbstractSession, M> implements IMessageSender<S, M> {

    private final int coreThreads;

    private final OrderedQueuePoolExecutor messageSender;

    public DefaultMessageSender(int coreThreads) {
        this.coreThreads = coreThreads;
        messageSender = new OrderedQueuePoolExecutor("message-sender", coreThreads, Integer.MAX_VALUE);
    }

    @Override
    public void send(S session, M msg) {
        messageSender.addTask(session.getId(), new MessageSendWorker(session, msg));
    }

    private class MessageSendWorker extends AbstractOrderedWorker {

        private S session;

        private M msg;

        public MessageSendWorker(S session, M msg) {
            this.session = session;
            this.msg = msg;
        }

        @Override
        protected void action() {
            session.sendMessage(msg);
            LogUtil.s2cMessage("发送消息：{}", msg);
        }
    }
}
