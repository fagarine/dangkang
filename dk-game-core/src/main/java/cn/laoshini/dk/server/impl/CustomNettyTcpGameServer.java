package cn.laoshini.dk.server.impl;

import cn.laoshini.dk.domain.GameServerConfig;
import cn.laoshini.dk.net.codec.CustomNettyMessageDecoder;
import cn.laoshini.dk.net.codec.CustomNettyMessageEncoder;
import cn.laoshini.dk.net.codec.INettyMessageDecoder;
import cn.laoshini.dk.net.codec.INettyMessageEncoder;
import cn.laoshini.dk.net.msg.INettyDto;
import cn.laoshini.dk.net.msg.ReqNettyCustomMessage;
import cn.laoshini.dk.server.AbstractNettyTcpGameServer;
import cn.laoshini.dk.server.channel.INettyChannelReader;
import cn.laoshini.dk.server.channel.NettyCustomMessageChannelReader;

/**
 * 使用自定义消息类型与客户端通信的Netty TCP游戏服务器
 *
 * @author fagarine
 */
public class CustomNettyTcpGameServer extends AbstractNettyTcpGameServer<ReqNettyCustomMessage<INettyDto>> {

    public CustomNettyTcpGameServer(GameServerConfig gameConfig) {
        super(gameConfig, "CustomTcpServer");
    }

    @Override
    protected INettyMessageEncoder<ReqNettyCustomMessage<INettyDto>> getMessageEncoder() {
        return new CustomNettyMessageEncoder();
    }

    @Override
    protected INettyMessageDecoder<ReqNettyCustomMessage<INettyDto>> getMessageDecoder() {
        return new CustomNettyMessageDecoder();
    }

    @Override
    public INettyChannelReader<ReqNettyCustomMessage<INettyDto>> getChannelReader() {
        return new NettyCustomMessageChannelReader();
    }

}
