package cn.laoshini.dk.net.codec;

import java.util.LinkedList;
import java.util.List;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.protobuf.ProtobufDecoder;

import cn.laoshini.dk.domain.GameSubject;

/**
 * Protobuf消息解码器
 *
 * @author fagarine
 */
public class ProtobufNettyMessageDecoder<M extends Message> extends ProtobufDecoder implements INettyMessageDecoder<M> {
    public ProtobufNettyMessageDecoder(MessageLite prototype) {
        super(prototype);
    }

    public ProtobufNettyMessageDecoder(MessageLite prototype, ExtensionRegistry extensionRegistry) {
        super(prototype, extensionRegistry);
    }

    @Override
    public M decode(ByteBuf data, GameSubject subject) {
        List<Object> messages = new LinkedList<>();
        try {
            decode(null, data, messages);
            if (!messages.isEmpty()) {
                return (M) messages.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
