package cn.laoshini.dk.server.impl;

import cn.laoshini.dk.constant.GameServerProtocolEnum;
import cn.laoshini.dk.domain.GameServerConfig;
import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.net.handler.MessageReceiveDispatcher;
import cn.laoshini.dk.net.msg.ReqMessage;
import cn.laoshini.dk.net.msg.RespMessage;
import cn.laoshini.dk.server.AbstractGameServer;
import cn.laoshini.dk.util.LogUtil;

/**
 * HTTP游戏服交互入口
 *
 * @author fagarine
 */
public class HttpGameServer extends AbstractGameServer {

    public HttpGameServer(GameServerConfig gameConfig) {
        super(gameConfig, "HttpGameServer");
    }

    /**
     * 消息处理与返回
     *
     * @param message 进入消息
     * @param subject 消息所属主体对象
     * @return 返回处理后的消息
     */
    public RespMessage dealMessage(ReqMessage message, GameSubject subject) {
        LogUtil.c2sMessage("HTTP服务器收到消息:[{}], subject:[{}]", message, subject);
        return MessageReceiveDispatcher.dealMessageAndBack(message, subject);
    }

    @Override
    protected void shutdown0() {

    }

    @Override
    public GameServerProtocolEnum getProtocolType() {
        return GameServerProtocolEnum.HTTP;
    }
}
