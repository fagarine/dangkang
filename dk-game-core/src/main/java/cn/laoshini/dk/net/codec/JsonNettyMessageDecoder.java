package cn.laoshini.dk.net.codec;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.json.JsonObjectDecoder;

import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.util.LogUtil;

/**
 * JSON格式消息解码器
 *
 * @author fagarine
 */
public class JsonNettyMessageDecoder extends JsonObjectDecoder implements INettyMessageDecoder<JSONObject> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        super.decode(ctx, in, out);

        LogUtil.info(String.valueOf(out));
    }

    @Override
    public JSONObject decode(ByteBuf data, GameSubject subject) {
        byte[] bytes = new byte[data.readableBytes()];
        data.readBytes(bytes);
        return JSON.parseObject(bytes, JSONObject.class);
    }
}
