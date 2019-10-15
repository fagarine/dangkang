package cn.laoshini.dk.server.channel;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;

import cn.laoshini.dk.net.msg.ReqMessage;

/**
 * JSON格式消息到达读取处理
 *
 * @author fagarine
 */
public class JsonMessageChannelReader implements INettyChannelReader<JSONObject> {

    private LastChannelReader delegate = new LastChannelReader();

    @Override
    public void channelRead(ChannelHandlerContext ctx, JSONObject msg) {
        // 将JSON对象转为ReqMessage对象
        ReqMessage<Object> reqMessage = new ReqMessage<>();
        reqMessage.setId(msg.getInteger("id"));
        reqMessage.setParams(msg.getString("params"));
        reqMessage.setData(msg.get("detail"));
        delegate.channelRead(ctx, reqMessage);
    }
}
