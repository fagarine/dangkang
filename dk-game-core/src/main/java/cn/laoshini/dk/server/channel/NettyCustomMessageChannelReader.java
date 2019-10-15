package cn.laoshini.dk.server.channel;

import io.netty.channel.ChannelHandlerContext;

import cn.laoshini.dk.net.msg.INettyDto;
import cn.laoshini.dk.net.msg.ReqNettyCustomMessage;

/**
 * 使用Netty通信的自定义消息类型读取处理
 *
 * @author fagarine
 */
public class NettyCustomMessageChannelReader implements INettyChannelReader<ReqNettyCustomMessage<INettyDto>> {

    private LastChannelReader delegate = new LastChannelReader();

    @Override
    public void channelRead(ChannelHandlerContext ctx, ReqNettyCustomMessage<INettyDto> reqMsg) {
        delegate.channelRead(ctx, reqMsg);
    }
}
