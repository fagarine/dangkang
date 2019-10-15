package cn.laoshini.dk.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import cn.laoshini.dk.constant.AttributeKeyConstant;
import cn.laoshini.dk.constant.GameServerProtocolEnum;
import cn.laoshini.dk.domain.GameServerConfig;
import cn.laoshini.dk.domain.Player;
import cn.laoshini.dk.event.ChannelCloseEvent;
import cn.laoshini.dk.eventbus.EventMgr;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.net.codec.INettyMessageDecoder;
import cn.laoshini.dk.net.codec.INettyMessageEncoder;
import cn.laoshini.dk.server.channel.INettyChannelReader;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.NetUtil;

import static cn.laoshini.dk.constant.GameConstant.MAX_FRAME_LENGTH;
import static cn.laoshini.dk.constant.GameConstant.MESSAGE_LENGTH_OFFSET;

/**
 * TCP游戏服务器，使用netty实现
 *
 * @param <MessageType> 与客户端交互时的消息对象类型
 * @author fagarine
 */
public abstract class AbstractNettyTcpGameServer<MessageType> extends AbstractGameServer {

    /**
     * 协议到达后的处理对象，GameServer的实现类可以通过{@link #getChannelReader()}方法设置该值
     */
    protected INettyChannelReader<MessageType> channelReader;

    private EventLoopGroup accepterGroup;

    private EventLoopGroup workerGroup;

    public AbstractNettyTcpGameServer(GameServerConfig serverConfig, String serverThreadName) {
        super(serverConfig, serverThreadName);
    }

    @Override
    public void run() {
        super.run();

        // 监听的端口
        int port = getPort();
        // 端口是否已经被占用
        if (!NetUtil.localPortAble(port)) {
            throw new BusinessException("tcp.port.bind",
                    String.format("本地端口 [%d] 已被占用，TCP游戏 [%s] 服务器启动失败", port, getGameName()));
        }

        channelReader = getChannelReader();

        // 大小为监听的端口数目
        accepterGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(8);

        LogUtil.info("游戏 [{}] 开始启动...", getGameName());

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(accepterGroup, workerGroup).channel(NioServerSocketChannel.class);

            b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000);
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.option(ChannelOption.TCP_NODELAY, true);
            b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            b.childOption(ChannelOption.SO_RCVBUF, 1024);
            b.childOption(ChannelOption.SO_SNDBUF, 4096);

            b.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast("frameEncoder", new LengthFieldPrepender(MESSAGE_LENGTH_OFFSET));
                    ch.pipeline().addLast(getMessageEncoder());

                    ch.pipeline().addLast("frameDecoder",
                            new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, 0, MESSAGE_LENGTH_OFFSET, 0, 4));
                    ch.pipeline().addLast(getMessageDecoder());

                    ch.pipeline().addLast(messageHandler());
                }
            });

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

    /**
     * 返回协议编码工具类
     *
     * @return
     */
    protected abstract INettyMessageEncoder<MessageType> getMessageEncoder();

    /**
     * 返回协议解码工具类
     *
     * @return
     */
    protected abstract INettyMessageDecoder<MessageType> getMessageDecoder();

    /**
     * 返回处理channel的具体的handler
     *
     * @return 返回处理channel的具体的handler
     */
    protected ChannelHandler messageHandler() {
        return new DefaultMessageHandler();
    }

    /**
     * 返回读取处理channel协议的对象
     *
     * @return 返回读取处理channel协议的对象
     */
    public abstract INettyChannelReader<MessageType> getChannelReader();

    @Override
    protected void shutdown0() {
        workerGroup.shutdownGracefully();
        accepterGroup.shutdownGracefully();

        // 等待端口释放，最多尝试100次
        int count = 0;
        while (!NetUtil.localPortAble(getPort())) {
            if (count++ > 100) {
                throw new BusinessException("release.port.fail",
                        String.format("TCP游戏服 [%s] 释放端口 [%d] 失败，请稍候重试", getGameName(), getPort()));
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class DefaultMessageHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            try {
                super.channelRead(ctx, msg);
                if (shutdown.get()) {
                    return;
                }

                if (pause.get()) {
                    // 业务暂停时，停止接受客户端消息，或返回提示信息，或考虑其他的处理方式
                    return;
                }

                /**
                 * 到达这里的消息已经经过{@link #getMessageDecoder()} 返回的解码器处理过，
                 * 所以只要没有配置错，这里的类型不应该有问题
                 */
                channelReader.channelRead(ctx, (MessageType) msg);
            } catch (Exception e) {
                LogUtil.error(String.format("GameServer[%s]消息读取处理出错", getServerThreadName()), e);
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            LogUtil.session("建立连接成功:[{}]", ctx.channel());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            Player player = ctx.channel().attr(AttributeKeyConstant.PLAYER).get();
            if (player != null) {
                EventMgr.getInstance().post(new ChannelCloseEvent(player));
            }
            LogUtil.session("断开连接成功:[{}]", ctx.channel());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            LogUtil.error("会话[{}]发生异常[{}]", ctx.channel(), cause.getMessage());
            LogUtil.session("会话[{}]发生异常[{}]", ctx.channel());
            ctx.close();
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            super.userEventTriggered(ctx, evt);
            // 心跳处理
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                // 读超时
                if (event.state() == IdleState.READER_IDLE) {
                    LogUtil.session("{}空闲超时关闭连接", ctx.channel());
                    ctx.close();
                }
            }
        }
    }

    @Override
    public GameServerProtocolEnum getProtocolType() {
        return GameServerProtocolEnum.TCP;
    }
}
