package cn.laoshini.dk.net;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import cn.laoshini.dk.annotation.ResourceHolder;
import cn.laoshini.dk.net.msg.ICustomDto;
import cn.laoshini.dk.util.CollectionUtil;

/**
 * @author fagarine
 */
@ResourceHolder
public enum MessageDtoClassHolder {
    /**
     * 枚举实现单例
     */
    INSTANCE;

    private static final Map<Integer, Class<?>> DTO_CLASS_MAP = new ConcurrentHashMap<>();

    private static final Map<Integer, Class<?>> CACHE = new ConcurrentHashMap<>();

    private static final Map<ClassLoader, Set<Integer>> MESSAGE_ID_MAP = new HashMap<>();

    public static MessageDtoClassHolder getInstance() {
        return INSTANCE;
    }

    public static void registerDtoClass(int messageId, Class<?> dtoClass) {
        DTO_CLASS_MAP.put(messageId, dtoClass);

        MESSAGE_ID_MAP.computeIfAbsent(dtoClass.getClassLoader(), (cl) -> new CopyOnWriteArraySet<>()).add(messageId);
    }

    public static void prepareUnregister(ClassLoader classLoader) {
        CACHE.clear();

        Set<Integer> messageIds = MESSAGE_ID_MAP.remove(classLoader);
        if (CollectionUtil.isNotEmpty(messageIds)) {
            for (Integer messageId : messageIds) {
                if (DTO_CLASS_MAP.containsKey(messageId)) {
                    CACHE.put(messageId, DTO_CLASS_MAP.remove(messageId));
                }
            }
        }
    }

    public static void cancelPrepareUnregister() {
        for (Map.Entry<Integer, Class<?>> entry : CACHE.entrySet()) {
            registerDtoClass(entry.getKey(), entry.getValue());
        }
        CACHE.clear();
    }

    public static void unregister() {
        CACHE.clear();
    }

    public static Class<?> getDtoClass(int messageId) {
        Class<?> clazz = DTO_CLASS_MAP.get(messageId);
        if (clazz == null) {
            clazz = CACHE.get(messageId);
        }
        return clazz;
    }

    public static Class<ICustomDto> getCustomDtoClass(int messageId) {
        Class<?> clazz = getDtoClass(messageId);
        if (clazz != null && ICustomDto.class.isAssignableFrom(clazz)) {
            return (Class<ICustomDto>) clazz;
        }
        return null;
    }
}
