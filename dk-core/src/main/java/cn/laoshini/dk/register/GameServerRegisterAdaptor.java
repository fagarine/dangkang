package cn.laoshini.dk.register;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.MessageLite;

import cn.laoshini.dk.constant.GameServerProtocolEnum;
import cn.laoshini.dk.constant.ServerType;
import cn.laoshini.dk.domain.GameServerConfig;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.net.codec.INettyMessageDecoder;
import cn.laoshini.dk.net.codec.INettyMessageEncoder;
import cn.laoshini.dk.net.connect.IConnectClosedHandler;
import cn.laoshini.dk.net.connect.IConnectExceptionHandler;
import cn.laoshini.dk.net.connect.IConnectOpenedHandler;
import cn.laoshini.dk.net.msg.IMessageDispatcher;
import cn.laoshini.dk.net.msg.IMessageInterceptor;
import cn.laoshini.dk.net.server.AbstractInnerGameServer;
import cn.laoshini.dk.net.server.InnerGameServerFactory;
import cn.laoshini.dk.net.session.IMessageSender;
import cn.laoshini.dk.net.session.ISessionCreator;
import cn.laoshini.dk.support.ParamSupplier;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.NetUtil;
import cn.laoshini.dk.util.StringUtil;

/**
 * @author fagarine
 */
public class GameServerRegisterAdaptor<S, M> implements IGameServerRegister<S, M> {

    private static final AtomicInteger GAME_ID = new AtomicInteger(1);

    private static final AtomicInteger SERVER_ID = new AtomicInteger(1);
    protected AbstractInnerGameServer<S, M> gameServer;
    protected boolean sharingPortWithGm;
    private ParamSupplier<Integer> gameIdSupplier = ParamSupplier
            .ofDefaultSupplier("游戏ID", Integer.class, GAME_ID::getAndIncrement);
    private ParamSupplier<String> gameNameSupplier = ParamSupplier
            .ofDefaultSupplier("游戏名称", String.class, () -> "game-" + gameId());
    private ParamSupplier<Integer> portSupplier = ParamSupplier.of("游戏端口", Integer.class);
    private ParamSupplier<Integer> serverIdSupplier = ParamSupplier
            .ofDefaultSupplier("游戏服务器ID", Integer.class, SERVER_ID::getAndIncrement);
    private ParamSupplier<String> serverNameSupplier = ParamSupplier
            .ofDefaultSupplier("游戏服务器名称", String.class, () -> gameName() + "-server-" + serverId());
    private GameServerProtocolEnum protocol = GameServerProtocolEnum.TCP;
    private int idleTime;
    private boolean tcpNoDelay;
    private ServerType serverType = ServerType.GAME;
    private List<IGameDataLoader> dataLoaders = new ArrayList<>();
    private ISessionCreator<S> sessionCreator;
    private INettyMessageDecoder<M> decoder;
    private INettyMessageEncoder<M> encoder;
    private IConnectOpenedHandler<S> connectOpened;
    private IConnectClosedHandler<S> connectClosed;
    private IConnectExceptionHandler<S> connectException;
    private List<IMessageInterceptor<S, M>> messageInterceptors = new ArrayList<>();
    private IMessageDispatcher<S, M> messageDispatcher;
    private IMessageSender<S, M> sender;
    private IGameServerStartedHandler serverStartedHandler;
    private IGmServerRegister gmRegister;

    @Override
    public GameServerRegisterAdaptor<S, M> startServer() {
        String gameName = gameName();
        if (StringUtil.isEmptyString(gameName)) {
            throw new IllegalArgumentException("请提供有效的游戏名称:" + gameName);
        }

        int port = portSupplier.get();
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("请提供有效的游戏端口号:" + port);
        }

        if (!NetUtil.localPortAble(port)) {
            throw new IllegalArgumentException("端口号已被占用:" + port);
        }

        // 加载数据
        if (!dataLoaders.isEmpty() && gameServer == null) {
            for (IGameDataLoader loader : dataLoaders) {
                try {
                    LogUtil.debug("开始执行数据加载：" + loader.name());
                    loader.load();
                    LogUtil.debug("数据加载执行完成：" + loader.name());
                } catch (Exception e) {
                    throw new BusinessException("load.data.error", "加载游戏数据出错, game:" + gameName, e);
                }
            }
        }

        // 启动服务器线程
        if (gameServer == null) {
            gameServer = InnerGameServerFactory.newGameServer(this);
        }
        gameServer.start();

        return self();
    }

    /**
     * 服务器是否已启动并处于运行中
     *
     * @return 返回判断结果
     */
    public boolean isStarted() {
        return gameServer != null && !gameServer.isShutdown();
    }

    /**
     * 设置游戏id
     *
     * @param gameId 游戏id
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setGameId(int gameId) {
        gameIdSupplier.setValue(gameId);
        return self();
    }

    /**
     * 通过传入配置项key，获取配置并设置为游戏id（容器启动后才能正确获取到参数）
     *
     * @param propertyKey 配置游戏id的配置项key
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setGameIdByProperty(String propertyKey) {
        gameIdSupplier.setPropertyKey(propertyKey);
        return self();
    }

    /**
     * 通过传入Lambda，获取并设置游戏id（延迟获取，或需要等待容器启动后才能获取到参数的，可以使用这种方式）
     *
     * @param supplier 获取游戏id的Lambda
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setGameIdByLambda(Supplier<Integer> supplier) {
        gameIdSupplier.setSupplier(supplier);
        return self();
    }

    /**
     * 设置游戏名称
     *
     * @param gameName 游戏名
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setGameName(String gameName) {
        gameNameSupplier.setValue(gameName);
        return self();
    }

    /**
     * 通过传入配置项key，获取配置并设置为游戏名称（容器启动后才能正确获取到参数）
     *
     * @param propertyKey 配置游戏名称的配置项key
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setGameNameByProperty(String propertyKey) {
        gameNameSupplier.setPropertyKey(propertyKey);
        return self();
    }

    /**
     * 通过传入Lambda，获取并设置游戏名称（延迟获取，或需要等待容器启动后才能获取到参数的，可以使用这种方式）
     *
     * @param supplier 获取游戏名称的Lambda
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setGameNameByLambda(Supplier<String> supplier) {
        gameNameSupplier.setSupplier(supplier);
        return self();
    }

    /**
     * 设置游戏服务器绑定端口
     *
     * @param bindPort 端口号
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setPort(int bindPort) {
        portSupplier.setValue(bindPort);
        return self();
    }

    /**
     * 通过传入配置项key，获取端口号配置并设置为游戏服务器绑定端口（容器启动后才能正确获取到参数）
     *
     * @param propertyKey 配置端口号的配置项key
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setPortByProperty(String propertyKey) {
        portSupplier.setPropertyKey(propertyKey);
        return self();
    }

    /**
     * 通过传入Lambda，获取并设置游戏服务器绑定端口（延迟获取，或需要等待容器启动后才能获取到参数的，可以使用这种方式）
     *
     * @param supplier 获取端口号的Lambda
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setPortByLambda(Supplier<Integer> supplier) {
        portSupplier.setSupplier(supplier);
        return self();
    }

    /**
     * 设置游戏服务器id
     *
     * @param serverId 服务器id
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setServerId(int serverId) {
        serverIdSupplier.setValue(serverId);
        return self();
    }

    /**
     * 通过传入配置项key，获取配置并设置为游戏服务器id（容器启动后才能正确获取到参数）
     *
     * @param propertyKey 配置游戏服务器id的配置项key
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setServerIdByProperty(String propertyKey) {
        serverIdSupplier.setPropertyKey(propertyKey);
        return self();
    }

    /**
     * 通过传入Lambda，获取并设置游戏服务器id（延迟获取，或需要等待容器启动后才能获取到参数的，可以使用这种方式）
     *
     * @param supplier 获取游戏服务器id的Lambda
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setServerIdByLambda(Supplier<Integer> supplier) {
        serverIdSupplier.setSupplier(supplier);
        return self();
    }

    /**
     * 设置游戏服务器名称
     *
     * @param serverName 服务器名称
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setServerName(String serverName) {
        serverNameSupplier.setValue(serverName);
        return self();
    }

    /**
     * 通过传入配置项key，获取配置并设置为游戏服务器名称（容器启动后才能正确获取到参数）
     *
     * @param propertyKey 配置游戏服务器名称的配置项key
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setServerNameByProperty(String propertyKey) {
        serverNameSupplier.setPropertyKey(propertyKey);
        return self();
    }

    /**
     * 通过传入Lambda，获取并设置游戏服务器名称（延迟获取，或需要等待容器启动后才能获取到参数的，可以使用这种方式）
     *
     * @param supplier 获取游戏服务器名称的Lambda
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setServerNameByLambda(Supplier<String> supplier) {
        serverNameSupplier.setSupplier(supplier);
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
     * 设置游戏服使用HTTP协议通信
     *
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> http() {
        this.protocol = GameServerProtocolEnum.HTTP;
        return self();
    }

    /**
     * 设置判定连接为空闲的时间（超过该时间没有响应，将断开连接），仅对TCP、Websocket协议有效
     *
     * @param idleSeconds 秒
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setIdleTime(int idleSeconds) {
        this.idleTime = idleSeconds;
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
     * 设置服务器类型
     *
     * @param serverType 服务器类型枚举
     * @return 返回当前对象
     */
    protected GameServerRegisterAdaptor<S, M> setServerType(ServerType serverType) {
        this.serverType = serverType;
        return self();
    }

    /**
     * 设置GM服务器注册对象
     *
     * @param gmRegister GM服务器注册对象
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setGmServerRegister(IGmServerRegister gmRegister) {
        this.gmRegister = gmRegister;
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
    public GameServerRegisterAdaptor<S, M> setMessageDecode(INettyMessageDecoder<M> decoder) {
        this.decoder = decoder;
        return self();
    }

    /**
     * 设置消息编码前
     *
     * @param encoder 消息编码器
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> setMessageEncode(INettyMessageEncoder<M> encoder) {
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
        //        this.decoder = (IByteMessageDecoder<M>) IByteMessageDecoder.newProtobufDecoder(prototype, extensionRegistry);
        //        this.encoder = (IByteMessageEncoder<M>) IByteMessageEncoder.newProtobufEncoder();
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
     * 设置游戏服务器线程启动成功后的逻辑，该逻辑由游戏服线程执行
     *
     * @param serverStartedHandler 游戏启动后逻辑
     * @return 返回当前对象
     */
    public GameServerRegisterAdaptor<S, M> onServerStarted(IGameServerStartedHandler serverStartedHandler) {
        this.serverStartedHandler = serverStartedHandler;
        return self();
    }

    /**
     * 将配置信息转换为{@link GameServerConfig}对象返回
     *
     * @return 该方法不会返回null
     */
    public GameServerConfig toGameServerConfig() {
        return GameServerConfig.builder().gameId(gameId()).gameName(gameName()).serverId(serverId())
                .serverName(serverName()).port(port()).serverType(serverType).protocol(protocol).idleTime(idleTime)
                .tcpNoDelay(tcpNoDelay).build();
    }

    private GameServerRegisterAdaptor<S, M> self() {
        return this;
    }

    @Override
    public int gameId() {
        return gameIdSupplier.get();
    }

    @Override
    public String gameName() {
        return gameNameSupplier.get();
    }

    @Override
    public int serverId() {
        return serverIdSupplier.get();
    }

    @Override
    public String serverName() {
        return serverNameSupplier.get();
    }

    @Override
    public int port() {
        return portSupplier.get();
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
    public ServerType serverType() {
        return serverType;
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
    public INettyMessageDecoder<M> decoder() {
        return decoder;
    }

    @Override
    public INettyMessageEncoder<M> encoder() {
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

    @Override
    public IGameServerStartedHandler serverStartedHandler() {
        return serverStartedHandler;
    }

    /**
     * 判断服务器是否GM服务器
     *
     * @return 返回判断结果
     */
    public boolean isGmServer() {
        return ServerType.GM.equals(serverType());
    }

    public IGmServerRegister gmRegister() {
        return gmRegister;
    }

    public boolean isSharingPortWithGm() {
        return sharingPortWithGm;
    }
}
