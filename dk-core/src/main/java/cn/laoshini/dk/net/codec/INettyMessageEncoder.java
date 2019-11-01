package cn.laoshini.dk.net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * 使用netty通信的消息发送前的编码
 *
 * @param <InMessageType> 编码前的消息类型
 * @author fagarine
 */
public interface INettyMessageEncoder<InMessageType> extends ChannelHandler, IMessageEncoder<ByteBuf, InMessageType> {

    @Override
    default void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // 消息编码实现这些不需要方法，在接口中做默认实现，避免影响子类
    }

    @Override
    default void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // 消息编码实现这些不需要方法，在接口中做默认实现，避免影响子类
    }

    @Override
    default void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 消息编码实现这些不需要方法，在接口中做默认实现，避免影响子类
    }
}
