package cn.laoshini.dk.net.server;

import java.util.concurrent.TimeUnit;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import cn.laoshini.dk.net.msg.IMessageInterceptor;
import cn.laoshini.dk.net.session.AbstractSession;
import cn.laoshini.dk.register.GameServerRegisterAdaptor;
import cn.laoshini.dk.util.ChannelUtil;
import cn.laoshini.dk.util.LogUtil;

/**
 * @author fagarine
 */
public abstract class AbstractInnerNettyGameServer<S, M> extends AbstractInnerGameServer<S, M> {

    private static final AttributeKey<Long> CHANNEL_ID = AttributeKey.valueOf("CHANNEL ID");

    /**
     * Netty服务器中负责用户业务的工作组
     */
    protected EventLoopGroup workerGroup;

    public AbstractInnerNettyGameServer(GameServerRegisterAdaptor<S, M> gameServerRegister, String serverThreadName) {
        super(gameServerRegister, serverThreadName);
    }

    protected void setChannelId(Channel channel, long channelId) {
        Attribute<Long> attribute = channel.attr(CHANNEL_ID);
        attribute.set(channelId);
    }

    protected Long getChannelId(Channel channel) {
        return channel.attr(CHANNEL_ID).get();
    }

    protected long channel2Id(Channel channel) {
        return ChannelUtil.channel2Id(channel);
    }

    protected S getSessionByChannel(Channel channel) {
        Long channelId = getChannelId(channel);
        return channelId == null ? null : getSession(channelId);
    }

    protected AbstractSession getInnerSessionByChannel(Channel channel) {
        Long channelId = getChannelId(channel);
        return channelId == null ? null : getInnerSession(channelId);
    }

    /**
     * 检查消息是否应该被拦截
     *
     * @param session 当前会话对象
     * @param msg 消息
     * @return 返回是否应该拦截
     */
    protected boolean msgShouldIntercept(S session, M msg) {
        if (!getGameServerRegister().messageInterceptors().isEmpty()) {
            for (IMessageInterceptor<S, M> interceptor : getGameServerRegister().messageInterceptors()) {
                if (interceptor.check(session, msg)) {
                    LogUtil.message("消息[{}]被拦截器{}拦截", msg, interceptor);
                    return true;
                }
            }
        }
        return false;
    }

    protected void dispatchMessage(Channel channel, M msg) {
        Long channelId = getChannelId(channel);
        dispatchMessage(getSession(channelId), msg);
    }

    protected void dispatchMessage(long channelId, M msg) {
        dispatchMessage(getSession(channelId), msg);
    }

    protected void dispatchMessage(S session, M msg) {
        if (msgShouldIntercept(session, msg)) {
            return;
        }
        getGameServerRegister().messageDispatcher().dispatch(session, msg);
    }

    @Override
    protected int messageSenderThreads() {
        // 默认使用工作组同样多的线程数
        return ((NioEventLoopGroup) workerGroup).executorCount();
    }

    @Override
    protected void shutdown0() {
        workerGroup.shutdownGracefully();
    }

    protected void idleHandler(ChannelPipeline pipeLine) {
        if (getGameServerRegister().idleTime() > 0) {
            // 心跳 定时检查在线的客户端channel是否空闲
            pipeLine.addLast(new IdleStateHandler(getGameServerRegister().idleTime(), 0, 0, TimeUnit.SECONDS));
            pipeLine.addLast(new HeartbeatHandler());
        }
    }

    private class HeartbeatHandler extends ChannelDuplexHandler {
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            super.userEventTriggered(ctx, evt);
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent e = (IdleStateEvent) evt;
                if (e.state() == IdleState.READER_IDLE) {
                    LogUtil.session("连接触发心跳超时，断开连接:" + ctx.channel());
                    ctx.close();
                }
            }
        }
    }

}
