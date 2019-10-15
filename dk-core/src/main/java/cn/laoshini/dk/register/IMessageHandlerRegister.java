package cn.laoshini.dk.register;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.net.MessageHandlerHolder;
import cn.laoshini.dk.util.CollectionUtil;

/**
 * 游戏消息处理类注册器
 *
 * @author fagarine
 */
public interface IMessageHandlerRegister extends IFunctionRegister {

    /**
     * 获取Handler类扫描器
     *
     * @return 如果要使用类扫描，该方法不应该返回null
     */
    IClassScanner<Class<?>> scanner();

    /**
     * 设置Handler类扫描器
     *
     * @param handlerScanner Handler类扫描器
     * @return 返回当前对象，用于fluent风格编程
     */
    IMessageHandlerRegister setScanner(IClassScanner<Class<?>> handlerScanner);

    /**
     * 要想将Handler类与消息id相关联，需要读取Handler类对应的id，获取消息id读取器
     *
     * @return 如果要使用自动扫描与注册，该方法不应该返回null
     */
    Function<Class<?>, Integer> idReader();

    /**
     * 设置消息id读取器
     * <p>
     * 注意：仅当Handler类中记录有消息id的信息时，可以使用该功能完成Handler类的自动注册；
     * 如果Handler类中并没有记录消息id时，用户应该手动注册Handler类（如调用{@link #registerHandlerClass(int, Class)}），
     * 或重写{@link #action(ClassLoader)}方法并实现{@link #registerHandlerClass(Class)}方法，自定义自动注册逻辑
     * </p>
     *
     * @param idReader 消息id读取器
     * @return 返回当前对象，用于fluent风格编程
     */
    IMessageHandlerRegister setIdReader(Function<Class<?>, Integer> idReader);

    /**
     * 设置handler对象使用单例模式（默认为原型模式，每次返回新的对象）
     * 使用建议：
     * <p>
     * 如果Handler对象本身不持有状态，即不依赖除了系统托管对象之外的其他数据，建议使用单例模式；
     * 反之，如果Handler本身会记录数据，则应该选择原型模式。
     * </p>
     *
     * @return 返回当前对象，用于fluent风格编程
     */
    IMessageHandlerRegister singleton();

    /**
     * 注册Handler类，该方法由系统调用
     *
     * @param handlerClass 消息处理Handler类
     * @return 返回当前对象，用于fluent风格编程
     */
    IMessageHandlerRegister registerHandlerClass(Class<?> handlerClass);

    @Override
    default void action(ClassLoader classLoader) {
        IClassScanner<Class<?>> handlerScanner = scanner();
        if (handlerScanner != null) {
            if (idReader() == null) {
                throw new BusinessException("id.reader.missing", "要使用系统自动扫描注册Handler类，idReader不能为空");
            }

            List<Class<?>> classes = handlerScanner.findClasses(classLoader);
            for (Class<?> clazz : classes) {
                registerHandlerClass(clazz);
            }
        }
    }

    /**
     * 用户手动注册单个消息处理Handler对象（单例模式下推荐）
     * <p>
     * 使用该方法的典型场景：用户的Handler类都托管给容器，由容器负责初始化，用户可以在Handler初始化时调用本方法来完成注册
     * </p>
     *
     * @param messageId 消息id
     * @param handler 消息处理类
     * @return 返回当前对象，用于fluent风格编程
     */
    default IMessageHandlerRegister registerHandler(int messageId, Object handler) {
        MessageHandlerHolder.registerSingletonHandler(messageId, handler);
        return this;
    }

    /**
     * 用户手动注册单个消息处理类
     *
     * @param messageId 消息id
     * @param handlerClass 消息处理类
     * @return 返回当前对象，用于fluent风格编程
     */
    default IMessageHandlerRegister registerHandlerClass(int messageId, Class<?> handlerClass) {
        MessageHandlerHolder.registerHandler(messageId, handlerClass);
        return this;
    }

    /**
     * 用户手动注册多个消息处理类
     *
     * @param handlerClassMap 要注册的消息类, key: 消息id, value: 消息处理类
     * @return 返回当前对象，用于fluent风格编程
     */
    default IMessageHandlerRegister registerHandlerClasses(Map<Integer, Class<?>> handlerClassMap) {
        if (CollectionUtil.isNotEmpty(handlerClassMap)) {
            for (Map.Entry<Integer, Class<?>> entry : handlerClassMap.entrySet()) {
                registerHandlerClass(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    @Override
    default String functionName() {
        return "消息处理Handler";
    }
}
