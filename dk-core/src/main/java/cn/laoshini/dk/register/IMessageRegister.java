package cn.laoshini.dk.register;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.exception.MessageException;
import cn.laoshini.dk.net.MessageHolder;
import cn.laoshini.dk.util.CollectionUtil;

/**
 * 游戏消息（用于自定义的，未依照当康系统设计的消息类接口设计的消息类）注册，该类实现消息类的发现、注册、和对象创建，类似于消息池（MessagePool）
 * <p>
 * 消息类注册支持系统自动扫描和手动注册两种方式，方法{@link #registerMessage(int, Class)}和{@link #registerMessages(Map)}用于支持手动注册。
 * </p>
 * <p>
 * 如果要使用系统自动扫描消息类的功能，用户的消息类需要遵守以下几个规范：
 * <ol>
 * <li>消息类必须统一继承或实现了指定超类，或被指定注解标记，这样消息类才能被扫描到</li>
 * <li>消息类必须有一个外部可访问的无参构造方法，否则系统无法创建对象</li>
 * <li>每当消息到达，都会产生一个新的消息对象，消息类应该是单例或其他共享对象的方式</li>
 * <li>一个消息类，必须对应于一个消息id，且消息id的最大值不得大于{@link Integer#MAX_VALUE}</li>
 * <li>消息类必须保证可获取到其对应的消息id，可以是通过消息类的方法，或者标记消息类注解的方法，该方法必须是无参方法，且方法名需与{@link #idMethod()}返回的一致</li>
 * </ol>
 * </p>
 *
 * @author fagarine
 */
public interface IMessageRegister extends IFunctionRegister {

    /**
     * 获取Message类扫描器
     *
     * @return 如果要使用类扫描，该方法不应该返回null
     */
    IClassScanner<Class<?>> scanner();

    /**
     * 设置Message类扫描器
     *
     * @param messageScanner Message类扫描器
     * @return 返回当前对象，用于fluent风格编程
     */
    IMessageRegister setScanner(IClassScanner<Class<?>> messageScanner);

    /**
     * 要想将Message类与消息id相关联，需要读取Message类对应的id，获取消息id读取器
     *
     * @return 如果要使用自动扫描与注册，该方法不应该返回null
     */
    Function<Class<?>, Integer> idReader();

    /**
     * 设置消息id读取器
     * <p>
     * 注意：仅当Message类中记录有消息id的信息时，可以使用该功能完成Message类的自动注册；
     * 如果Message类中并没有记录消息id时，用户应该手动注册Message类（如调用{@link #registerMessage(int, Class)} ），
     * 或重写{@link #action(ClassLoader)}方法，自定义自动注册逻辑
     * </p>
     *
     * @param idReader 消息id读取器
     * @return 返回当前对象，用于fluent风格编程
     */
    IMessageRegister setIdReader(Function<Class<?>, Integer> idReader);

    /**
     * 用户手动注册单个消息类
     *
     * @param messageId 消息id
     * @param messageClass 对应的消息类
     * @return 返回当前对象，用于fluent风格编程
     * @throws MessageException 如果传入的数据不符合规范，将会抛出该异常
     */
    default IMessageRegister registerMessage(int messageId, Class<?> messageClass) throws MessageException {
        MessageHolder.registerMessage(messageId, messageClass);
        return this;
    }

    /**
     * 用户手动注册多个消息类
     *
     * @param messageClassMap 要注册的消息类, key: 消息id, value: 消息类
     * @return 返回当前对象，用于fluent风格编程
     */
    default IMessageRegister registerMessages(Map<Integer, Class<?>> messageClassMap) {
        if (CollectionUtil.isNotEmpty(messageClassMap)) {
            for (Map.Entry<Integer, Class<?>> entry : messageClassMap.entrySet()) {
                registerMessage(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    @Override
    default void action(ClassLoader classLoader) {
        if (scanner() != null) {
            if (idReader() == null) {
                throw new BusinessException("id.reader.missing", "要使用系统自动扫描注册消息类，idReader不能为空");
            }

            List<Class<?>> classes = scanner().findClasses(classLoader);
            Function<Class<?>, Integer> idReader = idReader();
            for (Class<?> clazz : classes) {
                Integer messageId = idReader.apply(clazz);
                if (messageId != null) {
                    MessageHolder.registerMessage(messageId, clazz);
                }
            }
        }
    }

    /**
     * 游戏消息功能
     *
     * @return 返回功能名称
     */
    @Override
    default String functionName() {
        return "游戏消息";
    }
}
