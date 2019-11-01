package cn.laoshini.dk.net.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
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
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;

import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.net.session.AbstractSession;
import cn.laoshini.dk.net.session.NettyHttpSession;
import cn.laoshini.dk.register.GameServerRegisterAdaptor;
import cn.laoshini.dk.util.LogUtil;

/**
 * @author fagarine
 */
public class InnerNettyHttpGameServer<S, M> extends AbstractInnerNettyGameServer<S, M> {

    private static final String FAVICON_ICO = "/favicon.ico";

    private EventLoopGroup bossGroup;

    public InnerNettyHttpGameServer(GameServerRegisterAdaptor<S, M> gameServerRegister) {
        super(gameServerRegister, "netty-http-server");
    }

    @Override
    public void run() {
        super.run();

        int port = getPort();
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            LogUtil.info("HTTP游戏 [{}] 开始启动...", getGameName());
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            // 使用HttpRequestDecoder & HttpResponseEncoder
                            p.addLast(new HttpServerCodec());
                            // 在处理POST消息体时需要加上
                            p.addLast(new HttpObjectAggregator(1024 * 1024));
                            p.addLast(new HttpServerExpectContinueHandler());
                            p.addLast(new DefaultHttpServerHandler());
                        }
                    });

            Channel ch = b.bind(port).sync().channel();
            LogUtil.start("HTTP游戏 [{}] 成功绑定端口 [{}]，启动成功", getGameName(), port);

            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            throw new BusinessException("http.start.error", String.format("HTTP游戏服 [%s] 启动异常", getServerConfig()));
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class DefaultHttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
            if (shutdown.get()) {
                return;
            }

            if (pause.get()) {
                // 业务暂停时，停止接受客户端消息，或返回提示信息，或考虑其他的处理方式
                return;
            }

            if (msg instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) msg;
                String uri = request.uri();
                if (FAVICON_ICO.equals(uri)) {
                    return;
                }

                LogUtil.info("request uri:" + uri);

                if (msg instanceof HttpContent) {
                    ByteBuf in = ((HttpContent) msg).content();
                    if (msg instanceof LastHttpContent) {
                        boolean keepAlive = HttpUtil.isKeepAlive(request);
                        Channel channel = ctx.channel();

                        long channelId = channel2Id(channel);
                        setChannelId(channel, channelId);
                        NettyHttpSession innerSession = new NettyHttpSession(channel, keepAlive);
                        innerSession.setId(channelId);
                        innerSession.addEncoder(getGameServerRegister().encoder());
                        recordInnerSession(channelId, innerSession);

                        // 记录URI和header等信息
                        innerSession.setUri(uri);
                        innerSession.setHeaders(request.headers().entries());

                        S session = getGameServerRegister().sessionCreator().newSession(innerSession);

                        // 消息解码
                        M message = getGameServerRegister().decoder().decode(in, null);
                        // 消息分发
                        dispatchMessage(session, message);
                    }
                }
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            LogUtil.error("http server channel exception", cause);
            ctx.close();
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);

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

    @Override
    protected void shutdown0() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

}
