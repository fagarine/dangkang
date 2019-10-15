package cn.laoshini.dk.net.server;

import java.util.List;

import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.register.GameServerRegisterAdaptor;
import cn.laoshini.dk.util.LogUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.MessageToByteEncoder;

import static cn.laoshini.dk.constant.GameConstant.MAX_FRAME_LENGTH;
import static cn.laoshini.dk.constant.GameConstant.MESSAGE_LENGTH_OFFSET;
import static io.netty.buffer.Unpooled.wrappedBuffer;

/**
 * @author fagarine
 */
class InnerNettyTcpGameServer<S, M> extends AbstractInnerNettyGameServer<S, M> {

    InnerNettyTcpGameServer(GameServerRegisterAdaptor<S, M> gameServerRegister) {
        super(gameServerRegister, "netty-tcp-server");
    }

    @Override
    public void run() {
        super.run();

        int port = getPort();

        // 大小为监听的端口数目
        EventLoopGroup accepterGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        LogUtil.info("游戏 [{}] 开始启动...", getGameName());

        try {
            ServerBootstrap b = new ServerBootstrap();
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
            LogUtil.start("游戏 [{}] 成功绑定端口 [{}]，启动成功", getGameName(), port);

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

    private class TcpChannelReaderHandler extends AbstractKeepAliveHandler<M> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, M msg) throws Exception {
            // 消息分发
            dispatchMessage(ctx.channel(), msg);
        }

    }

    private ByteToMessageDecoder newMessageDecoder() {
        return new ByteToMessageDecoder() {
            @Override
            protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
                final byte[] bytes;
                final int offset;
                final int length = in.readableBytes();
                if (in.hasArray()) {
                    bytes = in.array();
                    offset = in.arrayOffset() + in.readerIndex();
                } else {
                    bytes = ByteBufUtil.getBytes(in, in.readerIndex(), length, false);
                    offset = 0;
                }

                out.addAll(getGameServerRegister().decoder().decode(bytes, offset, length));
            }
        };
    }

    private MessageToByteEncoder<M> newMessageEncoder() {
        return new MessageToByteEncoder<M>() {
            @Override
            protected void encode(ChannelHandlerContext ctx, M msg, ByteBuf out) throws Exception {
                byte[] bytes = getGameServerRegister().encoder().encode(msg);
                out.writeBytes(wrappedBuffer(bytes == null ? Constants.EMPTY_BYTES : bytes));
                ctx.flush();
            }
        };
    }

}
