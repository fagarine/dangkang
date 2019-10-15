package cn.laoshini.dk.server.sender;

import io.netty.channel.ChannelHandlerContext;

import cn.laoshini.dk.excutor.AbstractOrderedWorker;
import cn.laoshini.dk.excutor.DkExecutors;
import cn.laoshini.dk.excutor.IOrderedExecutor;
import cn.laoshini.dk.net.msg.RespMessage;

/**
 * @author fagarine
 */
public enum NettyMessageSender {
    /**
     * 枚举单例
     */
    INSTANCE;

    private IOrderedExecutor<Long> sendExecutor = DkExecutors.newOrderedExecutor("", 20, -1);

    public static void sendMsg(RespMessage<?> message, ChannelHandlerContext ctx) {

    }

    private static class sendWorker extends AbstractOrderedWorker {

        private RespMessage<?> message;

        private ChannelHandlerContext ctx;

        @Override
        protected void action() {
            ctx.write(message);
        }
    }
}
