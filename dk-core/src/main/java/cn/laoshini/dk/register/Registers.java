package cn.laoshini.dk.register;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

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

    public static IMessageDtoRegister newDangKangCustomDtoRegister() {
        return () -> ClassScanners.<Tuple<Integer, Class<?>>>newAnnotationAndParentScanner(MessageHandle.class,
                IMessageHandler.class).setConverter(clazz -> {
            Class<? extends IMessageHandler> handlerClass = (Class<? extends IMessageHandler>) clazz;
            Class<?> dtoClass = ReflectHelper.getMessageHandlerGenericType(handlerClass);
            if (dtoClass != null) {
                int messageId = clazz.getAnnotation(MessageHandle.class).id();
                return new Tuple<>(messageId, dtoClass);
            }
            return null;
        });
    }

    public static IMessageRegister newDangKangCustomMessageRegister() {
        return new MessageRegisterAdapter().setScanner(ClassScanners.newParentScanner(IMessage.class, false, true))
                .setIdReader(ClassIdReader.methodReader(IMessage.ID_METHOD));
    }

    public static IMessageHandlerRegister newDangKangMessageHandlerRegister() {
        return new MessageHandlerRegisterAdaptor()
                .setScanner(ClassScanners.newAnnotationAndParentScanner(MessageHandle.class, IMessageHandler.class))
                .setIdReader(ClassIdReader.annotationReader(MessageHandle.class, MessageHandle.ID_METHOD));
    }

    public static IMessageHandlerRegister newDangKangSingletonHandlerRegister() {
        return newDangKangMessageHandlerRegister().singleton();
    }

    public static IEntityRegister newDangKangEntityRegister() {
        return new EntityRegisterAdaptor().setScanner(ClassScanners.newAnnotationScanner(TableMapping.class))
                .setTableNameReader(clazz -> {
                    TableMapping tableMapping = clazz.getAnnotation(TableMapping.class);
                    if (StringUtil.isNotEmptyString(tableMapping.value())) {
                        return tableMapping.value();
                    }
                    return clazz.getSimpleName();
                });
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

}
