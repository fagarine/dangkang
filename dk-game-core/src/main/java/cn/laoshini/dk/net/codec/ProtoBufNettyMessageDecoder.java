package cn.laoshini.dk.net.codec;

import java.util.LinkedList;
import java.util.List;

import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.protobuf.ProtobufDecoder;

import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.net.msg.BaseProtobufMessage;

/**
 * ProtoBuf消息解码器
 *
 * @author fagarine
 */
public class ProtoBufNettyMessageDecoder extends ProtobufDecoder
        implements INettyMessageDecoder<BaseProtobufMessage.Base> {
    public ProtoBufNettyMessageDecoder(MessageLite prototype) {
        super(prototype);
    }

    @Override
    public BaseProtobufMessage.Base decode(ByteBuf data, GameSubject subject) {
        List<Object> messages = new LinkedList<>();
        try {
            decode(null, data, messages);
            if (!messages.isEmpty()) {
                return (BaseProtobufMessage.Base) messages.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
