package cn.laoshini.dk.net.handler;

import cn.laoshini.dk.constant.GameCodeEnum;
import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.domain.msg.ReqMessage;
import cn.laoshini.dk.domain.msg.RespMessage;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.executor.IOrderedExecutor;
import cn.laoshini.dk.executor.OrderedQueuePoolExecutor;
import cn.laoshini.dk.net.MessageHandlerHolder;
import cn.laoshini.dk.server.worker.MessageReceiveWorker;
import cn.laoshini.dk.util.LogUtil;

/**
 * 消息到达处理分发和处理
 *
 * @author fagarine
 */
public class MessageReceiveDispatcher {
    private static IOrderedExecutor<Long> MESSAGE_EXECUTOR;

    static {
        MESSAGE_EXECUTOR = new OrderedQueuePoolExecutor("received-message", 3);
    }

    private MessageReceiveDispatcher() {
    }

    /**
     * 接收到达的消息
     *
     * @param reqMessage 消息对象
     * @param gameSubject 消息所属主体
     */
    public static void messageReceived(ReqMessage<Object> reqMessage, GameSubject gameSubject) {
        MESSAGE_EXECUTOR.addTask(gameSubject.getSession().getId(), new MessageReceiveWorker(reqMessage, gameSubject));
    }

    /**
     * 消息处理
     *
     * @param reqMessage 进入消息
     * @param gameSubject 消息所属主体
     */
    public static void dealMessage(ReqMessage<Object> reqMessage, GameSubject gameSubject) {
        try {
            MessageHandlerHolder.doMessageHandler(reqMessage, gameSubject);
        } catch (MessageException e) {
            RespMessage<Object> resp = buildErrorResponse(reqMessage.getId() + 1, e.getGameCode().getCode(),
                    e.getMessage());
            gameSubject.getSession().sendMessage(resp);
        } catch (Throwable t) {
            LogUtil.error(String.format("消息处理出现未知异常, message:%s", reqMessage), t);
            RespMessage<Object> resp = buildErrorResponse(reqMessage.getId() + 1, GameCodeEnum.UNKNOWN_ERROR.getCode(),
                    "未知错误");
            gameSubject.getSession().sendMessage(resp);
        }
    }

    private static RespMessage<Object> buildErrorResponse(int respMessageId, int returnCode, String desc) {
        RespMessage<Object> resp = new RespMessage<>();
        resp.setId(respMessageId);
        resp.setCode(returnCode);
        resp.setParams(desc);
        return resp;
    }

    /**
     * 等待消息处理，并返回处理结果，本方法只会尝试调用{@link IHttpMessageHandler#call(ReqMessage, GameSubject)}，请保证对应的handler实现了该方法
     * 注意：
     * <p>
     * 通过该方法传入的消息，将会由当前线程执行逻辑，并且无法保证一定按消息到达先后执行
     * </p>
     *
     * @param reqMessage 进入消息
     * @param gameSubject 消息所属主体
     * @return 返回客户端的消息
     */
    public static RespMessage dealMessageAndBack(ReqMessage<Object> reqMessage, GameSubject gameSubject) {
        RespMessage resp = new RespMessage();
        try {
            return MessageHandlerHolder.doMessageHandlerCall(reqMessage, gameSubject);
        } catch (MessageException e) {
            resp.setCode(e.getGameCode().getCode());
            resp.setParams(e.getGameCode().getDesc());
        } catch (Throwable t) {
            LogUtil.error(String.format("消息处理出现未知异常, message:%s", reqMessage), t);
            resp.setCode(GameCodeEnum.UNKNOWN_ERROR.getCode());
            resp.setParams(GameCodeEnum.UNKNOWN_ERROR.getDesc());
        }
        resp.setId(reqMessage.getId());

        return resp;
    }
}
