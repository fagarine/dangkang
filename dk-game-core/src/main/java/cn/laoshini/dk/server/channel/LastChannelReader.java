package cn.laoshini.dk.server.channel;

import io.netty.channel.ChannelHandlerContext;

import cn.laoshini.dk.constant.AttributeKeyConstant;
import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.net.MessageHandlerHolder;
import cn.laoshini.dk.net.handler.MessageReceiveDispatcher;
import cn.laoshini.dk.net.msg.ReqMessage;
import cn.laoshini.dk.util.LogUtil;

/**
 * 真正负责消息读取和转发处理的类
 *
 * @author fagarine
 */
class LastChannelReader implements INettyChannelReader<ReqMessage> {

    @Override
    public void channelRead(ChannelHandlerContext ctx, ReqMessage msg) {
        // 协议到达后处理
        GameSubject gameSubject = ctx.channel().attr(AttributeKeyConstant.PLAYER).get();
        if (gameSubject == null) {
            if (!MessageHandlerHolder.allowGuestRequest(msg.getId())) {
                LogUtil.error("玩家未登录，msg:[{}], 关闭连接:{}", msg, ctx);
                ctx.close();
                return;
            }
        }
        MessageReceiveDispatcher.messageReceived(msg, gameSubject);
    }
}
