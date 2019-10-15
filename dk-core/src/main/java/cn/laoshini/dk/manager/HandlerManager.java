package cn.laoshini.dk.manager;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import cn.laoshini.dk.annotation.MessageHandle;
import cn.laoshini.dk.constant.GameCodeEnum;
import cn.laoshini.dk.constant.GameConstant;
import cn.laoshini.dk.constant.LogLabel;
import cn.laoshini.dk.domain.ExecutorBean;
import cn.laoshini.dk.domain.HandlerDesc;
import cn.laoshini.dk.domain.HandlerExecutorMethod;
import cn.laoshini.dk.domain.common.ArrayTuple;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.net.handler.IMessageHandler;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.ReflectHelper;
import cn.laoshini.dk.util.ReflectUtil;
import cn.laoshini.dk.util.SpringUtils;
import cn.laoshini.dk.util.TypeUtil;

/**
 * @author fagarine
 */
@Component
public class HandlerManager {

    @Value("#{dangKangBasicProperties.maxResponse}")
    private int maxResponseTime;

    /**
     * 记录已注册的消息标记相关信息
     */
    private Map<Integer, HandlerExecutorMethod> handlerExecutorMap = new ConcurrentHashMap<>();

    /**
     * 缓存，用于模块热更新时，新旧handler的过渡
     */
    private Map<Integer, HandlerExecutorMethod> executorCache = new ConcurrentHashMap<>();

    /**
     * 记录已注册消息的handler实例
     */
    private Map<Integer, Object> handlerMap = new ConcurrentHashMap<>();

    /**
     * handler缓存，用于模块热更新时，新旧handler的过渡
     */
    private Map<Integer, Object> handlerCache = new ConcurrentHashMap<>();

    public String registerHandler(int messageId, ExecutorBean<MessageHandle> executorBean) {
        checkExecutorMethodAbsent(messageId, executorBean.getExecutorClassName());

        MessageHandle annotation = executorBean.getAnnotation();
        @SuppressWarnings("unchecked")
        Class<? extends IMessageHandler> clazz = (Class<? extends IMessageHandler>) executorBean.getExecutorClass();
        // 记录handler类的泛型类型
        Class<?> genericType = ReflectHelper.getMessageHandlerGenericType(clazz);
        HandlerDesc extension = new HandlerDesc(messageId, annotation.description(), annotation.allowGuestRequest(),
                annotation.sequential(), true, genericType);
        HandlerExecutorMethod executorMethod = new HandlerExecutorMethod(messageId, clazz,
                executorBean.getExecutorMethod(), executorBean.getParams(), extension, true);

        return registerHandler(executorMethod, clazz);
    }

    private void checkExecutorMethodAbsent(int messageId, String handlerClassName) {
        HandlerExecutorMethod exist = getExecutorBean(messageId);
        if (exist != null) {
            throw new BusinessException("register.handler.duplicate",
                    String.format("多个消息处理handler注册了同一消息id, 消息id:%d, 已注册类:%s, 新注册类:%s", messageId, exist.getClassName(),
                            handlerClassName));
        }
    }

    public <HA extends Annotation> String registerExternalHandler(ExecutorBean<HA> executorBean, HandlerDesc desc,
            boolean buildOnce) {
        checkExecutorMethodAbsent(desc.getId(), executorBean.getExecutorClassName());

        HandlerExecutorMethod executorMethod = new HandlerExecutorMethod(desc.getId(), executorBean.getExecutorClass(),
                executorBean.getExecutorMethod(), executorBean.getParams(), desc, buildOnce);
        return registerHandler(executorMethod, executorBean.getExecutorClass());
    }

    private ArrayTuple<String, String> toParamTuple(Class<?>[] paramClass, String[] names) {
        if (CollectionUtil.isEmpty(paramClass)) {
            return new ArrayTuple<>();
        }

        String[] types = new String[paramClass.length];
        for (int i = 0; i < paramClass.length; i++) {
            Class<?> clazz = paramClass[i];
            types[i] = clazz.getName();
        }
        return toParamTuple(names, types);
    }

    private ArrayTuple<String, String> toParamTuple(String[] names, String[] types) {
        return new ArrayTuple<>(types, names);
    }

    private String registerHandler(HandlerExecutorMethod executorMethod, Class<?> handlerClass) {
        int messageId = executorMethod.getUid();
        checkExecutorMethodAbsent(messageId, executorMethod.getClassName());

        String beanName = null;
        if (executorMethod.isBuildOnce()) {
            Object handler;
            if (SpringUtils.isSpringBeanClass(handlerClass)) {
                handler = SpringUtils.registerSpringBean(handlerClass);
                beanName = StringUtils.uncapitalize(handlerClass.getSimpleName());
            } else {
                try {
                    handler = handlerClass.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new BusinessException("create.handler.fail",
                            String.format("创建handler实例失败, 消息id:%d, handler类:%s", messageId, handlerClass));
                }
            }
            // handler注册
            handlerMap.put(messageId, handler);
            executorMethod.setHandler(handler);
        }
        handlerExecutorMap.put(messageId, executorMethod);

        return beanName;
    }

    private void registerExternalHandler(HandlerExecutorMethod executorMethod, Object handler) {
        int messageId = executorMethod.getUid();
        checkExecutorMethodAbsent(messageId, executorMethod.getClassName());

        executorMethod.setBuildOnce(true);
        executorMethod.setHandler(handler);
        handlerMap.put(messageId, handler);
        handlerExecutorMap.put(messageId, executorMethod);
    }

    /**
     * 预备删除注册的handler，用于handler更新的过渡
     *
     * @param messageIds 预备删除handler的消息id
     */
    public void prepareUnregisterHandlers(Collection<Integer> messageIds) {
        if (CollectionUtil.isEmpty(messageIds)) {
            return;
        }

        // 将handler从已注册中移除，加入cache中，等待删除
        for (Integer messageId : messageIds) {
            HandlerExecutorMethod executorBean = handlerExecutorMap.remove(messageId);
            if (executorBean != null) {
                executorCache.put(messageId, executorBean);
            }

            Object handler = handlerMap.remove(messageId);
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
    public void unregisterHandlers(Collection<Integer> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }

        for (Integer messageId : messageIds) {
            unregisterHandler(messageId);
        }
    }

    public void unregisterHandler(Integer messageId) {
        // 删除ExecutorBean cache记录
        executorCache.remove(messageId);

        // 删除handler cache中的记录
        handlerCache.remove(messageId);
    }

    /**
     * 传入的消息是否允许游客请求
     *
     * @param messageId 消息id
     * @return 如果消息id不存在，将会抛出异常
     */
    public boolean allowGuestRequest(int messageId) {
        HandlerExecutorMethod executorMethod = getExecutorBeanOnCheckNotNull(messageId);
        return executorMethod.getExtension().isAllowGuestRequest();
    }

    public void invokeHandler(int messageId, Object... params) {
        HandlerExecutorMethod executorMethod = getExecutorBeanOnCheckNotNull(messageId);
        Object handler = getMessageHandlerOnCheck(messageId, executorMethod);

        checkHandlerMethodParams(messageId, executorMethod, params);

        long now = System.currentTimeMillis();
        try {
            ReflectUtil.invokeMethod(handler, GameConstant.HANDLER_ACTION_METHOD, params);
        } catch (Throwable t) {
            String print = String
                    .format("消息id[%d]执行处理逻辑出错, handler:%s, params: %s", messageId, handler, Arrays.toString(params));
            LogUtil.error(print, t);
            throw new MessageException(GameCodeEnum.SERVER_EXCEPTION, "protocol.handle.error", print, t);
        } finally {
            long duration = System.currentTimeMillis() - now;
            // 超过最大响应时间的操作，记录日志
            if (duration > maxResponseTime) {
                LogUtil.error(LogLabel.HANDLER, "消息id[{}]处理时间[{}], params: {}", messageId, duration,
                        Arrays.toString(params));
            }
        }
    }

    private void checkHandlerMethodParams(int messageId, HandlerExecutorMethod executorMethod, Object[] params) {
        String[] types = executorMethod.getParams().getV1();
        int paramCount = params == null ? 0 : params.length;
        int expectCount = types == null ? 0 : types.length;
        if (expectCount != paramCount) {
            throw new MessageException(GameCodeEnum.PARAM_ERROR, "handle.param.error",
                    String.format("消息id为[%d]的Handler的方法传入参数数量不匹配，期望数量: %d, 传入参数: %s", messageId, expectCount,
                            Arrays.toString(params)));
        }

        if (expectCount > 0) {
            for (int i = 0; i < types.length; i++) {
                String type = types[i];
                if (!TypeUtil.isSimilar(type, params[i].getClass())) {
                    throw new MessageException(GameCodeEnum.PARAM_ERROR, "handle.param.error",
                            String.format("消息id[%d]的Handler处理方法的[%s]参数类型不匹配，期望类型: %s, 传入参数: %s", messageId,
                                    executorMethod.getParams().getV2()[i], type, params[i]));
                }
            }
        }
    }

    private Object getMessageHandlerOnCheck(int messageId, HandlerExecutorMethod executorMethod) {
        Object handler = handlerMap.get(messageId);
        if (handler == null) {
            handler = handlerCache.get(messageId);
            if (handler == null) {
                throw new MessageException(GameCodeEnum.NO_HANDLER, "message.handler.missing",
                        String.format("找不到消息id[%d]的处理类[%s]的实例", messageId, executorMethod.getClassName()));
            }
        }
        return handler;
    }

    private HandlerExecutorMethod getExecutorBeanOnCheckNotNull(int messageId) {
        HandlerExecutorMethod executorMethod = getExecutorBean(messageId);
        if (executorMethod == null) {
            executorMethod = executorCache.get(messageId);
            if (executorMethod == null) {
                throw new MessageException(GameCodeEnum.NO_HANDLER, "message.handler.missing",
                        String.format("找不到消息id[%d]的处理类", messageId));
            }
        }
        return executorMethod;
    }

    private HandlerExecutorMethod getExecutorBean(int messageId) {
        return handlerExecutorMap.get(messageId);
    }

}
