package cn.laoshini.dk.net.codec;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.Attribute;

import cn.laoshini.dk.constant.AttributeKeyConstant;
import cn.laoshini.dk.constant.GameConstant;
import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.net.MessageDtoClassHolder;
import cn.laoshini.dk.net.msg.INettyDto;
import cn.laoshini.dk.net.msg.ReqNettyCustomMessage;
import cn.laoshini.dk.net.session.NettySession;
import cn.laoshini.dk.util.LogUtil;

/**
 * 自定义消息类型解码器
 * <p>
 * 消息格式：长度 + 校验码 + 消息id ＋ 消息内容（字节数组，不会压缩）
 * </p>
 *
 * @author fagarine
 */
public class CustomNettyMessageDecoder extends ByteToMessageDecoder
        implements INettyMessageDecoder<ReqNettyCustomMessage<INettyDto>> {

    /**
     * 请求最大消息字节数
     */
    private static final int MAX_MSG_BYTES = 1024 * 1024;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 校验消息消息头长度字节是否足够
        if (in.readableBytes() < Integer.BYTES) {
            return;
        }

        // 标记读的位置
        in.markReaderIndex();

        // 消息长度
        int length = in.readInt();
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }

        // 校验码
        int key = in.readInt();
        // 消息类型id
        int msgId = in.readInt();

        Channel channel = ctx.channel();
        if (length > MAX_MSG_BYTES) {
            LogUtil.error("会话[{}]发送的消息[{}]超过最大字节数[{}]", channel, msgId, MAX_MSG_BYTES);
            channel.close();
            return;
        }

        if (in.readableBytes() > GameConstant.MAX_FRAME_LENGTH) {
            LogUtil.error("会话[{}]剩余缓冲超过最大字节数[{}]", channel, msgId, GameConstant.MAX_FRAME_LENGTH);
            channel.close();
            return;
        }

        /*
         * 校验码验证，解码校验码和发送消息的序号比对
         */
        int decodeOrder = key ^ 0x200 ^ length;

        Attribute<Integer> reqOrderAttr = channel.attr(AttributeKeyConstant.REQ_MSG_ORDER);
        // 请求消息的序号从0开始
        Integer reqOrder = reqOrderAttr.get();
        if (reqOrder == null) {
            reqOrder = 0;
        }

        // 创建消息对象
        ReqNettyCustomMessage<INettyDto> reqMessage = new ReqNettyCustomMessage<>();
        reqMessage.setId(msgId);

        if (decodeOrder == reqOrder) {
            // 获取消息内容的类型，并传入reqMessage中，这样才能保证后面读取消息不会失败
            Class<?> genericType = MessageDtoClassHolder.getDtoClass(msgId);
            if (genericType != null) {
                reqMessage.setDataType((Class<INettyDto>) genericType);
            }

            // 读取消息
            reqMessage.read(in);

            out.add(reqMessage);
            reqOrderAttr.set(++reqOrder);
        } else {
            LogUtil.error("[{}]发送消息序列出错, 发包序列[{}],当前序列:[{}]", channel, decodeOrder, reqOrder, msgId);
            channel.close();
            return;
        }

        LogUtil.c2sMessage("收到消息[{}]", reqMessage);
    }

    @Override
    public ReqNettyCustomMessage<INettyDto> decode(ByteBuf in, GameSubject subject) {
        // 校验消息消息头长度字节是否足够
        if (in.readableBytes() < Integer.BYTES) {
            return null;
        }

        // 标记读的位置
        in.markReaderIndex();

        // 消息长度
        int length = in.readInt();
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return null;
        }

        // 校验码
        int key = in.readInt();
        // 消息类型id
        int msgId = in.readInt();

        NettySession session = (NettySession) subject.getSession();
        if (length > MAX_MSG_BYTES) {
            LogUtil.error("会话[{}]发送的消息[{}]超过最大字节数[{}]", session, msgId, MAX_MSG_BYTES);
            session.close();
            return null;
        }

        if (in.readableBytes() > GameConstant.MAX_FRAME_LENGTH) {
            LogUtil.error("会话[{}]剩余缓冲超过最大字节数[{}]", session, msgId, GameConstant.MAX_FRAME_LENGTH);
            session.close();
            return null;
        }

        /*
         * 校验码验证，解码校验码和发送消息的序号比对
         */
        int decodeOrder = key ^ 0x200 ^ length;

        Attribute<Integer> reqOrderAttr = session.getChannel().attr(AttributeKeyConstant.REQ_MSG_ORDER);
        // 请求消息的序号从0开始
        Integer reqOrder = reqOrderAttr.get();
        if (reqOrder == null) {
            reqOrder = 0;
        }

        // 创建消息对象
        ReqNettyCustomMessage<INettyDto> reqMessage = new ReqNettyCustomMessage<>();
        reqMessage.setId(msgId);

        if (decodeOrder == reqOrder) {
            // 获取消息内容的类型，并传入reqMessage中，这样才能保证后面读取消息不会失败
            Class<?> genericType = MessageDtoClassHolder.getDtoClass(msgId);
            if (genericType != null) {
                reqMessage.setDataType((Class<INettyDto>) genericType);
            }

            // 读取消息
            reqMessage.read(in);
            reqOrderAttr.set(++reqOrder);
        } else {
            LogUtil.error("[{}]发送消息序列出错, 发包序列[{}],当前序列:[{}]", session, decodeOrder, reqOrder, msgId);
            session.close();
        }

        LogUtil.c2sMessage("收到消息[{}]", reqMessage);
        return reqMessage;
    }
}
