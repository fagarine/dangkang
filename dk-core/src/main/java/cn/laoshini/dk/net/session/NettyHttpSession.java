package cn.laoshini.dk.net.session;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;

import cn.laoshini.dk.net.codec.INettyMessageEncoder;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * 对应Netty Http服务器的会话类型
 *
 * @author fagarine
 */
public class NettyHttpSession extends NettySession {

    private static final String HTTP_ENCODER_KEY = "HTTP ENCODER";

    private String uri;

    private String method;

    private Map<String, String> headers = new HashMap<>();

    public NettyHttpSession(Channel channel, boolean isKeepAlive) {
        super(channel);
        setHttpConnect(isKeepAlive);
    }

    @Override
    public void sendMessage(Object message) {
        boolean keepAlive = isHttpKeepAlive();
        ByteBuf data = encoder().encode(message, getSubject());
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, data);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/octet-stream");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

        if (!keepAlive) {
            channel.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, KEEP_ALIVE);
            channel.writeAndFlush(response);
        }
    }

    public INettyMessageEncoder<Object> encoder() {
        return (INettyMessageEncoder<Object>) getAttr(HTTP_ENCODER_KEY);
    }

    public <M> void addEncoder(INettyMessageEncoder<M> encoder) {
        add(HTTP_ENCODER_KEY, encoder);
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Map.Entry<String, String>> entries) {
        for (Map.Entry<String, String> entry : entries) {
            headers.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        super.clear();
        uri = null;
        method = null;
        headers.clear();
        headers = null;
    }
}
