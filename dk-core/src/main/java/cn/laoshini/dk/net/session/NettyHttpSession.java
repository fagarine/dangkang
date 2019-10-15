package cn.laoshini.dk.net.session;

import java.util.List;
import java.util.Map;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;

import cn.laoshini.dk.net.codec.IByteMessageEncoder;

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

    private Map<String, String> headers;

    public NettyHttpSession(Channel channel, boolean isKeepAlive) {
        super(channel);
        setHttpConnect(isKeepAlive);
    }

    @Override
    public void sendMessage(Object message) {
        boolean keepAlive = isHttpKeepAlive();
        byte[] data = encoder().encode(message);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(data));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/octet-stream");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

        if (!keepAlive) {
            channel.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, KEEP_ALIVE);
            channel.write(response);
        }
    }

    public IByteMessageEncoder encoder() {
        return (IByteMessageEncoder) getAttr(HTTP_ENCODER_KEY);
    }

    public void addEncoder(IByteMessageEncoder encoder) {
        add(HTTP_ENCODER_KEY, encoder);
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Map.Entry<String, String>> entries) {
        for (Map.Entry<String, String> entry : entries) {
            headers.put(entry.getKey(), entry.getValue());
        }
    }
}
