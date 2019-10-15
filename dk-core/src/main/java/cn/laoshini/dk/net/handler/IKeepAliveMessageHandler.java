package cn.laoshini.dk.net.handler;

import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.net.msg.ReqMessage;
import cn.laoshini.dk.net.msg.RespMessage;

/**
 * 长连接消息处理接口
 *
 * @author fagarine
 */
public interface IKeepAliveMessageHandler<DataType> extends IMessageHandler<DataType> {

    @Override
    default RespMessage call(ReqMessage<DataType> reqMessage, GameSubject subject) throws MessageException {
        // 长连接不需要实现该方法
        return null;
    }
}
