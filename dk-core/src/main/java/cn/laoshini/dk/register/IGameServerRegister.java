package cn.laoshini.dk.register;

import java.util.Collections;
import java.util.List;

import cn.laoshini.dk.constant.GameServerProtocolEnum;
import cn.laoshini.dk.constant.ServerType;
import cn.laoshini.dk.net.codec.INettyMessageDecoder;
import cn.laoshini.dk.net.codec.INettyMessageEncoder;
import cn.laoshini.dk.net.connect.IConnectClosedHandler;
import cn.laoshini.dk.net.connect.IConnectExceptionHandler;
import cn.laoshini.dk.net.connect.IConnectOpenedHandler;
import cn.laoshini.dk.net.msg.IMessageDispatcher;
import cn.laoshini.dk.net.msg.IMessageInterceptor;
import cn.laoshini.dk.net.session.IMessageSender;
import cn.laoshini.dk.net.session.ISessionCreator;

/**
 * 游戏服务器注册器
 *
 * @param <S> Session的首字母，表示应用内会话类型，对应一个客户端连接
 * @param <M> Message的首字母，表示消息类型
 * @author fagarine
 */
public interface IGameServerRegister<S, M> extends IFunctionRegister {

    /**
     * 获取游戏id
     *
     * @return 一个游戏一个id
     */
    int gameId();

    /**
     * 获取游戏名称
     *
     * @return 进程内唯一
     */
    String gameName();

    /**
     * 获取游戏服id
     *
     * @return 所有游戏服唯一
     */
    int serverId();

    /**
     * 获取游戏服名称
     *
     * @return 应尽量保证所有游戏服唯一
     */
    String serverName();

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
     * 是否采用 TCP 协议通信
     *
     * @return 返回判断结果
     */
    default boolean isTcp() {
        return GameServerProtocolEnum.TCP.equals(protocol());
    }

    /**
     * 是否采用 HTTP 协议通信
     *
     * @return 返回判断结果
     */
    default boolean isHttp() {
        return GameServerProtocolEnum.HTTP.equals(protocol());
    }

    /**
     * 是否采用 WebSocket 协议通信
     *
     * @return 返回判断结果
     */
    default boolean isWebsocket() {
        return GameServerProtocolEnum.WEBSOCKET.equals(protocol());
    }

    /**
     * 是否采用 UDP 协议通信
     *
     * @return 返回判断结果
     */
    default boolean isUdp() {
        return GameServerProtocolEnum.UDP.equals(protocol());
    }

    /**
     * 获取客户端连接最大空闲时间，超过该时间没有响应的连接将被断开
     *
     * @return 如果返回的值不大于0，表示不对客户端连接使用空闲状态监控，也不会主动断开空闲连接
     */
    int idleTime();

    /**
     * 如果使用了TCP作为通信协议，是否使用消息立即发送机制
     *
     * @return 仅当使用TCP通信时有效
     */
    boolean isTcpNoDelay();

    /**
     * 获取服务器类型
     *
     * @return 该方法不允许返回null
     */
    ServerType serverType();

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
    INettyMessageDecoder<M> decoder();

    /**
     * 返回一个消息编码器
     *
     * @return 仅支持编码为二进制消息，部分协议如Http、Websocket允许为null
     */
    INettyMessageEncoder<M> encoder();

    /**
     * 获取客户端连接建立成功时的行为，其主要目的是将会话对象Session与游戏内的玩家主体对象关联，以便于消息到达后快速关联到玩家主体。
     *
     * @return 返回一个连接建立后的事件处理对象，不允许返回null
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
     * 返回游戏服务器启动成功后的行为
     *
     * @return 返回服务器启动成功后，需要立即执行的逻辑，该方法如果返回null，表示没有需要立即执行的逻辑
     */
    IGameServerStartedHandler serverStartedHandler();

    /**
     * 当服务器停止对外服务时，返回给客户端的消息
     * <p>
     * 注意：该方法并非必须，仅在你想要在暂停服务器，又不想玩家退出游戏，返回给玩家提示信息时，才需要考虑使用
     * </p>
     * 这种方式并不推荐使用，服务器暂停时，将玩家留在游戏中，可能发生难以预料的结果
     *
     * @return 返回消息，如果该方法返回null，表示不返回任何消息给客户端
     */
    default Object pauseResponseMessage() {
        return null;
    }

    /**
     * 启动服务器线程
     *
     * @return 返回当前对象，用于fluent风格编程
     */
    IGameServerRegister<S, M> startServer();

    @Override
    default void action(ClassLoader classLoader) {
        startServer();
    }

    @Override
    default String functionName() {
        return "游戏服务器";
    }
}
