package cn.laoshini.dk.net.codec;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

import cn.laoshini.dk.domain.GameSubject;

/**
 * Protobuf消息编码器
 *
 * @author fagarine
 */
public class ProtobufNettyMessageEncoder<M extends Message> extends ProtobufEncoder implements INettyMessageEncoder<M> {

    @Override
    public ByteBuf encode(M message, GameSubject subject) {
        byte[] bytes = message.toByteArray();
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
        return buf;
    }
}
