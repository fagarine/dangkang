package cn.laoshini.dk.net;

import java.util.Collection;
import java.util.Map;

import cn.laoshini.dk.annotation.ConfigurableFunction;
import cn.laoshini.dk.constant.GameCodeEnum;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.net.msg.ICustomMessage;
import cn.laoshini.dk.util.LogUtil;

/**
 * 自定义格式消息工厂接口
 *
 * @author fagarine
 */
@ConfigurableFunction(key = "dk.message.factory")
public interface ICustomMessageFactory<Type extends ICustomMessage> {

    /**
     * 注册单个消息类
     *
     * @param messageId 消息id
     * @param messageClass 消息类
     */
    void registerMessage(int messageId, Class<Type> messageClass);

    /**
     * 注册传入的所有自定义消息类
     *
     * @param messageClassMap 自定义消息类集合
     */
    default void registerAllMessage(Map<Integer, Class<Type>> messageClassMap) {
        if (messageClassMap == null || messageClassMap.isEmpty()) {
            return;
        }

        for (Map.Entry<Integer, Class<Type>> entry : messageClassMap.entrySet()) {
            registerMessage(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 预备清除已注册的消息类，用于模块更新时的过渡
     *
     * @param messageIds 消息id
     */
    void prepareBatchUnregister(Collection<Integer> messageIds);

    /**
     * 清除已注册的消息类
     *
     * @param messageIds 消息id
     */
    void unregisterMessages(Collection<Integer> messageIds);

    /**
     * 返回消息id对应的消息类
     *
     * @param messageId 消息id
     * @return 返回消息id对应的消息类，有可能返回空
     */
    Class<Type> getMessageClass(int messageId);

    /**
     * 根据消息id返回新的消息实例对象
     *
     * @param messageId 消息id
     * @return 返回消息实例对象，有可能返回空
     */
    default Type newMessage(int messageId) {
        Class<Type> cls = getMessageClass(messageId);
        if (cls == null) {
            throw new MessageException(GameCodeEnum.NO_MESSAGE, "custom.message.missing",
                    String.format("找不到自定义消息:%d", messageId));
        }

        try {
            return cls.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            LogUtil.error(String.format("创建自定义消息实例出错, id:%d, class:%s", messageId, cls), e);
        }
        return null;
    }

    /**
     * 是否有指定id的消息类
     *
     * @param messageId 消息id
     * @return 是否有指定id的消息类
     */
    default boolean containsMessage(int messageId) {
        return getMessageClass(messageId) != null;
    }

}
