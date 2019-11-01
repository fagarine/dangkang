package cn.laoshini.dk.server.channel;

import io.netty.channel.ChannelHandlerContext;

import cn.laoshini.dk.net.msg.ReqMessage;

/**
 * JSON格式消息到达读取处理
 *
 * @author fagarine
 */
public class JsonMessageChannelReader implements INettyChannelReader<ReqMessage<?>> {

    private LastChannelReader delegate = new LastChannelReader();

    @Override
    public void channelRead(ChannelHandlerContext ctx, ReqMessage<?> msg) {
        delegate.channelRead(ctx, msg);
    }
}
