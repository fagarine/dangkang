package cn.laoshini.dk.net.codec;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.serialization.ObjectEncoder;

import cn.laoshini.dk.domain.GameSubject;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * JSON格式消息编码器
 *
 * @author fagarine
 */
public class JsonNettyMessageEncoder extends ObjectEncoder implements INettyMessageEncoder<JSONObject> {

    @Override
    public ByteBuf encode(JSONObject message, GameSubject subject) {
        byte[] bytes = message.toJSONString().getBytes(UTF_8);
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
        return buf;
    }
}
