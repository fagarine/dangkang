package cn.laoshini.dk.server.impl;

import cn.laoshini.dk.domain.GameServerConfig;
import cn.laoshini.dk.net.codec.INettyMessageDecoder;
import cn.laoshini.dk.net.codec.INettyMessageEncoder;
import cn.laoshini.dk.net.codec.ProtobufNettyMessageDecoder;
import cn.laoshini.dk.net.codec.ProtobufNettyMessageEncoder;
import cn.laoshini.dk.net.msg.BaseProtobufMessage;
import cn.laoshini.dk.server.AbstractNettyTcpGameServer;
import cn.laoshini.dk.server.channel.INettyChannelReader;
import cn.laoshini.dk.server.channel.ProtobufMessageChannelReader;

/**
 * 使用Protobuf与客户端通信的TCP游戏服务器
 *
 * @author fagarine
 */
public class ProtobufNettyTcpGameServer extends AbstractNettyTcpGameServer<BaseProtobufMessage.Base> {

    public ProtobufNettyTcpGameServer(GameServerConfig gameConfig) {
        super(gameConfig, "ProtobufTcpServer");
    }

    @Override
    protected INettyMessageEncoder<BaseProtobufMessage.Base> getMessageEncoder() {
        return new ProtobufNettyMessageEncoder();
    }

    @Override
    protected INettyMessageDecoder<BaseProtobufMessage.Base> getMessageDecoder() {
        return new ProtobufNettyMessageDecoder(BaseProtobufMessage.Base.getDefaultInstance());
    }

    @Override
    public INettyChannelReader<BaseProtobufMessage.Base> getChannelReader() {
        return new ProtobufMessageChannelReader();
    }

}
