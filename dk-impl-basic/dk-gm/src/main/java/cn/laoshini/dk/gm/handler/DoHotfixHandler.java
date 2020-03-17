package cn.laoshini.dk.gm.handler;

import javax.annotation.Resource;

import cn.laoshini.dk.annotation.MessageHandle;
import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.domain.msg.ReqMessage;
import cn.laoshini.dk.domain.msg.RespMessage;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.gm.message.hotfix.DoHotfixReq;
import cn.laoshini.dk.gm.message.hotfix.DoHotfixRes;
import cn.laoshini.dk.net.handler.IHttpMessageHandler;
import cn.laoshini.dk.service.GmService;

/**
 * @author fagarine
 */
@MessageHandle(id = DoHotfixReq.MESSAGE_ID)
public class DoHotfixHandler implements IHttpMessageHandler<DoHotfixReq> {

    @Resource
    private GmService gmService;

    @Override
    public void action(ReqMessage<DoHotfixReq> reqMessage, GameSubject subject) throws MessageException {
        subject.getSession().sendMessage(call(reqMessage, subject));
    }

    @Override
    public RespMessage call(ReqMessage<DoHotfixReq> reqMessage, GameSubject subject) throws MessageException {
        DoHotfixRes res = new DoHotfixRes();
        res.setMessage(gmService.doHotfix(reqMessage.getData().getHotfixKey()));
        return buildRespMessage(DoHotfixRes.MESSAGE_ID, res);
    }
}
