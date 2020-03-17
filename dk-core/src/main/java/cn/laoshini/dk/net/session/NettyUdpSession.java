package cn.laoshini.dk.net.session;

import java.net.InetSocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;

import cn.laoshini.dk.net.codec.INettyMessageEncoder;

/**
 * 对应Netty Udp服务器的会话类型
 *
 * @author fagarine
 */
public class NettyUdpSession extends NettySession {
    private INettyMessageEncoder<Object> encoder;
    private String hostName;
    private int port;

    public NettyUdpSession(Channel channel) {
        super(channel);
    }

    @Override
    public void sendMessage(Object message) {
        ByteBuf byteBuf = encoder.encode(message, getSubject());
        InetSocketAddress address = new InetSocketAddress(hostName, port);
        DatagramPacket packet = new DatagramPacket(byteBuf, address);
        super.sendMessage(packet);
    }

    public void setEncoder(INettyMessageEncoder<Object> encoder) {
        this.encoder = encoder;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void clear() {
        super.clear();

        encoder = null;
        hostName = null;
        port = 0;
    }
}
