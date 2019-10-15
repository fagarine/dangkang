package cn.laoshini.dk.register;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.MessageLite;

import cn.laoshini.dk.constant.GameServerProtocolEnum;
import cn.laoshini.dk.domain.GameServerConfig;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.net.codec.IByteMessageDecoder;
import cn.laoshini.dk.net.codec.IByteMessageEncoder;
import cn.laoshini.dk.net.connect.IConnectClosedHandler;
import cn.laoshini.dk.net.connect.IConnectExceptionHandler;
import cn.laoshini.dk.net.connect.IConnectOpenedHandler;
import cn.laoshini.dk.net.msg.IMessageDispatcher;
import cn.laoshini.dk.net.msg.IMessageInterceptor;
import cn.laoshini.dk.net.server.AbstractInnerGameServer;
import cn.laoshini.dk.net.server.InnerGameServerFactory;
import cn.laoshini.dk.net.session.IMessageSender;
import cn.laoshini.dk.net.session.ISessionCreator;
import cn.laoshini.dk.server.AbstractGameServer;
import cn.laoshini.dk.util.NetUtil;
import cn.laoshini.dk.util.StringUtil;

/**
 * @author fagarine
 */
public class GameServerRegisterAdaptor<S, M> implements IGameServerRegister<S, M> {

    private static final AtomicInteger SERVER_ID = new AtomicInteger(1);

    private int serverId;

    private String gameName;

    private int port;

    private GameServerProtocolEnum protocol = GameServerProtocolEnum.TCP;

    private int idleTime = 300;

    private boolean tcpNoDelay;

    private List<IGameDataLoader> dataLoaders = new ArrayList<>();

    private ISessionCreator<S> sessionCreator;

    private IByteMessageDecoder<M> decoder;

    private IByteMessageEncoder<M> encoder;

    private IConnectOpenedHandler<S> connectOpened;

    private IConnectClosedHandler<S> connectClosed;

    private IConnectExceptionHandler<S> connectException;

    private List<IMessageInterceptor<S, M>> messageInterceptors = new ArrayList<>();

    private IMessageDispatcher<S, M> messageDispatcher;

    private IMessageSender<S, M> sender;

    private AbstractInnerGameServer<S, M> gameServer;

    @Override
    public AbstractGameServer startServer() {
        if (StringUtil.isEmptyString(gameName)) {
            throw new IllegalArgumentException("无效的游戏名称:" + gameName);
        }

        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("无效端口号:" + port);
        }

        if (!NetUtil.localPortAble(port)) {
            throw new IllegalArgumentException("端口号已被占用:" + port);
        }

        ClassLoader classLoader = getClass().getClassLoader();

        // 加载数据
        if (!dataLoaders.isEmpty()) {
            for (IGameDataLoader loader : dataLoaders) {
                try {
                    loader.load();
                } catch (Exception e) {
                    throw new BusinessException("load.data.error", "加载游戏数据出错, game:" + gameName, e);
                }
            }
        }

        // 启动服务器线程
        gameServer = InnerGameServerFactory.newGameServer(this);
        gameServer.start();
        return gameServer;
    }

    /**
     * 设置游戏名称
     *
     * @param name 游戏名
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setGameName(String name) {
        this.gameName = name;
        return self();
    }

    /**
     * 设置游戏服务器绑定端口
     *
     * @param bindPort 端口号
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setPort(int bindPort) {
        this.port = bindPort;
        return self();
    }

    /**
     * 设置游戏服通信协议
     *
     * @param protocolEnum 通信协议枚举类型
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setProtocol(GameServerProtocolEnum protocolEnum) {
        if (protocolEnum != null) {
            this.protocol = protocolEnum;
        }
        return self();
    }

    /**
     * 设置游戏服使用TCP协议通信
     *
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> tcp() {
        this.protocol = GameServerProtocolEnum.TCP;
        return self();
    }

    /**
     * 设置游戏服使用UDP协议通信
     *
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> udp() {
        this.protocol = GameServerProtocolEnum.UDP;
        return self();
    }

    /**
     * 设置游戏服使用HTTP协议通信
     *
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> http() {
        this.protocol = GameServerProtocolEnum.HTTP;
        return self();
    }

    /**
     * 设置判定连接为空闲的时间（超过该时间没有响应，将断开连接），仅对TCP、UDP协议有效
     *
     * @param idleSeconds 秒
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setIdleTime(int idleSeconds) {
        if (idleSeconds > 0) {
            this.idleTime = idleSeconds;
        }
        return self();
    }

    /**
     * 设置消息不等待，立即发送，仅对TCP协议有效
     *
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setTcpNoDelay() {
        this.tcpNoDelay = true;
        return self();
    }

    /**
     * 添加游戏数据加载器
     *
     * @param loader 游戏数据加载器
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> addDataLoader(IGameDataLoader loader) {
        if (!dataLoaders.contains(loader)) {
            this.dataLoaders.add(loader);
        }
        return self();
    }

    /**
     * 设置服务端会话对象构造器
     *
     * @param sessionCreator 会话对象构造器
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setSessionCreator(ISessionCreator<S> sessionCreator) {
        this.sessionCreator = sessionCreator;
        return self();
    }

    /**
     * 设置消息解码器
     *
     * @param decoder 解码器
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setMessageDecode(IByteMessageDecoder<M> decoder) {
        this.decoder = decoder;
        return self();
    }

    /**
     * 设置消息编码前
     *
     * @param encoder 消息编码器
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setMessageEncode(IByteMessageEncoder<M> encoder) {
        this.encoder = encoder;
        return self();
    }

    /**
     * 设置使用Protobuf编解码器（会同时设置编码器和解码器）
     *
     * @param prototype Protobuf消息原型类
     * @param extensionRegistry 如果使用Protobuf3之前的版本，需要传入扩展类注册表对象ExtensionRegistry
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setProtobufCodec(MessageLite prototype,
            ExtensionRegistry extensionRegistry) {
        this.decoder = (IByteMessageDecoder<M>) IByteMessageDecoder.newProtobufDecoder(prototype, extensionRegistry);
        this.encoder = (IByteMessageEncoder<M>) IByteMessageEncoder.newProtobufEncoder();
        return self();
    }

    /**
     * 设置当客户端连接建立时的操作
     *
     * @param connectOpened 客户端连接建立时的业务逻辑
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> onConnected(IConnectOpenedHandler<S> connectOpened) {
        this.connectOpened = connectOpened;
        return self();
    }

    /**
     * 设置连接断开时的操作
     *
     * @param connectClosed 连接断开时的业务逻辑
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> onDisconnected(IConnectClosedHandler<S> connectClosed) {
        this.connectClosed = connectClosed;
        return self();
    }

    /**
     * 设置连接捕获到异常时的操作
     *
     * @param connectException 连接发送异常时的逻辑
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> onConnectException(IConnectExceptionHandler<S> connectException) {
        this.connectException = connectException;
        return self();
    }

    /**
     * 添加消息拦截器
     *
     * @param messageInterceptor 消息拦截器对象
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> addMessageInterceptor(IMessageInterceptor<S, M> messageInterceptor) {
        if (!messageInterceptors.contains(messageInterceptor)) {
            this.messageInterceptors.add(messageInterceptor);
        }
        return self();
    }

    /**
     * 设置消息到达时的转发逻辑
     *
     * @param messageDispatcher 消息转发逻辑
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> onMessageDispatcher(IMessageDispatcher<S, M> messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
        return self();
    }

    /**
     * 设置消息发送逻辑
     *
     * @param sender 消息发送逻辑
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> onMessageSend(IMessageSender<S, M> sender) {
        this.sender = sender;
        return self();
    }

    /**
     * 将配置信息转为{@link GameServerConfig}对象发回
     *
     * @return 该方法不会返回null
     */
    public GameServerConfig toGameServerConfig() {
        if (serverId == 0) {
            serverId = SERVER_ID.getAndIncrement();
        }
        return new GameServerConfig(serverId, gameName(), port(), protocol(), idleTime(), isTcpNoDelay());
    }

    private GameServerRegisterAdaptor<S, M> self() {
        return this;
    }

    @Override
    public String gameName() {
        return gameName;
    }

    @Override
    public int port() {
        return port;
    }

    @Override
    public GameServerProtocolEnum protocol() {
        return protocol;
    }

    @Override
    public int idleTime() {
        return idleTime;
    }

    @Override
    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    @Override
    public List<IGameDataLoader> dataLoaders() {
        return dataLoaders;
    }

    @Override
    public ISessionCreator<S> sessionCreator() {
        return sessionCreator;
    }

    @Override
    public IByteMessageDecoder<M> decoder() {
        return decoder;
    }

    @Override
    public IByteMessageEncoder<M> encoder() {
        return encoder;
    }

    @Override
    public IConnectOpenedHandler<S> connectOpenedOperation() {
        return connectOpened;
    }

    @Override
    public IConnectClosedHandler<S> connectClosedOperation() {
        return connectClosed;
    }

    @Override
    public IConnectExceptionHandler<S> connectExceptionOperation() {
        return connectException;
    }

    @Override
    public List<IMessageInterceptor<S, M>> messageInterceptors() {
        return messageInterceptors;
    }

    @Override
    public IMessageDispatcher<S, M> messageDispatcher() {
        return messageDispatcher;
    }

    @Override
    public IMessageSender<S, M> messageSender() {
        return sender;
    }
}
