package cn.laoshini.dk.net;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Message;

import cn.laoshini.dk.annotation.ConfigurableFunction;
import cn.laoshini.dk.annotation.MessageHandle;
import cn.laoshini.dk.domain.ExecutorBean;
import cn.laoshini.dk.domain.GameSubject;
import cn.laoshini.dk.net.handler.ExpressionMessageHandler;
import cn.laoshini.dk.net.msg.ReqMessage;
import cn.laoshini.dk.net.msg.RespMessage;

/**
 * 消息处理handler管理接口
 *
 * @author fagarine
 */
@ConfigurableFunction(key = "dk.message.manager")
public interface IMessageHandlerManager {

    /**
     * 注册单个消息处理handler
     *
     * @param messageId 消息id
     * @param executorBean handler相关记录
     * @return 如果handler由Spring容器实例化，返回对应的beanName，否则返回null
     */
    String registerHandler(int messageId, ExecutorBean<MessageHandle> executorBean);

    /**
     * 批量注册消息处理handler，并返回这些handler中有Spring容器管理的对象的beanName
     *
     * @param handlerExecutorMap 待注册的handler信息
     * @return 该方法不会返回null
     */
    List<String> registerHandlers(Map<Integer, ExecutorBean<MessageHandle>> handlerExecutorMap);

    /**
     * 注册表达式Handler
     *
     * @param messageId 消息id
     * @param expHandler 表达式Handler实例
     */
    void registerExpHandler(int messageId, ExpressionMessageHandler expHandler);

    /**
     * 预删除注册的handler，用于handler更新的过渡
     *
     * @param messageIds 预备删除handler的消息id
     */
    void prepareUnregisterHandlers(Collection<Integer> messageIds);

    /**
     * 清除传入消息id的注册信息
     *
     * @param messageId 消息id
     */
    void unregisterHandler(int messageId);

    /**
     * 清除传入消息id的注册信息
     *
     * @param messageIds 消息id集合
     */
    void unregisterHandlers(Collection<Integer> messageIds);

    /**
     * 获取handler类定义的泛型类
     *
     * @param messageId 消息id
     * @return 该方法可能返回null
     */
    Class<?> getHandlerGenericType(int messageId);

    /**
     * 获取protobuf消息处理handler类的泛型类
     *
     * @param messageId 消息id
     * @return 该方法可能返回null
     */
    default Class<Message> getProtobufHandlerGenericType(int messageId) {
        Class<?> clazz = getHandlerGenericType(messageId);
        if (clazz != null && Message.class.isAssignableFrom(clazz)) {
            return (Class<Message>) clazz;
        }
        return null;
    }

    /**
     * 传入的消息是否允许游客请求
     *
     * @param messageId 消息id
     * @return 如果消息id不存在，将会抛出异常
     */
    boolean allowGuestRequest(int messageId);

    /**
     * 消息id对应的处理handler是否已存在
     *
     * @param messageId 消息id
     * @return 返回是否已存在
     */
    boolean exists(int messageId);

    /**
     * 执行消息handler逻辑的action()方法
     *
     * @param reqMessage 进入消息
     * @param gameSubject 消息所属主体对象
     */
    void doMessageHandlerAction(ReqMessage reqMessage, GameSubject gameSubject);

    /**
     * 执行消息handler逻辑的call()方法
     *
     * @param reqMessage 进入消息
     * @param gameSubject 消息所属主体对象
     * @return 返回执行后的发往客户端的消息
     */
    RespMessage doMessageHandlerCall(ReqMessage reqMessage, GameSubject gameSubject);
}
