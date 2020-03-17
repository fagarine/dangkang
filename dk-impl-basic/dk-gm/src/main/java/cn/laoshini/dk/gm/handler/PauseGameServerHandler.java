package cn.laoshini.dk.gm.handler;

import javax.annotation.Resource;

import cn.laoshini.dk.annotation.MessageHandle;
import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.domain.msg.ReqMessage;
import cn.laoshini.dk.domain.msg.RespMessage;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.gm.message.server.PauseGameServerReq;
import cn.laoshini.dk.gm.message.server.PauseGameServerRes;
import cn.laoshini.dk.net.handler.IHttpMessageHandler;
import cn.laoshini.dk.service.GmService;

/**
 * @author fagarine
 */
@MessageHandle(id = PauseGameServerReq.MESSAGE_ID)
public class PauseGameServerHandler implements IHttpMessageHandler<PauseGameServerReq> {

    @Resource
    private GmService gmService;

    @Override
    public void action(ReqMessage<PauseGameServerReq> reqMessage, GameSubject subject) throws MessageException {
        subject.getSession().sendMessage(call(reqMessage, subject));
    }

    @Override
    public RespMessage call(ReqMessage<PauseGameServerReq> reqMessage, GameSubject subject) throws MessageException {
        PauseGameServerReq req = reqMessage.getData();
        gmService.pauseAll(req.getTips(), req.getOpenTime());
        return buildRespMessage(PauseGameServerRes.MESSAGE_ID, new PauseGameServerRes());
    }
}
