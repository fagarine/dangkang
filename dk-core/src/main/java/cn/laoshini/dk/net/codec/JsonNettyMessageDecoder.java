package cn.laoshini.dk.net.codec;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.domain.msg.AbstractMessage;
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

    private byte lengthFlag = -1;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        AbstractMessage<?> message = decode(in, null);
        if (message != null) {
            out.add(message);
        }
    }

    @Override
    public AbstractMessage<?> decode(ByteBuf data, GameSubject subject) {
        // 标记读的位置
        data.markReaderIndex();

        // 消息长度
        int length = readLength(data);
        byte[] bytes = new byte[length];
        data.readBytes(bytes);
        List<AbstractMessage<?>> messages = MessageUtil.jsonBytesToMessage(bytes);
        if (CollectionUtil.isNotEmpty(messages)) {
            return messages.get(0);
        }
        return null;
    }

    private int readLength(ByteBuf buf) {
        // 识别消息头中用来记录消息长度的类型
        if (lengthFlag < 0) {
            // 标记读的位置
            buf.markReaderIndex();

            // 尝试以int格式读取消息长度
            int len = buf.readInt();
            if (buf.readableBytes() < len) {
                buf.resetReaderIndex();

                // 尝试以short格式读取消息长度
                len = buf.readShort();
                if (buf.readableBytes() < len) {
                    buf.resetReaderIndex();
                    // 消息数据没有在头部记录长度
                    lengthFlag = 0;

                    len = buf.readableBytes();
                } else {
                    lengthFlag = 2;
                }
            } else {
                lengthFlag = 4;
            }
            return len;
        }

        switch (lengthFlag) {
            case 4:
                return buf.readInt();

            case 2:
                return buf.readShort();

            default:
                return buf.readableBytes();
        }
    }

}
