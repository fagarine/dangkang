package cn.laoshini.dk.server.impl;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.MessageLite;

import cn.laoshini.dk.domain.GameServerConfig;
import cn.laoshini.dk.net.codec.BaseProtobufMessageEncoder;
import cn.laoshini.dk.net.codec.INettyMessageDecoder;
import cn.laoshini.dk.net.codec.INettyMessageEncoder;
import cn.laoshini.dk.net.codec.ProtobufNettyMessageDecoder;
import cn.laoshini.dk.net.msg.BaseProtobufMessage;
import cn.laoshini.dk.server.AbstractNettyTcpGameServer;
import cn.laoshini.dk.server.channel.INettyChannelReader;
import cn.laoshini.dk.server.channel.ProtobufMessageChannelReader;

/**
 * 使用Protobuf与客户端通信的TCP游戏服务器
 *
 * @author fagarine
 */
public class ProtobufNettyTcpGameServer extends AbstractNettyTcpGameServer {

    public ProtobufNettyTcpGameServer(GameServerConfig gameConfig) {
        super(gameConfig, "ProtobufTcpServer");
    }

    @Override
    protected INettyMessageEncoder getMessageEncoder() {
        return new BaseProtobufMessageEncoder();
    }

    @Override
    protected INettyMessageDecoder getMessageDecoder() {
        return new ProtobufNettyMessageDecoder(BaseProtobufMessage.Base.getDefaultInstance());
    }

    @Override
    public INettyChannelReader<BaseProtobufMessage.Base> getChannelReader() {
        return new ProtobufMessageChannelReader();
    }

    /**
     * 设置Protobuf的原型默认实例（例如：{@link BaseProtobufMessage.Base#getDefaultInstance()}），proto3版本使用
     *
     * @param prototype 原型对象
     * @return 返回当前对象
     */
    public ProtobufNettyTcpGameServer setPrototype(MessageLite prototype) {
        setDecoder(new ProtobufNettyMessageDecoder(prototype));
        return this;
    }

    /**
     * 设置Protobuf的原型默认实例（例如：{@link BaseProtobufMessage.Base#getDefaultInstance()}）和类型扩展信息，proto2版本使用
     *
     * @param prototype 原型对象
     * @param extensionRegistry 扩展消息注册器
     * @return 返回当前对象
     */
    public ProtobufNettyTcpGameServer setPrototype(MessageLite prototype, ExtensionRegistry extensionRegistry) {
        setDecoder(new ProtobufNettyMessageDecoder(prototype, extensionRegistry));
        return this;
    }
}
