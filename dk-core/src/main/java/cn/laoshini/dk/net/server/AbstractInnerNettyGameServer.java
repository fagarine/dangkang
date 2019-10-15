package cn.laoshini.dk.net.server;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import cn.laoshini.dk.net.msg.IMessageInterceptor;
import cn.laoshini.dk.net.session.AbstractSession;
import cn.laoshini.dk.net.session.NettySession;
import cn.laoshini.dk.register.GameServerRegisterAdaptor;
import cn.laoshini.dk.util.LogUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

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
        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
        String ip = address.getAddress().getHostAddress();
        int port = address.getPort();
        Long id = ip2Long(ip) * 100000L + port;
        return id;
    }

    protected S getSessionByChannel(Channel channel) {
        Long channelId = getChannelId(channel);
        return channelId == null ? null : getSession(channelId);
    }

    /**
     * IP转成长整型
     *
     * @param ip
     * @return
     */
    private static long ip2Long(String ip) {
        long num = 0L;
        if (ip == null) {
            return num;
        }

        try {
            // 去除字符串前的空字符
            ip = ip.replaceAll("[^0-9.]", "");
            String[] ips = ip.split("\\.");
            if (ips.length == 4) {
                num = Long.parseLong(ips[0]) << 24 + Long.parseLong(ips[1]) << 16 + Long.parseLong(ips[2]) << 8 + Long
                        .parseLong(ips[3]);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return num;
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

    /**
     * 长连接处理抽象类
     *
     * @param <T> 消息类型
     */
    protected abstract class AbstractKeepAliveHandler<T> extends SimpleChannelInboundHandler<T> {
        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            super.channelRegistered(ctx);
            LogUtil.session("server channel registered:" + ctx.channel());
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            super.channelUnregistered(ctx);
            LogUtil.session("server channel unregistered:" + ctx.channel());
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            Channel channel = ctx.channel();
            LogUtil.session("连接建立成功:" + channel);

            long channelId = channel2Id(channel);
            setChannelId(channel, channelId);
            NettySession innerSession = new NettySession(channel);
            innerSession.setId(channelId);
            recordInnerSession(channelId, innerSession);

            S session = getGameServerRegister().sessionCreator().newSession(innerSession);
            recordSession(channelId, session);

            getGameServerRegister().connectOpenedOperation().onConnected(session);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            LogUtil.session("server channel channelInactive:" + ctx.channel());

            decrementOnline();

            Long channelId = getChannelId(ctx.channel());
            AbstractSession innerSession = removeInnerSession(channelId);
            if (innerSession != null) {
                innerSession.close();
            }

            S session = removeSession(channelId);
            if (session != null && getGameServerRegister().connectClosedOperation() != null) {
                getGameServerRegister().connectClosedOperation().onDisconnected(session);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            LogUtil.session("server channel exceptionCaught:" + ctx.channel());

            if (getGameServerRegister().connectExceptionOperation() != null) {
                getGameServerRegister().connectExceptionOperation()
                        .onException(getSessionByChannel(ctx.channel()), cause);
            }

            if (ctx.channel().isActive()) {
                ctx.close();
            }
        }
    }
}
