package cn.laoshini.dk.client;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;

import cn.laoshini.dk.net.codec.ProtobufNettyMessageDecoder;
import cn.laoshini.dk.net.codec.ProtobufNettyMessageEncoder;
import cn.laoshini.dk.net.msg.BaseProtobufMessage;
import cn.laoshini.dk.util.LogUtil;

/**
 * 使用netty + protobuf通信的TCP游戏客户端
 *
 * @author fagarine
 */
public class ProtobufNettyTcpClient<S, M extends Message> extends AbstractBiasCodecNettyTcpClient<S, M> {

    @Override
    protected void checkDepends() {
        // 如果用户没有设置编解码器，则使用系统自带的BaseProtobufMessage.Base
        if (messageEncoder() == null) {
            setCodecByPrototype(BaseProtobufMessage.Base.getDefaultInstance(), null);
            setIdReader(msg -> {
                BaseProtobufMessage.Base base = (BaseProtobufMessage.Base) msg;
                return base.getMessageId();
            });
            LogUtil.info("用户未设置自定义的Protobuf编解码器，系统认为用户的消息格式遵循BaseProtobufMessage.Base类格式");
        }

        super.checkDepends();
    }

    /**
     * 传入Protobuf原型类型，设置消息编解码器
     *
     * @param prototype 原型类型，不允许为空，如：{@link BaseProtobufMessage.Base#getDefaultInstance()}
     * @param registry 扩展消息注册器，Protobuf3以后的版本不需要，可以传入null
     */
    public ProtobufNettyTcpClient<S, M> setCodecByPrototype(MessageLite prototype, ExtensionRegistry registry) {
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
        return this;
    }
}
