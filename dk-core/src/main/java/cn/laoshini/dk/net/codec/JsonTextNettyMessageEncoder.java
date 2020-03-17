package cn.laoshini.dk.net.codec;

import io.netty.buffer.ByteBuf;

/**
 * @author fagarine
 */
public class JsonTextNettyMessageEncoder extends JsonNettyMessageEncoder {

    @Override
    protected void writeLength(ByteBuf buf, int length) {
        // 不记录长度信息
    }
}
