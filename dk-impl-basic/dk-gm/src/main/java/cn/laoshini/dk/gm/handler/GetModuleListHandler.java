package cn.laoshini.dk.gm.handler;

import javax.annotation.Resource;

import cn.laoshini.dk.annotation.MessageHandle;
import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.domain.msg.ReqMessage;
import cn.laoshini.dk.domain.msg.RespMessage;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.gm.message.module.GetModuleListReq;
import cn.laoshini.dk.gm.message.module.GetModuleListRes;
import cn.laoshini.dk.net.handler.IHttpMessageHandler;
import cn.laoshini.dk.service.GmService;

/**
 * @author fagarine
 */
@MessageHandle(id = GetModuleListReq.MESSAGE_ID)
public class GetModuleListHandler implements IHttpMessageHandler<GetModuleListReq> {

    @Resource
    private GmService gmService;

    @Override
    public void action(ReqMessage<GetModuleListReq> reqMessage, GameSubject subject) throws MessageException {
        subject.getSession().sendMessage(call(reqMessage, subject));
    }

    @Override
    public RespMessage call(ReqMessage<GetModuleListReq> reqMessage, GameSubject subject) throws MessageException {
        GetModuleListRes res = new GetModuleListRes();
        res.setModules(gmService.getModuleList());
        return buildRespMessage(GetModuleListRes.MESSAGE_ID, res);
    }
}
