package cn.laoshini.dk.net.codec;

import java.util.List;

import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.domain.msg.AbstractMessage;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.net.msg.BaseProtobufMessage;
import cn.laoshini.dk.util.ByteMessageUtil;

import static cn.laoshini.dk.constant.GameCodeEnum.MESSAGE_ENCODER_ERROR;
import static io.netty.buffer.Unpooled.wrappedBuffer;

/**
 * 默认使用{@link cn.laoshini.dk.net.msg.BaseProtobufMessage.Base}格式编码的Protobuf编码器，
 * 该类允许用户传入消息类型为{@link AbstractMessage}，且{@link AbstractMessage#data 消息详情}为{@link Message 用户protobuf中定义}类型的对象，
 * 本类会将这样的消息对象，转为{@link cn.laoshini.dk.net.msg.BaseProtobufMessage.Base}对象后再执行编码
 *
 * @author fagarine
 */
@ChannelHandler.Sharable
public class BaseProtobufMessageEncoder extends MessageToMessageEncoder<Object>
        implements INettyMessageEncoder<Object> {

    private static INettyMessageEncoder<Message> protobufEncoder = new ProtobufNettyMessageEncoder<>();

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        ByteBuf buf = encodeToByteBuf(msg);
        if (buf != null) {
            out.add(buf);
        }
    }

    @Override
    public ByteBuf encode(Object message, GameSubject subject) {
        return encodeToByteBuf(message);
    }

    private ByteBuf encodeToByteBuf(Object msg) {
        if (msg instanceof AbstractMessage) {
            AbstractMessage message = (AbstractMessage) msg;
            if (message.getData() instanceof Message) {
                return encodeDkMessageWithBase(message);
            }
        } else if (msg instanceof MessageOrBuilder) {
            if (msg instanceof Message) {
                return wrappedBuffer(((Message) msg).toByteArray());
            } else {
                return wrappedBuffer(((Message.Builder) msg).build().toByteArray());
            }
        }

        throw new MessageException(MESSAGE_ENCODER_ERROR.getCode(), "消息编码器错误");
    }

    private ByteBuf encodeDkMessageWithBase(AbstractMessage<Message> msg) {
        BaseProtobufMessage.Base base;
        String params = msg.getParams() == null ? "" : msg.getParams();
        if (msg.getData() != null) {
            base = ByteMessageUtil.buildBase(msg.getId(), msg.getCode(), params, msg.getData(), null);
        } else {
            base = ByteMessageUtil.buildErrorBase(msg.getId(), msg.getCode(), params);
        }
        return wrappedBuffer(base.toByteArray());
    }
}
