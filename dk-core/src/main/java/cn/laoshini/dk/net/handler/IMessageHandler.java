package cn.laoshini.dk.net.handler;

import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.domain.msg.ReqMessage;
import cn.laoshini.dk.domain.msg.RespMessage;
import cn.laoshini.dk.exception.MessageException;

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
     * 将传入数据作为{@link RespMessage}消息的内容，拼装成一个{@link RespMessage}对象并返回
     *
     * @param messageId 消息id
     * @param returnCode 消息返回码
     * @param data 消息内容
     * @param params 扩展内容
     * @return 返回RespMessage对象
     */
    default <T> RespMessage<T> buildRespMessage(int messageId, int returnCode, T data, String params) {
        RespMessage<T> rs = new RespMessage<>();
        rs.setId(messageId);
        rs.setCode(returnCode);
        rs.setParams(params);
        rs.setData(data);
        return rs;
    }

    /**
     * 将传入数据作为{@link RespMessage}消息的内容，拼装成一个{@link RespMessage}对象并返回
     *
     * @param messageId 消息id
     * @param data 消息内容
     * @return 返回RespMessage对象
     */
    default <T> RespMessage<T> buildRespMessage(int messageId, T data) {
        RespMessage<T> rs = new RespMessage<>();
        rs.setId(messageId);
        rs.setData(data);
        return rs;
    }

    /**
     * 将传入数据拼装成一个返回错误提示的{@link RespMessage}消息对象并返回
     *
     * @param messageId 消息id
     * @param errorCode 错误码
     * @param errorMessage 提示信息
     * @return 返回RespMessage对象
     */
    default <T> RespMessage<T> buildErrorRespMessage(int messageId, int errorCode, String errorMessage) {
        RespMessage<T> rs = new RespMessage<>();
        rs.setId(messageId);
        rs.setCode(errorCode);
        rs.setParams(errorMessage);
        return rs;
    }
}
