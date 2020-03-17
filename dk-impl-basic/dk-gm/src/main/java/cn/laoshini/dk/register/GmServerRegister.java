package cn.laoshini.dk.register;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;

import cn.laoshini.dk.constant.GameCodeEnum;
import cn.laoshini.dk.constant.GameConstant;
import cn.laoshini.dk.constant.GameServerProtocolEnum;
import cn.laoshini.dk.constant.ServerType;
import cn.laoshini.dk.domain.dto.GameServerInfoDTO;
import cn.laoshini.dk.domain.dto.GameServerRegisterDTO;
import cn.laoshini.dk.domain.msg.RespMessage;
import cn.laoshini.dk.server.GameServers;
import cn.laoshini.dk.support.ParamSupplier;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.HttpUtil;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.StringUtil;

/**
 * GM服务器注册器
 *
 * @author fagarine
 */
public class GmServerRegister<S, M> extends GameServerRegisterAdaptor<S, M> implements IGmServerRegister<S, M> {

    private static final AtomicInteger GM_SERVER_ID_INCREMENTER = new AtomicInteger(GameConstant.GM_SERVER_ID_BEGIN);

    /**
     * 后台服务器URL
     */
    private String serverRegisterUrl;

    /**
     * 后台消息过滤器，仅在{@link #sharingPortWithGm}为true时有效
     */
    private Predicate<M> consoleMessageFilter;

    /**
     * 记录收GM服务器管理的游戏服id
     */
    private Set<Integer> managedServerIds = new HashSet<>();

    /**
     * 记录供应受管理游戏服id的供应器
     */
    private List<ParamSupplier<int[]>> managedServerIdSuppliers = new LinkedList<>();

    /**
     * GM游戏服向后台管理服务器注册操作逻辑，该逻辑将在GM服务器启动成功后执行
     */
    private Predicate<List<GameServerInfoDTO>> serverRegisterOperation = (serverInfos) -> {
        GameServerRegisterDTO dto = new GameServerRegisterDTO();
        dto.setGameId(gameId());
        dto.setGameName(gameName());
        Map<Integer, String> servers = new HashMap<>(serverInfos.size());
        for (GameServerInfoDTO serverInfo : serverInfos) {
            servers.put(serverInfo.getServerId(), serverInfo.getServerName());
        }
        dto.setServers(servers);
        try {
            String result = HttpUtil.sendJsonPost(serverRegisterUrl, JSON.toJSONString(dto));
            if (StringUtil.isNotEmptyString(result)) {
                RespMessage resp = JSON.parseObject(result, RespMessage.class);
                if (GameCodeEnum.OK.getCode() == resp.getCode()) {
                    return true;
                }
            }
        } catch (Exception e) {
            LogUtil.error("向后台管理服务器发送GM服务器注册消息出错", e);
        }
        return false;
    };

    private GmServerRegister() {
        setServerType(ServerType.GM);
        setServerId(GM_SERVER_ID_INCREMENTER.getAndIncrement());
        setServerName("GM-SERVER-" + serverId());
        // GM服务器默认使用HTTP协议，GM消息一般频次很低，长连接太浪费资源（尤其是请求方保持长连接太浪费资源，请求方一般为后台服务器）
        setProtocol(GameServerProtocolEnum.HTTP);
    }

    /**
     * 创建一个注册GM服务器的空注册对象
     *
     * @param <S> 会话类型
     * @param <M> 消息类型
     * @return 返回GM服务器注册对象
     */
    public static <S, M> GmServerRegister<S, M> create() {
        return new GmServerRegister<>();
    }

    /**
     * 根据传入的游戏服注册器对象，生成GM服务器注册对象
     *
     * @param register 游戏服注册对象
     * @param port GM服务器监听端口号
     * @param <S> 会话类型
     * @param <M> 消息类型
     * @return 返回GM服务器注册对象
     */
    public static <S, M> GmServerRegister<S, M> of(IGameServerRegister<S, M> register, int port) {
        GmServerRegister<S, M> gmRegister = new GmServerRegister<>();
        gmRegister.setPort(port);
        copy(register, gmRegister);

        return gmRegister;
    }

    /**
     * 根据传入的游戏服注册器对象，生成GM服务器注册对象（端口号通过配置项参数提供）
     *
     * @param register 游戏服注册对象
     * @param portPropertyKey GM服务器监听端口号的配置项key
     * @param <S> 会话类型
     * @param <M> 消息类型
     * @return 返回GM服务器注册对象
     */
    public static <S, M> GmServerRegister<S, M> of(IGameServerRegister<S, M> register, String portPropertyKey) {
        GmServerRegister<S, M> gmRegister = new GmServerRegister<>();
        gmRegister.setPortByProperty(portPropertyKey);
        copy(register, gmRegister);

        return gmRegister;
    }

    /**
     * 根据传入的游戏服注册器对象，生成GM服务器注册对象（端口号通过Lambda表达式提供）
     *
     * @param register 游戏服注册对象
     * @param portSupplier GM服务器监听端口号的供应器
     * @param <S> 会话类型
     * @param <M> 消息类型
     * @return 返回GM服务器注册对象
     */
    public static <S, M> GmServerRegister<S, M> of(IGameServerRegister<S, M> register, Supplier<Integer> portSupplier) {
        GmServerRegister<S, M> gmRegister = new GmServerRegister<>();
        gmRegister.setPortByLambda(portSupplier);
        copy(register, gmRegister);

        return gmRegister;
    }

    /**
     * 创建一个与传入的游戏服注册，共享游戏服线程和端口的GM服务器注册对象
     *
     * @param register 游戏服注册对象
     * @param consoleMessageFilter 后台消息过滤器，当游戏服暂停对外服务时，只有通过该过滤器的消息可以进入Handler（通过过滤器的消息被认为是来自后台服务器的消息）
     * @param <S> 会话类型
     * @param <M> 消息类型
     * @return 返回GM服务器注册对象
     */
    public static <S, M> GmServerRegister<S, M> createByShareServer(GameServerRegisterAdaptor<S, M> register,
            Predicate<M> consoleMessageFilter) {
        GmServerRegister<S, M> gmRegister = new GmServerRegister<>();
        copy(register, gmRegister);
        gmRegister.sharingPortWithGm = true;
        gmRegister.gameServer = register.gameServer;
        gmRegister.consoleMessageFilter = consoleMessageFilter;
        register.sharingPortWithGm = true;
        register.setGmServerRegister(gmRegister);
        return gmRegister;
    }

    private static <S, M> void copy(IGameServerRegister<S, M> register, GmServerRegister<S, M> gmRegister) {
        gmRegister.addManagedServerId(register::serverId);
        gmRegister.setGameIdByLambda(register::gameId);
        gmRegister.setGameNameByLambda(register::gameName);
        gmRegister.setProtocol(register.protocol());
        gmRegister.setIdleTime(register.idleTime());
        gmRegister.setMessageDecode(register.decoder());
        gmRegister.setMessageEncode(register.encoder());
        gmRegister.onMessageSend(register.messageSender());
        gmRegister.setSessionCreator(register.sessionCreator());
        gmRegister.onConnected(register.connectOpenedOperation());
        gmRegister.onServerStarted(register.serverStartedHandler());
        gmRegister.onMessageDispatcher(register.messageDispatcher());
        gmRegister.onDisconnected(register.connectClosedOperation());
        gmRegister.onConnectException(register.connectExceptionOperation());
    }

    @Override
    public GmServerRegister<S, M> startServer() {
        if (!sharingPortWithGm) {
            super.startServer();
        }

        for (ParamSupplier<int[]> supplier : managedServerIdSuppliers) {
            int[] ids = supplier.get();
            if (ids != null && ids.length > 0) {
                for (int id : ids) {
                    managedServerIds.add(id);
                }
            }
        }
        managedServerIdSuppliers.clear();
        LogUtil.info("受[{}]管理的游戏服id:{}", serverName(), managedServerIds);

        if (StringUtil.isNotEmptyString(serverRegisterUrl)) {
            // 发送GM服务器注册消息
            sendGmServerRegister();
        }

        return this;
    }

    /**
     * 发送GM服务器注册消息到后台管理服务器
     */
    protected void sendGmServerRegister() {
        List<GameServerInfoDTO> serverInfos = GameServers
                .getServerInfos(server -> managedServerIds.contains(server.getServerId()));

        boolean success = serverRegisterOperation.test(serverInfos);
        if (success) {
            LogUtil.info("GM服务器已成功注册到后台管理服务器");
        } else {
            LogUtil.error("向后台管理服务器注册GM服务器失败");
        }
    }

    /**
     * 添加受本GM服务器管理的游戏服id
     *
     * @param ids 游戏服id
     */
    public GmServerRegister<S, M> addManagedServerId(Integer... ids) {
        managedServerIds.addAll(Sets.newHashSet(ids));
        return this;
    }

    /**
     * 批量添加受本GM服务器管理的游戏服id
     *
     * @param ids 游戏服id集合
     */
    public GmServerRegister<S, M> addManagedServerIds(Collection<Integer> ids) {
        if (CollectionUtil.isNotEmpty(ids)) {
            managedServerIds.addAll(ids);
        }
        return this;
    }

    /**
     * 添加受本GM服务器管理的游戏服id（延迟获取，或需要等待容器启动后才能获取到参数的，可以使用这种方式）
     *
     * @param serverIdSupplier 游戏服id供应器
     */
    public GmServerRegister<S, M> addManagedServerId(final IntSupplier serverIdSupplier) {
        managedServerIdSuppliers.add(ParamSupplier.ofDefaultSupplier("受GM管理服务器ID", int[].class, () -> {
            int serverId = serverIdSupplier.getAsInt();
            return new int[] { serverId };
        }));
        return this;
    }

    /**
     * 批量添加受本GM服务器管理的游戏服id（延迟获取，或需要等待容器启动后才能获取到参数的，可以使用这种方式）
     *
     * @param idsSupplier 游戏服id供应器
     */
    public GmServerRegister<S, M> addManagedServerIds(Supplier<int[]> idsSupplier) {
        managedServerIdSuppliers.add(ParamSupplier.ofDefaultSupplier("受GM管理服务器ID", int[].class, idsSupplier));
        return this;
    }

    @Override
    public String getServerRegisterUrl() {
        return this.serverRegisterUrl;
    }

    /**
     * 设置后台服务器URL，GM服务器启动后将会向后台服发送注册消息，需要注意的是：
     * <p>
     * 如果用户没有调用{@link #setServerRegisterOperation(Predicate)}设置GM服务器注册逻辑，系统将会使用默认的注册逻辑
     * </p>
     *
     * @param serverRegisterUrl 后台服务器URL
     */
    public GmServerRegister<S, M> setServerRegisterUrl(String serverRegisterUrl) {
        this.serverRegisterUrl = serverRegisterUrl;
        return this;
    }

    @Override
    public Predicate<List<GameServerInfoDTO>> getServerRegisterOperation() {
        return this.serverRegisterOperation;
    }

    /**
     * 设置GM游戏服向后台管理服务器注册操作
     *
     * @param serverRegisterOperation 具体逻辑
     */
    public GmServerRegister<S, M> setServerRegisterOperation(
            Predicate<List<GameServerInfoDTO>> serverRegisterOperation) {
        this.serverRegisterOperation = serverRegisterOperation;
        return this;
    }

    @Override
    public Predicate<M> getConsoleMessageFilter() {
        return consoleMessageFilter;
    }

    /**
     * 设置后台消息过滤器（仅在游戏服暂停对外服务时使用）
     *
     * @param consoleMessageFilter 后台消息过滤器，当游戏服暂停对外服务时，只有通过该过滤器的消息可以进入Handler（通过过滤器的消息被认为是来自后台服务器的消息）
     */
    public GmServerRegister<S, M> setConsoleMessageFilter(Predicate<M> consoleMessageFilter) {
        this.consoleMessageFilter = consoleMessageFilter;
        return this;
    }

}
