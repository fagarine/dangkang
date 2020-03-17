package cn.laoshini.dk.gm.handler;

import javax.annotation.Resource;

import cn.laoshini.dk.annotation.MessageHandle;
import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.domain.dto.HotfixRecordDTO;
import cn.laoshini.dk.domain.msg.ReqMessage;
import cn.laoshini.dk.domain.msg.RespMessage;
import cn.laoshini.dk.domain.query.Page;
import cn.laoshini.dk.domain.query.PageQueryCondition;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.gm.message.hotfix.GetHotfixHistoryReq;
import cn.laoshini.dk.gm.message.hotfix.GetHotfixHistoryRes;
import cn.laoshini.dk.net.handler.IHttpMessageHandler;
import cn.laoshini.dk.service.GmService;

/**
 * @author fagarine
 */
@MessageHandle(id = GetHotfixHistoryReq.MESSAGE_ID)
public class GetHotfixHistoryHandler implements IHttpMessageHandler<GetHotfixHistoryReq> {

    @Resource
    private GmService gmService;

    @Override
    public void action(ReqMessage<GetHotfixHistoryReq> reqMessage, GameSubject subject) throws MessageException {
        subject.getSession().sendMessage(call(reqMessage, subject));
    }

    @Override
    public RespMessage call(ReqMessage<GetHotfixHistoryReq> reqMessage, GameSubject subject) throws MessageException {
        PageQueryCondition condition = new PageQueryCondition();
        condition.setPageNo(reqMessage.getData().getPageNo());
        condition.setPageSize(reqMessage.getData().getPageSize());
        Page<HotfixRecordDTO> page = gmService.getHotfixHistory(condition);
        GetHotfixHistoryRes res = new GetHotfixHistoryRes();
        res.setPageNo(page.getPageNo());
        res.setPageSize(page.getPageSize());
        res.setTotal(page.getTotal());
        res.setRecords(page.getResult());
        return buildRespMessage(GetHotfixHistoryRes.MESSAGE_ID, res);
    }
}
