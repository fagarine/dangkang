package cn.laoshini.dk.gm.handler;

import javax.annotation.Resource;

import cn.laoshini.dk.annotation.MessageHandle;
import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.domain.msg.ReqMessage;
import cn.laoshini.dk.domain.msg.RespMessage;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.gm.message.server.GetGameServerInfoReq;
import cn.laoshini.dk.gm.message.server.GetGameServerInfoRes;
import cn.laoshini.dk.net.handler.IHttpMessageHandler;
import cn.laoshini.dk.service.GmService;

/**
 * @author fagarine
 */
@MessageHandle(id = GetGameServerInfoReq.MESSAGE_ID)
public class GetGameServerInfoHandler implements IHttpMessageHandler<GetGameServerInfoReq> {

    @Resource
    private GmService gmService;

    @Override
    public void action(ReqMessage<GetGameServerInfoReq> reqMessage, GameSubject subject) throws MessageException {
        subject.getSession().sendMessage(call(reqMessage, subject));
    }

    @Override
    public RespMessage call(ReqMessage<GetGameServerInfoReq> reqMessage, GameSubject subject) throws MessageException {
        GetGameServerInfoRes res = new GetGameServerInfoRes();
        res.setServers(gmService.getAllServerInfo());
        return buildRespMessage(GetGameServerInfoRes.MESSAGE_ID, res);
    }
}
