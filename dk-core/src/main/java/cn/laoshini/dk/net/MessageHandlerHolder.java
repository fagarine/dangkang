package cn.laoshini.dk.net;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.util.StringUtils;

import cn.laoshini.dk.annotation.ResourceHolder;
import cn.laoshini.dk.domain.HandlerDesc;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.net.handler.IMessageHandler;
import cn.laoshini.dk.util.CollectionUtil;

/**
 * @author fagarine
 */
@ResourceHolder
public enum MessageHandlerHolder {
    /**
     * 枚举实现单例
     */
    INSTANCE;

    /**
     * 记录handler类的类型
     */
    private static final Map<Integer, Class<?>> HANDLER_TYPE_MAP = new ConcurrentHashMap<>();

    /**
     * 缓存handler类的类型
     */
    private static final Map<Integer, Class<?>> HANDLER_TYPE_CACHE = new ConcurrentHashMap<>();

    /**
     * 记录handler类的单例对象
     */
    private static final Map<Integer, Object> HANDLER_INSTANCE_MAP = new ConcurrentHashMap<>();

    /**
     * 缓存handler类的单例对象
     */
    private static final Map<Integer, Object> HANDLER_INSTANCE_CACHE = new ConcurrentHashMap<>();

    /**
     * 记录类加载器已加载的消息id
     */
    private static final Map<ClassLoader, Set<Integer>> MESSAGE_ID_MAP = new HashMap<>();

    /**
     * 记录类加载器已加载的消息Handler，已注册到Spring容器的对象名称
     */
    private static final Map<ClassLoader, Set<String>> SPRING_BEAN_NAMES = new HashMap<>();

    private static final Map<Integer, HandlerDesc> DESCRIPTORS = new ConcurrentHashMap<>();

    private static void checkDuplicate(int messageId, Class<?> clazz) {
        Class<?> origin = HANDLER_TYPE_MAP.get(messageId);
        if (origin != null) {
            throw new BusinessException("message.handler.duplicate",
                    String.format("多个消息处理类注册了同一个消息id, id:%d, 已注册类:%s, 新发现类:%s", messageId, origin.getName(),
                            clazz.getName()));
        }

        Object handler = HANDLER_INSTANCE_MAP.get(messageId);
        if (handler != null) {
            throw new BusinessException("message.handler.duplicate",
                    String.format("多个消息处理类注册了同一个消息id, id:%d, 已注册类:%s, 新发现类:%s", messageId, handler.getClass().getName(),
                            clazz.getName()));
        }
    }

    private static void recordHandlerDescriptor(int messageId, Class<?> clazz, boolean allowGuestRequest) {
        HandlerDesc desc = new HandlerDesc(messageId);
        desc.setAllowGuestRequest(allowGuestRequest);
        if (IMessageHandler.class.isAssignableFrom(clazz)) {
            desc.setInternal(true);
            desc.setGenericType(MessageDtoClassHolder.getDtoClass(messageId));
        }
        DESCRIPTORS.put(messageId, desc);
    }

    /**
     * 注册消息处理Handler类（原型模式）
     *
     * @param messageId 消息id
     * @param clazz 消息处理类
     * @param allowGuestRequest 该消息是否允许未登录用户请求
     */
    public static void registerHandler(int messageId, Class<?> clazz, boolean allowGuestRequest) {
        checkDuplicate(messageId, clazz);

        HANDLER_TYPE_MAP.put(messageId, clazz);
        MESSAGE_ID_MAP.computeIfAbsent(clazz.getClassLoader(), (cl) -> new CopyOnWriteArraySet<>()).add(messageId);
        recordHandlerDescriptor(messageId, clazz, allowGuestRequest);
    }

    /**
     * 注册消息处理Handler类（原型模式）
     *
     * @param messageId 消息id
     * @param clazz 消息处理类
     */
    public static void registerHandler(int messageId, Class<?> clazz) {
        registerHandler(messageId, clazz, false);
    }

    /**
     * 注册消息处理Handler实例（单例模式）
     *
     * @param messageId 消息id
     * @param handler 消息处理对象
     * @param allowGuestRequest 该消息是否允许未登录用户请求
     */
    public static void registerSingletonHandler(int messageId, Object handler, boolean allowGuestRequest) {
        checkDuplicate(messageId, handler.getClass());

        HANDLER_INSTANCE_MAP.put(messageId, handler);
        MESSAGE_ID_MAP.computeIfAbsent(handler.getClass().getClassLoader(), (cl) -> new CopyOnWriteArraySet<>())
                .add(messageId);
        recordHandlerDescriptor(messageId, handler.getClass(), allowGuestRequest);
    }

    /**
     * 注册消息处理Handler实例（单例模式）
     *
     * @param messageId 消息id
     * @param handler 消息处理对象
     */
    public static void registerSingletonHandler(int messageId, Object handler) {
        checkDuplicate(messageId, handler.getClass());

        HANDLER_INSTANCE_MAP.put(messageId, handler);
        MESSAGE_ID_MAP.computeIfAbsent(handler.getClass().getClassLoader(), (cl) -> new CopyOnWriteArraySet<>())
                .add(messageId);
    }

    /**
     * 注册已添加到Spring容器，受Spring容器管理的Handler实例
     *
     * @param messageId 消息id
     * @param handler 消息处理对象
     */
    public static void registerSpringBeanHandler(int messageId, Object handler) {
        registerSingletonHandler(messageId, handler);

        String beanName = StringUtils.uncapitalize(handler.getClass().getSimpleName());
        SPRING_BEAN_NAMES.computeIfAbsent(handler.getClass().getClassLoader(), cl -> new CopyOnWriteArraySet<>())
                .add(beanName);
    }

    /**
     * 预备移除指定类加载器加载的Handler类
     *
     * @param classLoader 类加载器
     */
    public static void prepareUnregisterHandlers(ClassLoader classLoader) {
        Collection<Integer> messageIds = MESSAGE_ID_MAP.get(classLoader);
        if (CollectionUtil.isNotEmpty(messageIds)) {
            for (Integer messageId : messageIds) {
                Object handler = HANDLER_INSTANCE_MAP.remove(messageId);
                if (handler != null) {
                    HANDLER_INSTANCE_CACHE.put(messageId, handler);
                } else {
                    Class<?> clazz = HANDLER_TYPE_MAP.get(messageId);
                    if (clazz != null) {
                        HANDLER_TYPE_CACHE.put(messageId, clazz);
                    }
                }
            }
        }
    }

    /**
     * 移除指定类加载器加载的Handler记录
     *
     * @param classLoader 类加载器
     */
    public static void unregisterHandlers(ClassLoader classLoader) {
        HANDLER_TYPE_CACHE.clear();
        HANDLER_INSTANCE_CACHE.clear();
        Collection<Integer> messageIds = MESSAGE_ID_MAP.remove(classLoader);
        if (CollectionUtil.isNotEmpty(messageIds)) {
            for (Integer messageId : messageIds) {
                DESCRIPTORS.remove(messageId);
            }
        }
        SPRING_BEAN_NAMES.remove(classLoader);
    }

    /**
     * 在原型模式Handler类中查找，并返回查找到的Handler类
     *
     * @param messageId 消息id
     * @param <H> Handler类型
     * @return 该方法可能返回null
     */
    public static <H> Class<H> getProtoHandlerClass(int messageId) {
        Class<?> clazz = HANDLER_TYPE_MAP.get(messageId);
        if (clazz == null) {
            return (Class<H>) HANDLER_TYPE_CACHE.get(messageId);
        }
        return (Class<H>) clazz;
    }

    /**
     * 在单例Handler类中查找，并返回查找到的Handler类
     *
     * @param messageId 消息id
     * @param <H> Handler类型
     * @return 该方法可能返回null
     */
    public static <H> H getSingletonHandler(int messageId) {
        Object handler = HANDLER_INSTANCE_MAP.get(messageId);
        if (handler == null) {
            return (H) HANDLER_INSTANCE_CACHE.get(messageId);
        }
        return (H) handler;
    }

    /**
     * 获取指定消息id的消息处理Handler类，不管是单例模式还是原型模式的Handler类
     *
     * @param messageId 消息id
     * @param <H> Handler类型
     * @return 该方法可能返回null
     */
    public static <H> Class<H> getHandlerClass(int messageId) {
        Object handler = getSingletonHandler(messageId);
        if (handler != null) {
            return (Class<H>) handler.getClass();
        }

        return (Class<H>) getProtoHandlerClass(messageId);
    }

    /**
     * 是否存在处理指定消息id的Handler
     *
     * @param messageId 消息id
     * @return 返回查找结果
     */
    public static boolean exists(int messageId) {
        return getProtoHandlerClass(messageId) != null || getSingletonHandler(messageId) != null;
    }

    /**
     * 判断指定消息是否允许未登录用户请求
     *
     * @param messageId 消息id
     * @return 如果消息id找不到Handler记录，将返回false
     */
    public static boolean allowGuestRequest(int messageId) {
        HandlerDesc desc = DESCRIPTORS.get(messageId);
        return desc != null && desc.isAllowGuestRequest();
    }

    /**
     * 获取Handler的泛型类型，当康系统{@link IMessageHandler}类型的Handler专用
     *
     * @param messageId 消息id
     * @return 该方法可能返回null
     */
    public static Class<?> getHandlerGenericType(int messageId) {
        HandlerDesc desc = DESCRIPTORS.get(messageId);
        if (desc.getGenericType() == null) {
            return MessageDtoClassHolder.getDtoClass(messageId);
        }
        return desc.getGenericType();
    }
}
