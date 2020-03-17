package cn.laoshini.dk.net.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.net.codec.INettyMessageEncoder;
import cn.laoshini.dk.net.session.NettyUdpSession;
import cn.laoshini.dk.register.GameServerRegisterAdaptor;
import cn.laoshini.dk.register.UdpGameServerRegister;
import cn.laoshini.dk.util.LogUtil;

/**
 * Netty Udp游戏服务器
 *
 * @author fagarine
 */
class InnerNettyUdpGameServer<S, M> extends AbstractInnerNettyGameServer<S, M> {

    private boolean buildSession;

    InnerNettyUdpGameServer(GameServerRegisterAdaptor<S, M> gameServerRegister) {
        super(gameServerRegister, "netty-udp-server");
    }

    @Override
    public void run() {
        super.run();

        if (getGameServerRegister() instanceof UdpGameServerRegister) {
            UdpGameServerRegister register = (UdpGameServerRegister) getGameServerRegister();
            buildSession = register.isBuildSession();
        }

        // 监听的端口
        int port = getPort();

        workerGroup = new NioEventLoopGroup();
        try {
            // udp不能使用ServerBootstrap
            Bootstrap b = new Bootstrap();
            LogUtil.start("UDP游戏 [{}] 开始启动...", getGameName());
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
                            pipeLine.addLast("messageHandler", new UdpServerHandler());
                        }
                    });

            ChannelFuture f = b.bind(port).sync();
            LogUtil.start("UDP游戏 [{}] 成功绑定端口 [{}]", getGameName(), port);

            // 执行游戏服启动成功后的逻辑
            serverStartsSuccessful();

            f.channel().closeFuture().await();
        } catch (Exception e) {
            throw new BusinessException("udp.start.error", String.format("UDP游戏服 [%s] 启动异常", getServerConfig()));
        } finally {
            workerGroup.shutdownGracefully();
        }

    }

    @Override
    protected void shutdown0() {
        super.shutdown0();

        clearInnerSessions();
        clearSessions();
    }

    private class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
            if (isShutdown()) {
                return;
            }

            if (isPaused()) {
                // 业务暂停时，停止接受客户端消息，或返回提示信息，或考虑其他的处理方式
                sendPauseMessage(getSessionByChannel(ctx.channel()));
                return;
            }

            // 读取收到的数据
            ByteBuf buf = packet.copy().content();
            byte[] req = new byte[buf.readableBytes()];
            buf.readBytes(req);

            // 消息解码
            M message = getGameServerRegister().decoder().decode(buf, null);
            LogUtil.c2sMessage("读取到udp消息:" + message);

            if (message != null) {
                S session = null;
                if (buildSession) {
                    /*
                     * 由于系统会在客户端消息到达时，创建会话对象，如果服务器不停止则不会清除，用户需要根据自己的情况决定是否清除这些数据。
                     * 保留会话对象的原因：
                     * 由于UDP面向无连接，如果服务端要想客户端主动推送消息，需要知道客户端的hostname和port，这些数据保存在{@link NettyUdpSession}中，以备用户之需。
                     */
                    Channel channel = ctx.channel();
                    long channelId = channel2Id(channel);
                    setChannelId(channel, channelId);
                    NettyUdpSession innerSession = new NettyUdpSession(channel);
                    innerSession.setId(channelId);
                    innerSession.setPort(packet.sender().getPort());
                    innerSession.setHostName(packet.sender().getHostName());
                    innerSession.setEncoder((INettyMessageEncoder) getGameServerRegister().encoder());
                    recordInnerSession(channelId, innerSession);

                    session = getGameServerRegister().sessionCreator().newSession(innerSession);
                }

                // 消息分发
                dispatchMessage(session, message);
            }
        }
    }
}
