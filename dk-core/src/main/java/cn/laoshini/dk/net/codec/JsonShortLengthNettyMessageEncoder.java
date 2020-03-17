package cn.laoshini.dk.net.codec;

import io.netty.buffer.ByteBuf;

/**
 * @author fagarine
 */
public class JsonShortLengthNettyMessageEncoder extends JsonNettyMessageEncoder {
    @Override
    protected void writeLength(ByteBuf buf, int length) {
        buf.writeShort(length);
    }
}
