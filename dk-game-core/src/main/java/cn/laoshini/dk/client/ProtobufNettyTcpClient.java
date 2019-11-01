package cn.laoshini.dk.client;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;

import cn.laoshini.dk.net.codec.ProtobufNettyMessageDecoder;
import cn.laoshini.dk.net.codec.ProtobufNettyMessageEncoder;
import cn.laoshini.dk.net.msg.BaseProtobufMessage;

/**
 * 使用netty + protobuf通信的TCP游戏客户端
 *
 * @author fagarine
 */
public class ProtobufNettyTcpClient<S, M extends Message> extends AbstractBiasCodecNettyTcpClient<S, M> {

    @Override
    protected void checkDepends() {
        super.checkDepends();

        /**
         * 类似{@link BaseProtobufMessage.Base#getCmd()}
         */
        if (idReader() == null) {
            throw new ClientException("消息id读取器不能为空");
        }
    }

    /**
     * 传入Protobuf原型类型，设置消息编解码器
     *
     * @param prototype 原型类型，不允许为空，如：{@link BaseProtobufMessage.Base#getDefaultInstance()}
     * @param registry 扩展消息注册器，Protobuf3以后的版本不需要，可以传入null
     */
    public void setCodecByPrototype(MessageLite prototype, ExtensionRegistry registry) {
        if (messageEncoder() == null) {
            setMessageEncoder(new ProtobufNettyMessageEncoder());
        }

        if (messageDecoder() == null) {
            if (registry == null) {
                setMessageDecoder(new ProtobufNettyMessageDecoder(prototype));
            } else {
                setMessageDecoder(new ProtobufNettyMessageDecoder(prototype, registry));
            }
        }
    }
}
