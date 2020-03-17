package cn.laoshini.dk.net.session;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import cn.laoshini.dk.net.codec.INettyMessageEncoder;

/**
 * 对应Netty Websocket服务器的会话类型
 *
 * @author fagarine
 */
public class NettyWebsocketSession extends NettySession {
    private INettyMessageEncoder<Object> encoder;
    private boolean binaryFrame;

    public NettyWebsocketSession(Channel channel) {
        super(channel);
    }

    @Override
    public void sendMessage(Object message) {
        ByteBuf byteBuf = encoder.encode(message, getSubject());
        WebSocketFrame frame;
        if (binaryFrame) {
            frame = new BinaryWebSocketFrame(byteBuf);
        } else {
            frame = new TextWebSocketFrame(byteBuf);
        }
        super.sendMessage(frame);
    }

    public void setEncoder(INettyMessageEncoder<Object> encoder) {
        this.encoder = encoder;
    }

    public void setBinaryFrame(boolean binaryFrame) {
        this.binaryFrame = binaryFrame;
    }

    @Override
    public void clear() {
        super.clear();

        binaryFrame = false;
        encoder = null;
    }
}
