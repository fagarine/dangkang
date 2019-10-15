package cn.laoshini.dk.server.impl;

import com.alibaba.fastjson.JSONObject;

import cn.laoshini.dk.domain.GameServerConfig;
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
public class JsonNettyTcpGameServer extends AbstractNettyTcpGameServer<JSONObject> {

    public JsonNettyTcpGameServer(GameServerConfig gameConfig) {
        super(gameConfig, "JsonTcpServer");
    }

    @Override
    protected INettyMessageEncoder<JSONObject> getMessageEncoder() {
        return new JsonNettyMessageEncoder();
    }

    @Override
    protected INettyMessageDecoder<JSONObject> getMessageDecoder() {
        return new JsonNettyMessageDecoder();
    }

    @Override
    public INettyChannelReader<JSONObject> getChannelReader() {
        return new JsonMessageChannelReader();
    }

}
