package cn.laoshini.dk.gm.handler;

import javax.annotation.Resource;

import cn.laoshini.dk.annotation.MessageHandle;
import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.domain.msg.ReqMessage;
import cn.laoshini.dk.domain.msg.RespMessage;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.gm.message.server.ReleaseGameServerReq;
import cn.laoshini.dk.gm.message.server.ReleaseGameServerRes;
import cn.laoshini.dk.net.handler.IHttpMessageHandler;
import cn.laoshini.dk.service.GmService;

/**
 * @author fagarine
 */
@MessageHandle(id = ReleaseGameServerReq.MESSAGE_ID)
public class ReleaseGameServerHandler implements IHttpMessageHandler<ReleaseGameServerReq> {

    @Resource
    private GmService gmService;

    @Override
    public void action(ReqMessage<ReleaseGameServerReq> reqMessage, GameSubject subject) throws MessageException {
        subject.getSession().sendMessage(call(reqMessage, subject));
    }

    @Override
    public RespMessage call(ReqMessage<ReleaseGameServerReq> reqMessage, GameSubject subject) throws MessageException {
        gmService.releaseAll();
        return buildRespMessage(ReleaseGameServerRes.MESSAGE_ID, new ReleaseGameServerRes());
    }
}
