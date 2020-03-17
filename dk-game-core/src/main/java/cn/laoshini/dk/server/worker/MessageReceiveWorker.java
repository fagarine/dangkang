package cn.laoshini.dk.server.worker;

import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.domain.msg.ReqMessage;
import cn.laoshini.dk.executor.AbstractOrderedWorker;
import cn.laoshini.dk.net.handler.MessageReceiveDispatcher;

/**
 * @author fagarine
 */
public class MessageReceiveWorker extends AbstractOrderedWorker {

    private ReqMessage<Object> reqMessage;

    private GameSubject gameSubject;

    public MessageReceiveWorker(ReqMessage<Object> reqMessage, GameSubject gameSubject) {
        this.reqMessage = reqMessage;
        this.gameSubject = gameSubject;
    }

    @Override
    protected void action() {
        MessageReceiveDispatcher.dealMessage(reqMessage, gameSubject);
    }
}
