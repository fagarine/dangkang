package cn.laoshini.dk.gm.handler;

import javax.annotation.Resource;

import cn.laoshini.dk.annotation.MessageHandle;
import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.domain.msg.ReqMessage;
import cn.laoshini.dk.domain.msg.RespMessage;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.gm.message.module.RemoveModuleReq;
import cn.laoshini.dk.gm.message.module.RemoveModuleRes;
import cn.laoshini.dk.net.handler.IHttpMessageHandler;
import cn.laoshini.dk.service.GmService;

/**
 * @author fagarine
 */
@MessageHandle(id = RemoveModuleReq.MESSAGE_ID)
public class RemoveModuleHandler implements IHttpMessageHandler<RemoveModuleReq> {

    @Resource
    private GmService gmService;

    @Override
    public void action(ReqMessage<RemoveModuleReq> reqMessage, GameSubject subject) throws MessageException {
        subject.getSession().sendMessage(call(reqMessage, subject));
    }

    @Override
    public RespMessage call(ReqMessage<RemoveModuleReq> reqMessage, GameSubject subject) throws MessageException {
        gmService.removeModule(reqMessage.getData().getModuleName());
        return buildRespMessage(RemoveModuleRes.MESSAGE_ID, new RemoveModuleRes());
    }
}
