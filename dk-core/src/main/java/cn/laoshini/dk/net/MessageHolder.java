package cn.laoshini.dk.net;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import cn.laoshini.dk.constant.GameCodeEnum;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.LogUtil;

/**
 * @author fagarine
 */
public enum MessageHolder {
    /**
     * 枚举实现单例
     */
    INSTANCE;

    private static final Map<Integer, Class<?>> MESSAGES = new HashMap<>();

    private static final Map<Integer, Class<?>> MESSAGE_CACHE = new HashMap<>();

    private static final Map<ClassLoader, Set<Integer>> MESSAGE_ID_MAP = new HashMap<>();

    /**
     * 注册单个消息类
     *
     * @param messageId 消息id
     * @param messageClass 消息类
     */
    public static void registerMessage(int messageId, Class<?> messageClass) {
        MESSAGES.put(messageId, messageClass);
        MESSAGE_ID_MAP.computeIfAbsent(messageClass.getClassLoader(), (cl) -> new CopyOnWriteArraySet<>())
                .add(messageId);
    }

    /**
     * 注册传入的所有自定义消息类
     *
     * @param messageClassMap 自定义消息类集合
     */
    public static void registerAllMessage(Map<Integer, Class<?>> messageClassMap) {
        if (messageClassMap == null || messageClassMap.isEmpty()) {
            return;
        }

        for (Map.Entry<Integer, Class<?>> entry : messageClassMap.entrySet()) {
            registerMessage(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 预备清除指定类加载器加载的消息类，用于模块更新时的过渡
     *
     * @param classLoader 类加载器
     */
    public static void prepareUnregisterMessages(ClassLoader classLoader) {
        Collection<Integer> messageIds = MESSAGE_ID_MAP.get(classLoader);
        if (CollectionUtil.isNotEmpty(messageIds)) {
            for (Integer messageId : messageIds) {
                MESSAGE_CACHE.put(messageId, MESSAGES.remove(messageId));
            }
        }
    }

    /**
     * 清除指定类加载器加载的消息类
     *
     * @param classLoader 类加载器
     */
    public static void unregisterMessages(ClassLoader classLoader) {
        Collection<Integer> messageIds = MESSAGE_ID_MAP.remove(classLoader);
        if (CollectionUtil.isNotEmpty(messageIds)) {
            for (Integer messageId : messageIds) {
                MESSAGE_CACHE.remove(messageId);
            }
        }
    }

    /**
     * 返回消息id对应的消息类
     *
     * @param messageId 消息id
     * @return 返回消息id对应的消息类，有可能返回空
     */
    public static Class<?> getMessageClass(int messageId) {
        Class<?> messageClass = MESSAGES.get(messageId);
        if (messageClass == null) {
            messageClass = MESSAGE_CACHE.get(messageId);
        }
        return messageClass;
    }

    /**
     * 根据消息id返回新的消息实例对象
     *
     * @param messageId 消息id
     * @return 返回消息实例对象，有可能返回null
     */
    public static Object newMessage(int messageId) {
        Class<?> cls = getMessageClass(messageId);
        if (cls == null) {
            throw new MessageException(GameCodeEnum.NO_MESSAGE, "message.noe.found",
                    String.format("找不到消息:%d", messageId));
        }

        try {
            return cls.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            LogUtil.error(String.format("创建消息实例出错（消息类没有提供可外部访问的无参构造）, id:%d, class:%s", messageId, cls), e);
        }
        return null;
    }

    /**
     * 是否有指定id的消息类
     *
     * @param messageId 消息id
     * @return 是否有指定id的消息类
     */
    public static boolean containsMessage(int messageId) {
        return getMessageClass(messageId) != null;
    }
}
