package cn.laoshini.dk.net.server;

import java.util.List;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;

import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.net.session.AbstractSession;
import cn.laoshini.dk.net.session.NettySession;
import cn.laoshini.dk.register.GameServerRegisterAdaptor;
import cn.laoshini.dk.util.LogUtil;

import static cn.laoshini.dk.constant.GameConstant.MAX_FRAME_LENGTH;
import static cn.laoshini.dk.constant.GameConstant.MESSAGE_LENGTH_OFFSET;

/**
 * @author fagarine
 */
class InnerNettyTcpGameServer<S, M> extends AbstractInnerNettyGameServer<S, M> {

    private GlobalTrafficShapingHandler trafficShapingHandler;

    private EventLoopGroup accepterGroup;

    InnerNettyTcpGameServer(GameServerRegisterAdaptor<S, M> gameServerRegister) {
        super(gameServerRegister, "netty-tcp-server");
    }

    @Override
    public void run() {
        super.run();

        int port = getPort();

        // 大小为监听的端口数目
        accepterGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        LogUtil.start("TCP游戏 [{}] 开始启动...", getGameName());
        try {
            ServerBootstrap b = new ServerBootstrap();
            trafficShapingHandler = new GlobalTrafficShapingHandler(workerGroup, 5000L);
            b.group(accepterGroup, workerGroup).channel(NioServerSocketChannel.class);
            b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000);
            b.option(ChannelOption.SO_BACKLOG, 1024);
            if (isTcpNoDelay()) {
                b.option(ChannelOption.TCP_NODELAY, true);
            }

            b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            b.childOption(ChannelOption.SO_RCVBUF, 1024);
            b.childOption(ChannelOption.SO_SNDBUF, 4096);

            b.childHandler(newBusinessHandler());

            Channel channel = b.bind(port).sync().channel();
            LogUtil.start("TCP游戏 [{}] 成功绑定端口 [{}]", getGameName(), port);

            // 执行游戏服启动成功后的逻辑
            serverStartsSuccessful();

            channel.closeFuture().sync();
        } catch (Exception e) {
            throw new BusinessException("tcp.start.error", String.format("TCP游戏服 [%s] 启动异常", getServerConfig()));
        } finally {
            workerGroup.shutdownGracefully();
            accepterGroup.shutdownGracefully();
        }
    }

    private ChannelInitializer<SocketChannel> newBusinessHandler() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeLine = ch.pipeline();
                pipeLine.addLast(trafficShapingHandler);
                idleHandler(pipeLine);

                pipeLine.addLast("frameEncoder", new LengthFieldPrepender(MESSAGE_LENGTH_OFFSET));
                pipeLine.addLast("gameEncoder", newMessageEncoder());

                pipeLine.addLast("frameDecoder",
                        new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, 0, MESSAGE_LENGTH_OFFSET, 0, 4));
                pipeLine.addLast("gameDecoder", newMessageDecoder());

                pipeLine.addLast("messageHandler", new TcpChannelReaderHandler());
            }
        };
    }

    private ByteToMessageDecoder newMessageDecoder() {
        return new ByteToMessageDecoder() {
            @Override
            protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
                GameSubject subject = getInnerSessionByChannel(ctx.channel()).getSubject();
                out.add(getGameServerRegister().decoder().decode(in, subject));
            }
        };
    }

    private MessageToByteEncoder<M> newMessageEncoder() {
        return new MessageToByteEncoder<M>() {
            @Override
            protected void encode(ChannelHandlerContext ctx, M msg, ByteBuf out) throws Exception {
                GameSubject subject = getInnerSessionByChannel(ctx.channel()).getSubject();
                ByteBuf buf = getGameServerRegister().encoder().encode(msg, subject);
                out.writeBytes(buf);
            }
        };
    }

    @Override
    protected void shutdown0() {
        accepterGroup.shutdownGracefully();
        super.shutdown0();
    }

    private class TcpChannelReaderHandler extends SimpleChannelInboundHandler<M> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, M msg) {
            if (isShutdown()) {
                return;
            }

            if (isPaused()) {
                // 业务暂停时，停止接受客户端消息，或返回提示信息，或考虑其他的处理方式
                sendPauseMessage(getSessionByChannel(ctx.channel()));
                return;
            }

            LogUtil.c2sMessage("读取到tcp消息:" + msg);
            // 消息分发
            dispatchMessage(ctx.channel(), msg);
        }

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

            incrementOnline();

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
            LogUtil.session("tcp server channel channelInactive:" + ctx.channel());

            decrementOnline();

            Long channelId = getChannelId(ctx.channel());
            S session = removeSession(channelId);
            if (session != null && getGameServerRegister().connectClosedOperation() != null) {
                getGameServerRegister().connectClosedOperation().onDisconnected(session);
            }

            AbstractSession innerSession = removeInnerSession(channelId);
            if (innerSession != null) {
                innerSession.close();
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
