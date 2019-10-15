package cn.laoshini.dk.register;

import java.util.Collections;
import java.util.List;

import cn.laoshini.dk.constant.GameServerProtocolEnum;
import cn.laoshini.dk.net.codec.IByteMessageDecoder;
import cn.laoshini.dk.net.codec.IByteMessageEncoder;
import cn.laoshini.dk.net.connect.IConnectClosedHandler;
import cn.laoshini.dk.net.connect.IConnectExceptionHandler;
import cn.laoshini.dk.net.connect.IConnectOpenedHandler;
import cn.laoshini.dk.net.msg.IMessageDispatcher;
import cn.laoshini.dk.net.msg.IMessageInterceptor;
import cn.laoshini.dk.net.session.IMessageSender;
import cn.laoshini.dk.net.session.ISessionCreator;
import cn.laoshini.dk.server.AbstractGameServer;

/**
 * @param <S> 应用内会话类型，对应一个客户端连接
 * @param <M> 消息类型
 * @author fagarine
 */
public interface IGameServerRegister<S, M> extends IFunctionRegister {

    /**
     * 获取游戏名称
     *
     * @return 进程内唯一
     */
    String gameName();

    /**
     * 获取游戏服务器监听端口
     *
     * @return 端口号
     */
    int port();

    /**
     * 获取游戏服务器使用的通信协议
     *
     * @return 通信协议枚举类型
     */
    GameServerProtocolEnum protocol();

    /**
     * 获取客户端连接最大空闲时间，超过该时间没有响应的连接将被断开
     *
     * @return 如果返回的值不大于0，表示不使用空闲时间监控，也不会主动断开空闲连接
     */
    int idleTime();

    /**
     * 如果使用了TCP作为通信协议，是否使用消息立即发送机制
     *
     * @return 仅当使用TCP通信时有效
     */
    boolean isTcpNoDelay();

    /**
     * 返回所有游戏数据加载器
     *
     * @return 该方法应保证不返回null，可以返回空列表
     */
    default List<IGameDataLoader> dataLoaders() {
        return Collections.emptyList();
    }

    /**
     * 返回一个用于创建会话的对象，如果返回null，将会使用系统自带的会话{@link cn.laoshini.dk.net.session.AbstractSession}
     *
     * @return 如果用户不希望使用系统自带的会话对象，需要保证该方法不会返回null
     */
    ISessionCreator<S> sessionCreator();

    /**
     * 返回一个消息解码器
     *
     * @return 仅支持二进制消息解码，不允许返回null
     */
    IByteMessageDecoder<M> decoder();

    /**
     * 返回一个消息编码器
     *
     * @return 仅支持编码为二进制消息，不允许返回null
     */
    IByteMessageEncoder<M> encoder();

    /**
     * 获取客户端连接建立成功时的行为
     *
     * @return 返回一个连接建立后的事件处理对象，该方法不允许返回null
     */
    IConnectOpenedHandler<S> connectOpenedOperation();

    /**
     * 获取客户端连接断开后的行为
     *
     * @return 返回一个连接断开后的事件处理对象，允许为null
     */
    default IConnectClosedHandler<S> connectClosedOperation() {
        return null;
    }

    /**
     * 返回客户端连接出现异常时的行为
     *
     * @return 返回一个连接异常时的事件处理对象，允许为null
     */
    default IConnectExceptionHandler<S> connectExceptionOperation() {
        return null;
    }

    /**
     * 返回所有消息拦截器对象
     *
     * @return 该方法应保证不返回null，可以返回空列表
     */
    default List<IMessageInterceptor<S, M>> messageInterceptors() {
        return Collections.emptyList();
    }

    /**
     * 返回一个消息分发器，用于消息到达后（到达的消息已经完成解码）的处理
     *
     * @return 该方法不允许返回null，否则服务器将不能启动
     */
    IMessageDispatcher<S, M> messageDispatcher();

    /**
     * 获取消息发送对象，注意：如果{@link #sessionCreator()}返回不为空，且用户使用了自定义的会话类型，则用户也必须自定义消息发送逻辑
     *
     * @return 该方法如果返回null，且用户使用了系统的会话类型，将使用系统默认的消息发送逻辑
     */
    IMessageSender<S, M> messageSender();

    /**
     * 启动服务器线程
     *
     * @return 返回当前对象，用于fluent风格编程
     */
    AbstractGameServer startServer();

    @Override
    default void action(ClassLoader classLoader) {
    }

    @Override
    default String functionName() {
        return "游戏服务器";
    }
}
