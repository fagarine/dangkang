package cn.laoshini.dk.net.handler;

import cn.laoshini.dk.constant.GameCodeEnum;
import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.function.VariousWaysManager;
import cn.laoshini.dk.net.IMessageHandlerManager;
import cn.laoshini.dk.net.msg.ReqMessage;
import cn.laoshini.dk.net.msg.RespMessage;
import cn.laoshini.dk.util.LogUtil;

/**
 * 消息到达处理分发和处理
 *
 * @author fagarine
 */
public class MessageReceiveDispatcher {
    private MessageReceiveDispatcher() {
    }

    private static IMessageHandlerManager messageHandlerManager = VariousWaysManager
            .getCurrentImpl(IMessageHandlerManager.class);

    /**
     * 消息处理
     *
     * @param reqMessage 进入消息
     * @param gameSubject 消息所属主体
     */
    public static void dealMessage(ReqMessage reqMessage, GameSubject gameSubject) {
        try {
            messageHandlerManager.doMessageHandlerAction(reqMessage, gameSubject);
        } catch (MessageException e) {
            LogUtil.error(e.getMessage(), e);
        } catch (Throwable t) {
            LogUtil.error(String.format("消息处理出现未知异常, message:%s", reqMessage), t);
        }
    }

    /**
     * 消息处理，该方法会保证消息处理按消息的到达先后顺序执行
     *
     * @param reqMessage 进入消息
     * @param gameSubject 消息所属主体
     */
    public static void dealMessageSequential(ReqMessage reqMessage, GameSubject gameSubject) {
        try {
            messageHandlerManager.doMessageHandlerAction(reqMessage, gameSubject);
        } catch (MessageException e) {
            LogUtil.error(e.getMessage(), e);
        } catch (Throwable t) {
            LogUtil.error(String.format("消息处理出现未知异常, message:%s", reqMessage), t);
        }
    }

    /**
     * 等待消息处理，并返回处理结果
     * 注意：
     * <p>
     * 通过该方法传入的消息，将会由当前线程执行逻辑，并且无法保证一定按消息到达先后执行
     * </p>
     *
     * @param reqMessage 进入消息
     * @param gameSubject 消息所属主体
     * @return 返回客户端的消息
     */
    public static RespMessage dealMessageAndBack(ReqMessage reqMessage, GameSubject gameSubject) {
        RespMessage resp = new RespMessage();
        try {
            return messageHandlerManager.doMessageHandlerCall(reqMessage, gameSubject);
        } catch (MessageException e) {
            LogUtil.error(e.getMessage(), e);
            resp.setCode(e.getErrorCode().getCode());
            resp.setParams(e.getErrorCode().getDesc());
        } catch (Throwable t) {
            LogUtil.error(String.format("消息处理出现未知异常, message:%s", reqMessage), t);
            resp.setCode(GameCodeEnum.UNKNOWN_ERROR.getCode());
            resp.setParams(GameCodeEnum.UNKNOWN_ERROR.getDesc());
        }
        resp.setId(reqMessage.getId());

        return resp;
    }
}
