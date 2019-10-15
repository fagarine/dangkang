package cn.laoshini.dk.net.handler;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import cn.laoshini.dk.annotation.MessageHandle;
import cn.laoshini.dk.constant.ExpressionConstant;
import cn.laoshini.dk.constant.GameConstant;
import cn.laoshini.dk.constant.GameServerProtocolEnum;
import cn.laoshini.dk.domain.ExecutorBean;
import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.domain.dto.HandlerExpDescriptorDTO;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.expression.IExpressionLogic;
import cn.laoshini.dk.net.msg.ReqMessage;
import cn.laoshini.dk.net.msg.RespMessage;

/**
 * 使用表达式代替代码，实现业务逻辑的消息处理Handler
 *
 * @author fagarine
 */
public class ExpressionMessageHandler implements IMessageHandler {

    /**
     * Handler的逻辑表达式详情
     */
    private HandlerExpDescriptorDTO descriptor;

    /**
     * 记录Handler对应注解相关信息
     */
    private ExecutorBean<MessageHandle> executorBean;

    /**
     * 表达式逻辑处理对象
     */
    private IExpressionLogic expressionLogic;

    public ExpressionMessageHandler(HandlerExpDescriptorDTO descriptor, IExpressionLogic expressionLogic) {
        this.descriptor = descriptor;
        this.expressionLogic = expressionLogic;

        // 生成对应的注解实例，并填充相关信息
        MessageHandle annotation = new MessageHandleImpl();
        String method = GameServerProtocolEnum.HTTP.name().equalsIgnoreCase(descriptor.getProtocol()) ?
                GameConstant.HANDLER_ACTION_METHOD :
                GameConstant.HANDLER_CALL_METHOD;
        executorBean = new ExecutorBean<>(annotation, method, ExpressionMessageHandler.class);
    }

    @Override
    public void action(ReqMessage reqMessage, GameSubject subject) throws MessageException {
        Map<String, Object> params = packageParams(reqMessage, subject);
        expressionLogic.execute(params);
    }

    @Override
    public RespMessage call(ReqMessage reqMessage, GameSubject subject) throws MessageException {
        Map<String, Object> params = packageParams(reqMessage, subject);
        return (RespMessage<?>) expressionLogic.execute(params);
    }

    private Map<String, Object> packageParams(ReqMessage<?> reqMessage, GameSubject subject) {
        Map<String, Object> params = new HashMap<>();
        params.put(ExpressionConstant.REQ_MESSAGE_PARAM, reqMessage);
        params.put(ExpressionConstant.REQ_MESSAGE_DATA, reqMessage.getData());
        params.put(ExpressionConstant.GAME_SUBJECT_PARAM, subject);
        return params;
    }

    public ExecutorBean<MessageHandle> getExecutorBean() {
        return executorBean;
    }

    /**
     * 实现@{@link MessageHandle}注解，为了能够动态创建注解的实例对象
     */
    public class MessageHandleImpl implements MessageHandle {

        @Override
        public int id() {
            return descriptor.getId();
        }

        @Override
        public String description() {
            return descriptor.getDescription();
        }

        @Override
        public boolean allowGuestRequest() {
            return descriptor.getGuest();
        }

        @Override
        public boolean sequential() {
            return descriptor.getSequential();
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return MessageHandle.class;
        }
    }
}
