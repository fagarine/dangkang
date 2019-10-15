package cn.laoshini.dk.server.channel;

import io.netty.channel.ChannelHandlerContext;

import cn.laoshini.dk.net.msg.BaseProtobufMessage;
import cn.laoshini.dk.util.ByteMessageUtil;

/**
 * Protobuf消息到达读取处理
 *
 * @author fagarine
 */
public class ProtoBufMessageChannelReader implements INettyChannelReader<BaseProtobufMessage.Base> {

    private LastChannelReader delegate = new LastChannelReader();

    @Override
    public void channelRead(ChannelHandlerContext ctx, BaseProtobufMessage.Base msg) {
        // 将protobuf对象转为ReqMessage对象
        delegate.channelRead(ctx, ByteMessageUtil.baseToReqMessage(msg));
    }

}
