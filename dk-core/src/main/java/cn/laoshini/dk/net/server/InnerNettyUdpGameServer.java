package cn.laoshini.dk.net.server;

import java.util.List;

import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.register.GameServerRegisterAdaptor;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * @author fagarine
 */
class InnerNettyUdpGameServer<S, M> extends AbstractInnerNettyGameServer<S, M> {

    InnerNettyUdpGameServer(GameServerRegisterAdaptor<S, M> gameServerRegister) {
        super(gameServerRegister, "netty-udp-server");
    }

    @Override
    public void run() {
        super.run();

        // 监听的端口
        int port = getPort();

        workerGroup = new NioEventLoopGroup();
        try {
            // udp不能使用ServerBootstrap
            Bootstrap b = new Bootstrap();
            // 设置UDP通道
            b.group(workerGroup).channel(NioDatagramChannel.class)
                    // 支持广播
                    .option(ChannelOption.SO_BROADCAST, true).option(ChannelOption.SO_BACKLOG, 128)
                    // 设置UDP读缓冲区为1M
                    .option(ChannelOption.SO_RCVBUF, 1024 * 1024)
                    // 设置UDP写缓冲区为1M
                    .option(ChannelOption.SO_SNDBUF, 1024 * 1024)
                    // 初始化处理器
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel ch) throws Exception {
                            ChannelPipeline pipeLine = ch.pipeline();
                            idleHandler(pipeLine);

                            pipeLine.addLast("messageHandler", new UdpServerHandler());
                        }
                    });

            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().await();
        } catch (Exception e) {
            throw new BusinessException("udp.start.error", String.format("UDP游戏服 [%s] 启动异常", getServerConfig()));
        } finally {
            workerGroup.shutdownGracefully();
        }

    }

    private class UdpServerHandler extends AbstractKeepAliveHandler<DatagramPacket> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
            // 读取收到的数据
            ByteBuf buf = packet.copy().content();
            byte[] req = new byte[buf.readableBytes()];
            buf.readBytes(req);

            // 消息解码
            List<M> messages = getGameServerRegister().decoder().decode(req, 0, req.length);

            if (messages != null && !messages.isEmpty()) {
                Long channelId = getChannelId(ctx.channel());
                for (M message : messages) {
                    // 消息分发
                    dispatchMessage(channelId, message);
                }
            }
        }
    }

}
