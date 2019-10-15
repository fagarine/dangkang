package cn.laoshini.dk.net.handler;

import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.net.msg.ReqMessage;

/**
 * 短连接消息处理接口
 *
 * @author fagarine
 */
public interface IHttpMessageHandler<DataType> extends IMessageHandler<DataType> {

    @Override
    default void action(ReqMessage<DataType> reqMessage, GameSubject subject) throws MessageException {
        // HTTP之类的短连接不需要实现该方法
    }
}
