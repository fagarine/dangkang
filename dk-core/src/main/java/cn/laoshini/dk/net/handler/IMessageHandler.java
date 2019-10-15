package cn.laoshini.dk.net.handler;

import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.net.msg.ReqMessage;
import cn.laoshini.dk.net.msg.RespMessage;

/**
 * 消息处理逻辑类的接口定义
 *
 * @author fagarine
 */
public interface IMessageHandler<DataType> {

    /**
     * 执行业务逻辑，处理结果直接在消息体内发送，不需要返回（用于TCP、UDP这类长连接的请求）
     *
     * @param reqMessage 进入消息
     * @param subject 消息所属主体对象
     * @throws MessageException 如果执行中遇到可预测的错误，抛出异常，由上层统一捕获处理
     */
    void action(ReqMessage<DataType> reqMessage, GameSubject subject) throws MessageException;

    /**
     * 执行业务逻辑，并返回执行结果（用于需要等待返回结果的请求，比如HTTP请求，由于不能直接由服务端向客户端推送消息，处理完直接返回）
     *
     * @param reqMessage 进入消息
     * @param subject 消息所属主体对象
     * @return 返回执行结果消息，该消息用于返回客户端
     * @throws MessageException 如果执行中遇到可预测的错误，抛出异常，由上层统一捕获处理
     */
    RespMessage call(ReqMessage<DataType> reqMessage, GameSubject subject) throws MessageException;
}
