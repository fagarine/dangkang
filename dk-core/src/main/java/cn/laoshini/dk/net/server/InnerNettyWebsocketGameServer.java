package cn.laoshini.dk.net.server;

import java.util.List;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.net.msg.AbstractMessage;
import cn.laoshini.dk.net.session.AbstractSession;
import cn.laoshini.dk.net.session.NettySession;
import cn.laoshini.dk.register.GameServerRegisterAdaptor;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.MessageUtil;

/**
 * @author fagarine
 */
public class InnerNettyWebsocketGameServer<S, M> extends AbstractInnerNettyGameServer<S, M> {

    private EventLoopGroup bossGroup;

    public InnerNettyWebsocketGameServer(GameServerRegisterAdaptor<S, M> gameServerRegister) {
        super(gameServerRegister, "netty-web-socket-server");
    }

    @Override
    public void run() {
        super.run();

        int port = getPort();
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            LogUtil.info("Websocket游戏 [{}] 开始启动...", getGameName());
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //设置log监听器，并且日志级别为debug，方便观察运行流程
                            ch.pipeline().addLast("logging", new LoggingHandler("DEBUG"));
                            //设置解码器
                            ch.pipeline().addLast("http-codec", new HttpServerCodec());
                            // 聚合器，使用websocket会用到
                            ch.pipeline().addLast("aggregator", new HttpObjectAggregator(65536));
                            // 用于大数据的分区传输
                            ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
                            // websocket服务器处理的协议，用于指定给客户端连接访问的路由："/ws"
                            // 本handler会帮你处理一些繁重的复杂的事。会帮你处理握手动作: handshaking
                            // 对于websocket来讲，都是以frames进行传输的，不同的数据类型对应的frames也不同。
                            ch.pipeline().addLast(new WebSocketServerProtocolHandler("/ws"));
                            // 自定义的业务handler
                            ch.pipeline().addLast("handler", new WebsocketServerHandler());
                        }
                    });

            Channel ch = b.bind(port).sync().channel();
            LogUtil.start("Websocket游戏 [{}] 成功绑定端口 [{}]，启动成功", getGameName(), port);

            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            throw new BusinessException("websocket.start.error",
                    String.format("Websocket游戏服 [%s] 启动异常", getServerConfig()));
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class WebsocketServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
            // 处理websocket客户端的消息
            LogUtil.debug("收到websocket消息");
            readWebSocketFrame(ctx, msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            LogUtil.error("Websocket server channel exception", cause);
            ctx.close();
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            Channel channel = ctx.channel();
            LogUtil.session("Websocket连接建立成功:" + channel);

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

            LogUtil.session("Websocket server channel channelInactive:" + ctx.channel());

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
    }

    private void readWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame msg) {
        Channel channel = ctx.channel();
        Long channelId = getChannelId(channel);
        GameSubject subject = getInnerSession(channelId).getSubject();

        // 消息体
        M message;
        if (msg.content().readableBytes() > 2 || msg instanceof BinaryWebSocketFrame) {
            // 消息解码
            message = getGameServerRegister().decoder().decode(msg.content(), subject);
        } else {
            String jsonStr = ((TextWebSocketFrame) msg).text();
            List<AbstractMessage<?>> messages = MessageUtil.jsonStrToMessage(jsonStr);
            if (messages.isEmpty()) {
                message = null;
            } else {
                message = (M) messages.get(0);
            }
        }

        LogUtil.debug("读取到websocket消息:" + message);

        // 消息分发
        dispatchMessage(channelId, message);
    }

    @Override
    protected void shutdown0() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

}
