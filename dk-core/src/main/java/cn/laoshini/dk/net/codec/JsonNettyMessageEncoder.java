package cn.laoshini.dk.net.codec;

import java.io.Serializable;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.serialization.ObjectEncoder;

import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.domain.msg.AbstractMessage;
import cn.laoshini.dk.util.MessageUtil;

/**
 * JSON格式消息编码器
 *
 * @author fagarine
 */
@ChannelHandler.Sharable
public class JsonNettyMessageEncoder extends ObjectEncoder implements INettyMessageEncoder<AbstractMessage<?>> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out) throws Exception {
        if (msg instanceof AbstractMessage) {
            byte[] bytes = MessageUtil.messageToJsonBytes((AbstractMessage<?>) msg);
            writeLength(out, bytes.length);
            out.writeBytes(bytes);
        } else {
            super.encode(ctx, msg, out);
        }
    }

    @Override
    public ByteBuf encode(AbstractMessage<?> message, GameSubject subject) {
        byte[] bytes = MessageUtil.messageToJsonBytes(message);
        ByteBuf buf = Unpooled.buffer();
        writeLength(buf, bytes.length);
        buf.writeBytes(bytes);
        return buf;
    }

    protected void writeLength(ByteBuf buf, int length) {
        buf.writeInt(length);
    }
}
