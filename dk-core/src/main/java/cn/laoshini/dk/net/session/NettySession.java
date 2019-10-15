package cn.laoshini.dk.net.session;

import java.net.InetSocketAddress;

import io.netty.channel.Channel;

/**
 * 对应netty连接的会话
 *
 * @author fagarine
 */
public class NettySession extends AbstractSession<Channel> {

    public NettySession(Channel channel) {
        super(channel);
    }

    @Override
    public String getIp() {
        InetSocketAddress address = (InetSocketAddress) getChannel().remoteAddress();
        return address.getAddress().getHostAddress();
    }

    @Override
    public boolean isConnect() {
        return channel != null && channel.isActive();
    }

    @Override
    public void close() {
        if (isConnect()) {
            channel.close();
            channel = null;
        }
        clear();
    }

    @Override
    public void sendMessage(Object message) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(message);
        }
    }
}
