package cn.laoshini.dk.server.impl;

import cn.laoshini.dk.domain.GameServerConfig;
import cn.laoshini.dk.net.codec.INettyMessageDecoder;
import cn.laoshini.dk.net.codec.INettyMessageEncoder;
import cn.laoshini.dk.net.codec.ProtoBufNettyMessageDecoder;
import cn.laoshini.dk.net.codec.ProtoBufNettyMessageEncoder;
import cn.laoshini.dk.net.msg.BaseProtobufMessage;
import cn.laoshini.dk.server.AbstractNettyTcpGameServer;
import cn.laoshini.dk.server.channel.INettyChannelReader;
import cn.laoshini.dk.server.channel.ProtoBufMessageChannelReader;

/**
 * 使用ProtoBuf与客户端通信的TCP游戏服务器
 *
 * @author fagarine
 */
public class ProtoBufNettyTcpGameServer extends AbstractNettyTcpGameServer<BaseProtobufMessage.Base> {

    public ProtoBufNettyTcpGameServer(GameServerConfig gameConfig) {
        super(gameConfig, "ProtobufTcpServer");
    }

    @Override
    protected INettyMessageEncoder<BaseProtobufMessage.Base> getMessageEncoder() {
        return new ProtoBufNettyMessageEncoder();
    }

    @Override
    protected INettyMessageDecoder<BaseProtobufMessage.Base> getMessageDecoder() {
        return new ProtoBufNettyMessageDecoder(BaseProtobufMessage.Base.getDefaultInstance());
    }

    @Override
    public INettyChannelReader<BaseProtobufMessage.Base> getChannelReader() {
        return new ProtoBufMessageChannelReader();
    }

}
