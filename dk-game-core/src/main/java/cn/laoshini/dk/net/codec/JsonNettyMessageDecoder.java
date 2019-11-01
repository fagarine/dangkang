package cn.laoshini.dk.net.codec;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.net.msg.AbstractMessage;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.MessageUtil;

/**
 * JSON格式消息解码器
 *
 * @author fagarine
 */
@ChannelHandler.Sharable
public class JsonNettyMessageDecoder extends MessageToMessageDecoder<ByteBuf>
        implements INettyMessageDecoder<AbstractMessage<?>> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        AbstractMessage<?> message = decode(in, null);
        if (message != null) {
            out.add(message);
        }
    }

    @Override
    public AbstractMessage<?> decode(ByteBuf data, GameSubject subject) {
        byte[] bytes = new byte[data.readableBytes()];
        data.readBytes(bytes);
        List<AbstractMessage<?>> messages = MessageUtil.jsonBytesToMessage(bytes);
        if (CollectionUtil.isNotEmpty(messages)) {
            return messages.get(0);
        }
        return null;
    }

}
