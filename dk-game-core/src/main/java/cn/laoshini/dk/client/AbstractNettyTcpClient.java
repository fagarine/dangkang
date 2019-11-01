package cn.laoshini.dk.client;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import cn.laoshini.dk.net.codec.INettyMessageDecoder;
import cn.laoshini.dk.net.codec.INettyMessageEncoder;
import cn.laoshini.dk.net.msg.IMessageDispatcher;
import cn.laoshini.dk.net.session.IMessageSender;
import cn.laoshini.dk.net.session.ISessionCreator;
import cn.laoshini.dk.net.session.NettySession;
import cn.laoshini.dk.register.IMessageRegister;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.StringUtil;

import static cn.laoshini.dk.constant.GameConstant.MAX_FRAME_LENGTH;
import static cn.laoshini.dk.constant.GameConstant.MESSAGE_LENGTH_OFFSET;

/**
 * 使用netty实现与TCP服务端通信的抽象类，实现客户端的基本通信功能
 *
 * @param <S> 对应客户端连接的会话类型
 * @param <M> 消息类型
 * @author fagarine
 */
public abstract class AbstractNettyTcpClient<S, M> implements Runnable {

    /**
     * 客户端连服务器，连接不上时的重连等待时间
     */
    public static final int RECONNECT_DELAY = 1000;

    /**
     * 最大重连次数
     */
    public static final int RECONNECT_COUNT = 5;

    private String serverHost;
    private int serverPort;

    protected EventLoopGroup group;
    private boolean connecting = true;
    private AtomicBoolean connected = new AtomicBoolean();

    private ChannelHandlerContext context;
    private S session;

    private INettyMessageEncoder<M> messageEncoder;
    private INettyMessageDecoder<M> messageDecoder;
    private ISessionCreator<S> sessionCreator;
    private IMessageSender<S, M> messageSender;
    private IMessageDispatcher<S, M> messageDispatcher;
    private IMessageRegister messageRegister;
    private Function<M, Integer> idReader;

    @Override
    public void run() {
        checkDepends();

        if (messageRegister != null) {
            messageRegister.action(AbstractNettyTcpClient.class.getClassLoader());
        }

        group = new NioEventLoopGroup();
        ChannelFuture connect = null;
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class);
            // 通过NoDelay禁用Nagle,使消息立即发出去，不用等待到一定的数据量才发出去
            b.option(ChannelOption.TCP_NODELAY, true);
            b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new TcpClientChannelHandler());

            // 尝试连接的次数
            int tryConnectNum = 0;
            LogUtil.info("连接服务器， host:" + serverHost + ", serverPort:" + serverPort);
            while (!connected.get() && tryConnectNum <= RECONNECT_COUNT) {
                try {
                    connect = b.connect(serverHost, serverPort).sync();

                    connect.awaitUninterruptibly();
                    connected.set(true);
                } catch (Exception e) {
                    tryConnectNum++;
                    LogUtil.info("服务器没有连接上，等待重连,已尝试次数: " + tryConnectNum);
                    Thread.sleep(RECONNECT_DELAY);
                }
            }

            connecting = false;
            if (connected.get() && connect != null) {
                LogUtil.info("与服务器连接成功, " + serverHost + ":" + serverPort);
                connect.channel().closeFuture().sync();

                // 客户端已进入同步等待中，只有客户端连接关闭后才会走到这里
                LogUtil.info("连接关闭");
            } else {
                LogUtil.info("连接服务器失败");
            }
        } catch (InterruptedException e) {
            LogUtil.error("连接服务器线程中断", e);
            Thread.currentThread().interrupt();
        } finally {
            group.shutdownGracefully();
        }
    }

    protected void checkDepends() {
        if (StringUtil.isEmptyString(serverHost)) {
            throw new ClientException("服务器地址不能为空");
        }

        if (serverPort < 0 || serverPort >= 65535) {
            throw new ClientException("无效端口号:" + serverPort);
        }

        if (messageEncoder == null) {
            throw new ClientException("消息编码器不能为空");
        }

        if (messageDecoder == null) {
            throw new ClientException("消息解码器不能为空");
        }

        if (messageSender == null) {
            throw new ClientException("消息发送对象不能为空");
        }

        if (sessionCreator == null) {
            sessionCreator = (ISessionCreator<S>) ISessionCreator.DK_SESSION_CREATOR;
        }

        if (messageDispatcher == null) {
            LogUtil.error("未注册消息处理器，服务器消息到达后将会被丢弃！！！");
        }
    }

    public void close() {
        if (connected.get()) {
            // 关闭连接
            group.shutdownGracefully();

            // 重置连接状态
            connected.set(false);
        }
    }

    public void sendMsgToServer(M message) {
        if (isValidConnect()) {
            if (session != null) {
                messageSender.send(session, message);
            } else {
                context.writeAndFlush(message);
            }
        }
    }

    public Integer getMessageId(M message) {
        return idReader.apply(message);
    }

    class TcpClientChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            LogUtil.info("ClientChannelHandler initChannel:" + Thread.currentThread().getId());

            ChannelPipeline pipeLine = ch.pipeline();
            pipeLine.addLast("frameEncoder", new LengthFieldPrepender(MESSAGE_LENGTH_OFFSET));
            pipeLine.addLast("clientEncoder", messageEncoder());

            pipeLine.addLast("frameDecoder",
                    new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, 0, MESSAGE_LENGTH_OFFSET, 0, 4));

            pipeLine.addLast("clientDecoder", messageDecoder());
            pipeLine.addLast("messageHandler", new TcpClientMessageHandler());
        }
    }

    class TcpClientMessageHandler extends SimpleChannelInboundHandler<M> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, M msg) throws Exception {
            LogUtil.info("接收到服务器消息: " + msg);

            if (messageDispatcher != null) {
                messageDispatcher.dispatch(session, msg);
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            context = ctx;
            session = sessionCreator.newSession(new NettySession(ctx.channel()));
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            super.channelUnregistered(ctx);
            LogUtil.info("ClientChannelHandler channelUnregistered:" + Thread.currentThread().getId());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            context = null;
            session = null;
            LogUtil.info("ClientChannelHandler channelInactive:" + Thread.currentThread().getId());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            LogUtil.info("ClientChannelHandler exceptionCaught:" + Thread.currentThread().getId());
            ctx.close();
        }
    }

    public boolean isValidConnect() {
        return connected.get() && context != null && context.channel().isActive();
    }

    public String serverHost() {
        return serverHost;
    }

    public AbstractNettyTcpClient<S, M> setServerHost(String serverHost) {
        this.serverHost = serverHost;
        return this;
    }

    public int serverPort() {
        return serverPort;
    }

    public AbstractNettyTcpClient<S, M> setServerPort(int serverPort) {
        this.serverPort = serverPort;
        return this;
    }

    public boolean isConnecting() {
        return connecting;
    }

    public boolean isConnected() {
        return connected.get();
    }

    public S session() {
        return session;
    }

    public ISessionCreator<S> sessionCreator() {
        return sessionCreator;
    }

    public AbstractNettyTcpClient<S, M> setSessionCreator(ISessionCreator<S> sessionCreator) {
        this.sessionCreator = sessionCreator;
        return this;
    }

    public IMessageSender<S, M> messageSender() {
        return messageSender;
    }

    public AbstractNettyTcpClient<S, M> setMessageSender(IMessageSender<S, M> messageSender) {
        this.messageSender = messageSender;
        return this;
    }

    public INettyMessageEncoder<M> messageEncoder() {
        return messageEncoder;
    }

    public AbstractNettyTcpClient<S, M> setMessageEncoder(INettyMessageEncoder<M> messageEncoder) {
        this.messageEncoder = messageEncoder;
        return this;
    }

    public INettyMessageDecoder<M> messageDecoder() {
        return messageDecoder;
    }

    public AbstractNettyTcpClient<S, M> setMessageDecoder(INettyMessageDecoder<M> messageDecoder) {
        this.messageDecoder = messageDecoder;
        return this;
    }

    public IMessageDispatcher<S, M> messageDispatcher() {
        return messageDispatcher;
    }

    public AbstractNettyTcpClient<S, M> setMessageDispatcher(IMessageDispatcher<S, M> messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
        return this;
    }

    public IMessageRegister messageRegister() {
        return messageRegister;
    }

    public AbstractNettyTcpClient<S, M> setMessageRegister(IMessageRegister messageRegister) {
        this.messageRegister = messageRegister;
        return this;
    }

    public Function<M, Integer> idReader() {
        return idReader;
    }

    /**
     * 设置消息id读取器，其负责从消息对象中读取消息id
     *
     * @param idReader 消息id读取器
     * @return 返回当前对象
     */
    public AbstractNettyTcpClient<S, M> setIdReader(Function<M, Integer> idReader) {
        this.idReader = idReader;
        return this;
    }

}
