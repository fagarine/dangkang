package cn.laoshini.dk.gm.handler;

import javax.annotation.Resource;

import cn.laoshini.dk.annotation.MessageHandle;
import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.domain.msg.ReqMessage;
import cn.laoshini.dk.domain.msg.RespMessage;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.gm.message.module.ReloadModulesReq;
import cn.laoshini.dk.gm.message.module.ReloadModulesRes;
import cn.laoshini.dk.net.handler.IHttpMessageHandler;
import cn.laoshini.dk.service.GmService;

/**
 * @author fagarine
 */
@MessageHandle(id = ReloadModulesReq.MESSAGE_ID)
public class ReloadModulesHandler implements IHttpMessageHandler<ReloadModulesReq> {

    @Resource
    private GmService gmService;

    @Override
    public void action(ReqMessage<ReloadModulesReq> reqMessage, GameSubject subject) throws MessageException {
        subject.getSession().sendMessage(call(reqMessage, subject));
    }

    @Override
    public RespMessage call(ReqMessage<ReloadModulesReq> reqMessage, GameSubject subject) throws MessageException {
        gmService.reloadModules();
        return buildRespMessage(ReloadModulesRes.MESSAGE_ID, new ReloadModulesRes());
    }
}
