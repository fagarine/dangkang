package cn.laoshini.dk.server.impl;

import cn.laoshini.dk.domain.GameServerConfig;
import cn.laoshini.dk.domain.msg.AbstractMessage;
import cn.laoshini.dk.net.codec.INettyMessageDecoder;
import cn.laoshini.dk.net.codec.INettyMessageEncoder;
import cn.laoshini.dk.net.codec.JsonNettyMessageDecoder;
import cn.laoshini.dk.net.codec.JsonNettyMessageEncoder;
import cn.laoshini.dk.server.AbstractNettyTcpGameServer;
import cn.laoshini.dk.server.channel.INettyChannelReader;
import cn.laoshini.dk.server.channel.JsonMessageChannelReader;

/**
 * @author fagarine
 */
public class JsonNettyTcpGameServer<M extends AbstractMessage> extends AbstractNettyTcpGameServer<M> {

    public JsonNettyTcpGameServer(GameServerConfig gameConfig) {
        super(gameConfig, "JsonTcpServer");
    }

    @Override
    protected INettyMessageEncoder<M> getMessageEncoder() {
        return (INettyMessageEncoder<M>) new JsonNettyMessageEncoder();
    }

    @Override
    protected INettyMessageDecoder<M> getMessageDecoder() {
        return (INettyMessageDecoder<M>) new JsonNettyMessageDecoder();
    }

    @Override
    public INettyChannelReader<M> getChannelReader() {
        return (INettyChannelReader<M>) new JsonMessageChannelReader();
    }

}
