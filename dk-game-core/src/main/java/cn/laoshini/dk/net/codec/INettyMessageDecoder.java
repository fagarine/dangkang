package cn.laoshini.dk.net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * 使用netty通信的消息到达后的解析器
 *
 * @param <OutMessageType> 解析后返回的消息类型
 * @author fagarine
 */
public interface INettyMessageDecoder<OutMessageType> extends ChannelHandler, IMessageDecoder<ByteBuf, OutMessageType> {

    @Override
    default void handlerAdded(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    default void handlerRemoved(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    default void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 消息解析实现这些不需要方法，在接口中做默认实现，避免影响子类
    }
}
