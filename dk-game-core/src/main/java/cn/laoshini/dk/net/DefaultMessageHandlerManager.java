package cn.laoshini.dk.net;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.protobuf.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import cn.laoshini.dk.annotation.FunctionVariousWays;
import cn.laoshini.dk.annotation.MessageHandle;
import cn.laoshini.dk.condition.ConditionalOnPropertyValue;
import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.constant.GameCodeEnum;
import cn.laoshini.dk.constant.LogLabel;
import cn.laoshini.dk.domain.ExecutorBean;
import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.net.handler.ExpressionMessageHandler;
import cn.laoshini.dk.net.handler.IMessageHandler;
import cn.laoshini.dk.net.handler.IProtobufMessageHandler;
import cn.laoshini.dk.net.msg.ReqMessage;
import cn.laoshini.dk.net.msg.RespMessage;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.ReflectHelper;
import cn.laoshini.dk.util.SpringUtils;

/**
 * 消息处理handler管理类
 *
 * @author fagarine
 */
@Component
@FunctionVariousWays
@ConditionalOnPropertyValue(propertyName = "dk.message.manager", havingValue = Constants.DEFAULT_PROPERTY_NAME)
public class DefaultMessageHandlerManager implements IMessageHandlerManager {

    @Value("#{dangKangBasicProperties.maxResponse}")
    private int maxResponseTime;

    /**
     * 记录已注册的消息标记相关信息
     */
    private Map<Integer, ExecutorBean<MessageHandle>> handlerExecutorMap = new ConcurrentHashMap<>();

    /**
     * 缓存，用于模块热更新时，新旧handler的过渡
     */
    private Map<Integer, ExecutorBean<MessageHandle>> executorCache = new ConcurrentHashMap<>();

    /**
     * 记录已注册消息的handler实例
     */
    private Map<Integer, IMessageHandler> handlerMap = new ConcurrentHashMap<>();

    /**
     * handler缓存，用于模块热更新时，新旧handler的过渡
     */
    private Map<Integer, IMessageHandler> handlerCache = new ConcurrentHashMap<>();

    /**
     * 记录handler类的泛型类型
     */
    private Map<Integer, Class<?>> handlerGenericType = new ConcurrentHashMap<>();

    @Override
    public boolean exists(int messageId) {
        return getExecutorBean(messageId) != null;
    }

    /**
     * 注册单个消息处理的handler
     *
     * @param messageId 消息id
     * @param executorBean handler相关记录
     * @return 如果handler由Spring容器实例化，返回对应的beanName，否则返回null
     */
    @Override
    public String registerHandler(int messageId, ExecutorBean<MessageHandle> executorBean) {
        @SuppressWarnings("unchecked")
        Class<? extends IMessageHandler> clazz = (Class<? extends IMessageHandler>) executorBean.getExecutorClass();
        if (!IMessageHandler.class.isAssignableFrom(clazz)) {
            throw new BusinessException("handler.class.error",
                    String.format("handler类必须实现IMessageHandler接口, 消息id:%d, handler类:%s", messageId, clazz));
        }

        ExecutorBean<MessageHandle> originExecutorBean = handlerExecutorMap.get(messageId);
        if (originExecutorBean != null) {
            // 表达式Handler
            if (originExecutorBean.getAnnotation() instanceof ExpressionMessageHandler.MessageHandleImpl) {
                throw new BusinessException("registered.exp.handler",
                        String.format("消息id [%d] 已注册为表达式处理Handler, 新注册类 [%s] 不能再注册，请检查配置", messageId, clazz));
            }

            throw new BusinessException("register.handler.duplicate",
                    String.format("多个消息处理handler注册了同一消息id, 消息id:%d, 已注册类:%s, 新注册类:%s", messageId,
                            originExecutorBean.getExecutorClass(), clazz));
        }

        // 记录handler类的泛型类型
        Class<?> genericType = ReflectHelper.getMessageHandlerGenericType(clazz);
        if (genericType != null) {
            handlerGenericType.put(messageId, genericType);
        }

        String beanName = null;
        if (SpringUtils.isSpringBeanClass(clazz)) {
            IMessageHandler handler = SpringUtils.registerSpringBean(clazz);
            handlerMap.put(messageId, handler);
            beanName = StringUtils.uncapitalize(clazz.getSimpleName());
        } else {
            try {
                IMessageHandler handler = clazz.newInstance();

                // handler注册
                handlerMap.put(messageId, handler);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new BusinessException("create.handler.fail",
                        String.format("创建handler实例失败, 消息id:%d, handler类:%s", messageId, clazz));
            }
        }

        handlerExecutorMap.put(messageId, executorBean);
        return beanName;
    }

    /**
     * 批量注册消息处理handler，并返回这些handler中有Spring容器管理的对象的beanName
     *
     * @param handlerExecutorMap 待注册的handler信息
     * @return 该方法不会返回null
     */
    @Override
    public List<String> registerHandlers(Map<Integer, ExecutorBean<MessageHandle>> handlerExecutorMap) {
        List<String> beanNames = new ArrayList<>();
        for (Map.Entry<Integer, ExecutorBean<MessageHandle>> entry : handlerExecutorMap.entrySet()) {
            String beanName = registerHandler(entry.getKey(), entry.getValue());
            if (beanName != null) {
                beanNames.add(beanName);
            }
        }

        return beanNames;
    }

    /**
     * 注册表达式Handler
     *
     * @param messageId 消息id
     * @param handler 表达式Handler实例
     */
    @Override
    public void registerExpHandler(int messageId, ExpressionMessageHandler handler) {
        IMessageHandler origin = handlerMap.get(messageId);
        if (origin != null) {
            throw new BusinessException("register.handler.duplicate",
                    String.format("消息id [%d] 已注册了handler类 [%s]， 不能再注册为表达式Handler，请检查配置", messageId, origin.getClass()));
        }

        handlerExecutorMap.put(messageId, handler.getExecutorBean());
        handlerMap.put(messageId, handler);
    }

    /**
     * 预备删除注册的handler，用于handler更新的过渡
     *
     * @param messageIds 预备删除handler的消息id
     */
    @Override
    public void prepareUnregisterHandlers(Collection<Integer> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }

        // 将handler从已注册中移除，加入cache中，等待删除
        for (Integer messageId : messageIds) {
            ExecutorBean<MessageHandle> executorBean = handlerExecutorMap.remove(messageId);
            if (executorBean != null) {
                executorCache.put(messageId, executorBean);
            }

            IMessageHandler handler = handlerMap.remove(messageId);
            if (handler != null) {
                handlerCache.put(messageId, handler);
            }
        }
    }

    /**
     * 清除传入消息id的注册信息
     *
     * @param messageIds 消息id
     */
    @Override
    public void unregisterHandlers(Collection<Integer> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }

        for (Integer messageId : messageIds) {
            unregisterHandler(messageId);
        }
    }

    @Override
    public void unregisterHandler(int messageId) {
        // 删除ExecutorBean cache记录
        executorCache.remove(messageId);

        // 删除handler cache中的记录
        handlerCache.remove(messageId);

        // 删除泛型信息记录
        if (!handlerMap.containsKey(messageId)) {
            handlerGenericType.remove(messageId);
        }
    }

    private ExecutorBean<MessageHandle> getExecutorBean(int messageId) {
        ExecutorBean<MessageHandle> executorBean = handlerExecutorMap.get(messageId);
        if (executorBean == null) {
            executorBean = executorCache.get(messageId);
        }
        return executorBean;
    }

    /**
     * 获取handler类定义的泛型类
     *
     * @param messageId 消息id
     * @return 该方法可能返回null
     */
    @Override
    public Class<?> getHandlerGenericType(int messageId) {
        return handlerGenericType.get(messageId);
    }

    /**
     * 获取protobuf消息处理handler类的泛型类
     *
     * @param messageId 消息id
     * @return 该方法可能返回null
     */
    @SuppressWarnings("unchecked")
    @Override
    public Class<Message> getProtobufHandlerGenericType(int messageId) {
        IMessageHandler handler = getMessageHandlerOnCheck(messageId);
        if (handler instanceof IProtobufMessageHandler) {
            return (Class<Message>) handlerGenericType.get(messageId);
        }

        throw new MessageException(GameCodeEnum.SERVER_EXCEPTION, "protobuf.handler.error",
                String.format("消息id [%d] 的handler配置错误，不是protobuf消息处理handler", messageId));
    }

    /**
     * 传入的消息是否允许游客请求
     *
     * @param messageId 消息id
     * @return 如果消息id不存在，将会抛出异常
     */
    @Override
    public boolean allowGuestRequest(int messageId) {
        ExecutorBean<MessageHandle> executorBean = getExecutorBeanOnCheck(messageId);
        return executorBean.getAnnotation().allowGuestRequest();
    }

    /**
     * 执行消息handler逻辑的action()方法
     *
     * @param reqMessage 进入消息
     * @param gameSubject 消息所属主体对象
     */
    @Override
    public void doMessageHandlerAction(ReqMessage reqMessage, GameSubject gameSubject) {
        if (reqMessage == null) {
            throw new MessageException(GameCodeEnum.PARAM_ERROR, "req.message.null", "进入消息不能为空");
        }

        invokeHandlerAction(reqMessage.getId(), reqMessage, gameSubject);
    }

    /**
     * 执行消息handler逻辑的call()方法
     *
     * @param reqMessage 进入消息
     * @param gameSubject 消息所属主体对象
     * @return 返回执行后的发往客户端的消息
     */
    @Override
    public RespMessage doMessageHandlerCall(ReqMessage reqMessage, GameSubject gameSubject) {
        if (reqMessage == null) {
            throw new MessageException(GameCodeEnum.PARAM_ERROR, "req.message.null", "进入消息不能为空");
        }

        return invokeHandlerCall(reqMessage.getId(), reqMessage, gameSubject);
    }

    private void invokeHandlerAction(int messageId, ReqMessage<?> reqMessage, GameSubject subject) {
        IMessageHandler handler = getMessageHandlerOnCheck(messageId);

        long now = System.currentTimeMillis();
        try {
            handler.action(reqMessage, subject);
            //            ReflectUtil.invokeMethod(handler, Constants.HANDLER_ACTION_METHOD, reqMessage, subject);
        } catch (Throwable t) {
            String print = String
                    .format("消息id[%d]执行处理逻辑出错, handler:%s, message:%s, subject:%s", messageId, handler, reqMessage,
                            subject);
            LogUtil.error(print, t);
            throw new MessageException(GameCodeEnum.SERVER_EXCEPTION, "protocol.handle.error", print, t);
        } finally {
            long duration = System.currentTimeMillis() - now;
            // 超过最大响应时间的操作，记录日志
            if (duration > maxResponseTime) {
                LogUtil.error(LogLabel.HANDLER, "消息[{}]处理时间[{}]", reqMessage, duration);
            }
        }
    }

    private RespMessage invokeHandlerCall(int messageId, ReqMessage<?> reqMessage, GameSubject subject) {
        IMessageHandler handler = getMessageHandlerOnCheck(messageId);

        long now = System.currentTimeMillis();
        try {
            return handler.call(reqMessage, subject);
            //            return (RespMessage) ReflectUtil.invokeMethod(handler, Constants.HANDLER_CALL_METHOD, reqMessage, subject);
        } catch (Throwable t) {
            String print = String
                    .format("消息id[%d]执行处理逻辑出错, handler:%s, message:%s, subject:%s", messageId, handler, reqMessage,
                            subject);
            LogUtil.error(print, t);
            throw new MessageException(GameCodeEnum.SERVER_EXCEPTION, "protocol.handle.error", print, t);
        } finally {
            long duration = System.currentTimeMillis() - now;
            // 超过最大响应时间的操作，记录日志
            if (duration > maxResponseTime) {
                LogUtil.error(LogLabel.HANDLER, "消息[{}]处理时间[{}]", reqMessage, duration);
            }
        }
    }

    private IMessageHandler getMessageHandlerOnCheck(int messageId) {
        ExecutorBean<MessageHandle> executorBean = getExecutorBeanOnCheck(messageId);

        IMessageHandler handler = handlerMap.get(messageId);
        if (handler == null) {
            handler = handlerCache.get(messageId);
            if (handler == null) {
                throw new MessageException(GameCodeEnum.NO_HANDLER, "message.handler.missing",
                        String.format("找不到消息id[%d]的处理类[%s]的实例", messageId, executorBean.getExecutorClassName()));
            }
        }
        return handler;
    }

    private ExecutorBean<MessageHandle> getExecutorBeanOnCheck(int messageId) {
        ExecutorBean<MessageHandle> executorBean = getExecutorBean(messageId);
        if (executorBean == null) {
            throw new MessageException(GameCodeEnum.NO_HANDLER, "message.handler.missing",
                    String.format("找不到消息id[%d]的处理类", messageId));
        }
        return executorBean;
    }
}
