package cn.laoshini.dk.net.codec;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

import cn.laoshini.dk.domain.GameSubject;

import static io.netty.buffer.Unpooled.wrappedBuffer;

/**
 * Protobuf消息编码器
 *
 * @author fagarine
 */
public class ProtobufNettyMessageEncoder<M extends Message> extends ProtobufEncoder implements INettyMessageEncoder<M> {

    @Override
    public ByteBuf encode(M message, GameSubject subject) {
        return wrappedBuffer(message.toByteArray());
    }
}
