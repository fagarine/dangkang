package cn.laoshini.dk.net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.net.msg.BaseProtobufMessage;

/**
 * ProtoBuf消息编码器
 *
 * @author fagarine
 */
public class ProtoBufNettyMessageEncoder extends ProtobufEncoder
        implements INettyMessageEncoder<BaseProtobufMessage.Base> {

    @Override
    public ByteBuf encode(BaseProtobufMessage.Base message, GameSubject subject) {
        byte[] bytes = message.toByteArray();
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
        return buf;
    }
}
