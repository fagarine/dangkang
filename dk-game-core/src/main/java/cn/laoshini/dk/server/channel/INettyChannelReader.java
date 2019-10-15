package cn.laoshini.dk.server.channel;

import io.netty.channel.ChannelHandlerContext;

/**
 * Channel会话读取功能抽象类
 *
 * @param <MessageType> 进入时的消息类型
 * @author fagarine
 */
public interface INettyChannelReader<MessageType> {

    /**
     * 消息读取和处理
     *
     * @param ctx netty连接的上下文对象
     * @param msg 消息体
     */
    public abstract void channelRead(ChannelHandlerContext ctx, MessageType msg);
}
