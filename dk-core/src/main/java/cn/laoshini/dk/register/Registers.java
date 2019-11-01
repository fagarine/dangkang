package cn.laoshini.dk.register;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

import cn.laoshini.dk.annotation.MessageHandle;
import cn.laoshini.dk.dao.TableMapping;
import cn.laoshini.dk.domain.common.Tuple;
import cn.laoshini.dk.net.handler.IMessageHandler;
import cn.laoshini.dk.net.msg.IMessage;
import cn.laoshini.dk.util.ReflectHelper;
import cn.laoshini.dk.util.StringUtil;

/**
 * @author fagarine
 */
public class Registers {
    private Registers() {
    }

    private static final Set<IEntityRegister> ENTITY_REGISTERS = Collections.synchronizedSet(new LinkedHashSet<>());

    private static final Set<IMessageRegister> MESSAGE_REGISTERS = Collections.synchronizedSet(new LinkedHashSet<>());

    private static final Set<IMessageDtoRegister> DTO_REGISTERS = Collections.synchronizedSet(new LinkedHashSet<>());

    private static final Set<IMessageHandlerRegister> HANDLER_REGISTERS = Collections
            .synchronizedSet(new LinkedHashSet<>());

    public static final IMessageDtoRegister DK_CUSTOM_DTO_REGISTER = () -> ClassScanners.<Tuple<Integer, Class<?>>>newAnnotationAndParentScanner(
            MessageHandle.class, IMessageHandler.class).setConverter(clazz -> {
        Class<? extends IMessageHandler> handlerClass = (Class<? extends IMessageHandler>) clazz;
        Class<?> dtoClass = ReflectHelper.getMessageHandlerGenericType(handlerClass);
        if (dtoClass != null) {
            int messageId = clazz.getAnnotation(MessageHandle.class).id();
            return new Tuple<>(messageId, dtoClass);
        }
        return null;
    });

    public static final IMessageRegister DK_CUSTOM_MESSAGE_REGISTER = new MessageRegisterAdapter()
            .setScanner(ClassScanners.newParentScanner(IMessage.class, false, true))
            .setIdReader(ClassIdReader.methodReader(IMessage.ID_METHOD));

    public static final IMessageHandlerRegister DK_HANDLER_REGISTER = new MessageHandlerRegisterAdaptor()
            .setScanner(ClassScanners.newAnnotationAndParentScanner(MessageHandle.class, IMessageHandler.class))
            .setIdReader(ClassIdReader.annotationReader(MessageHandle.class, MessageHandle.ID_METHOD)).singleton();

    public static final IEntityRegister DK_ENTITY_REGISTER = new EntityRegisterAdaptor()
            .setScanner(ClassScanners.newAnnotationScanner(TableMapping.class)).setTableNameReader(clazz -> {
                TableMapping tableMapping = clazz.getAnnotation(TableMapping.class);
                if (StringUtil.isNotEmptyString(tableMapping.value())) {
                    return tableMapping.value();
                }
                return clazz.getSimpleName();
            });

    public static void addEntityRegister(IEntityRegister entityRegister) {
        if (entityRegister != null) {
            ENTITY_REGISTERS.add(entityRegister);
        }
    }

    public static Collection<IEntityRegister> getEntityRegisters() {
        return new ArrayList<>(ENTITY_REGISTERS);
    }

    public static void addMessageRegister(IMessageRegister messageRegister) {
        if (messageRegister != null) {
            MESSAGE_REGISTERS.add(messageRegister);
        }
    }

    public static Collection<IMessageRegister> getMessageRegisters() {
        return new ArrayList<>(MESSAGE_REGISTERS);
    }

    public static void addDtoRegister(IMessageDtoRegister dtoRegister) {
        if (dtoRegister != null) {
            DTO_REGISTERS.add(dtoRegister);
        }
    }

    public static Collection<IMessageDtoRegister> getDtoRegisters() {
        return new ArrayList<>(DTO_REGISTERS);
    }

    public static void addHandlerRegister(IMessageHandlerRegister handlerRegister) {
        if (handlerRegister != null) {
            HANDLER_REGISTERS.add(handlerRegister);
        }
    }

    public static Collection<IMessageHandlerRegister> getHandlerRegisters() {
        return new ArrayList<>(HANDLER_REGISTERS);
    }

    public static IMessageDtoRegister dangKangCustomDtoRegister() {
        return DK_CUSTOM_DTO_REGISTER;
    }

    public static IMessageRegister dangKangCustomMessageRegister() {
        return DK_CUSTOM_MESSAGE_REGISTER;
    }

    /**
     * 创建并返回一个，扫描传入包路径下所有类，通过{@link IMessage#ID_METHOD getId()}静态方法读取类id，并通过id将类注册为消息类的类扫描注册器
     * <p>
     * 该注册器会递归扫描包路径下的所有类，并根据类中是否有名称为getId()的无参静态方法，来决定是否将类注册到{@link cn.laoshini.dk.net.MessageHolder}
     * </p>
     *
     * @param packagePrefixes 扫描包路径
     * @return 该方法不会返回null
     */
    public static IMessageRegister newMethodIdMessageRegister(String[] packagePrefixes) {
        return new MessageRegisterAdapter().setScanner(ClassScanners.newPackageScanner(packagePrefixes))
                .setIdReader(ClassIdReader.methodReader(IMessage.ID_METHOD));
    }

    /**
     * 创建并返回一个，扫描传入包路径下所有类，通过{@link IMessage#ID_FIELD MESSAGE_ID}静态变量或常量读取类id，并通过id将类注册为消息类的类扫描注册器
     * <p>
     * 该注册器会递归扫描包路径下的所有类，并根据类中是否有名称为MESSAGE_ID的静态变量或常量，来决定是否将类注册到{@link cn.laoshini.dk.net.MessageHolder}
     * </p>
     *
     * @param packagePrefixes 扫描包路径
     * @return 该方法不会返回null
     */
    public static IMessageRegister newFieldIdMessageRegister(String[] packagePrefixes) {
        return new MessageRegisterAdapter().setScanner(ClassScanners.newPackageScanner(packagePrefixes))
                .setIdReader(ClassIdReader.fieldReader(IMessage.ID_FIELD));
    }

    /**
     * 创建并返回一个，用户自定义的消息类扫描注册器
     *
     * @param classFilter 类筛选器
     * @param packagePrefixes 类扫描路径
     * @param idReader 从类中读取消息id的id读取器
     * @return 该方法不会返回null
     */
    public static IMessageRegister newMessageRegister(IClassFilter classFilter, String[] packagePrefixes,
            Function<Class<?>, Integer> idReader) {
        return new MessageRegisterAdapter().setScanner(ClassScanners.newPackageScanner(classFilter, packagePrefixes))
                .setIdReader(idReader);
    }

    public static IMessageHandlerRegister dangKangMessageHandlerRegister() {
        return DK_HANDLER_REGISTER;
    }

    /**
     * 创建并返回一个，遵从当康系统设计的消息Handler类的类扫描注册器
     * <p>
     * 注意：该方法未传入类扫描路径，会使用当康系统容器启动时用户传入的包扫描路径，如果用户没有在启动当康系统容器时传入该值，请使用{@link #newDangKangHandlerRegister(String[])}方法
     * </p>
     *
     * @return 该方法不会返回null
     */
    public static IMessageHandlerRegister newDangKangHandlerRegister() {
        return new MessageHandlerRegisterAdaptor()
                .setScanner(ClassScanners.newAnnotationAndParentScanner(MessageHandle.class, IMessageHandler.class))
                .setIdReader(ClassIdReader.annotationReader(MessageHandle.class, MessageHandle.ID_METHOD));
    }

    public static IMessageHandlerRegister newDangKangHandlerRegister(String[] packagePrefixes) {
        return new MessageHandlerRegisterAdaptor().setScanner(ClassScanners
                .newAnnotationAndParentScanner(MessageHandle.class, IMessageHandler.class, packagePrefixes))
                .setIdReader(ClassIdReader.annotationReader(MessageHandle.class, MessageHandle.ID_METHOD));
    }

    public static <S, M> GameServerRegisterAdaptor<S, M> newGameServerRegisterAdaptor() {
        return new GameServerRegisterAdaptor<>();
    }

    public static <S, M> GameServerRegisterAdaptor<S, M> newTcpGameServerRegister() {
        return (GameServerRegisterAdaptor<S, M>) newGameServerRegisterAdaptor().tcp();
    }

    public static <S, M> GameServerRegisterAdaptor<S, M> newHttpGameServerRegister() {
        return (GameServerRegisterAdaptor<S, M>) newGameServerRegisterAdaptor().http();
    }

    public static <S, M> GameServerRegisterAdaptor<S, M> newUdpGameServerRegister() {
        return (GameServerRegisterAdaptor<S, M>) newGameServerRegisterAdaptor().udp();
    }

    public static <S, M> GameServerRegisterAdaptor<S, M> newWebsocketGameServerRegister() {
        return (GameServerRegisterAdaptor<S, M>) newGameServerRegisterAdaptor().websocket();
    }

    public static IEntityRegister dangKangEntityRegister() {
        return DK_ENTITY_REGISTER;
    }

    public static IEntityRegister newDangKangEntityRegister(String[] packagePrefixes) {
        return new EntityRegisterAdaptor()
                .setScanner(ClassScanners.newAnnotationScanner(TableMapping.class, packagePrefixes))
                .setTableNameReader(clazz -> {
                    TableMapping tableMapping = clazz.getAnnotation(TableMapping.class);
                    if (StringUtil.isNotEmptyString(tableMapping.value())) {
                        return tableMapping.value();
                    }
                    return clazz.getSimpleName();
                });
    }

}
