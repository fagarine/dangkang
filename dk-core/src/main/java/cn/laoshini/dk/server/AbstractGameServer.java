package cn.laoshini.dk.server;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import cn.laoshini.dk.constant.GameServerProtocolEnum;
import cn.laoshini.dk.constant.ServerType;
import cn.laoshini.dk.domain.GameServerConfig;
import cn.laoshini.dk.util.LogUtil;

/**
 * 游戏服务器抽象类
 *
 * @author fagarine
 */
public abstract class AbstractGameServer extends AbstractServer {

    /**
     * 游戏服务器配置信息
     */
    private final GameServerConfig serverConfig;

    /**
     * 服务器线程名称
     */
    private final String serverThreadName;

    /**
     * 记录在线用户数（有效连接数）
     */
    private final AtomicInteger onlineCount = new AtomicInteger();

    /**
     * 如果服务器当前不对外开放，提示信息
     */
    private String tips;

    /**
     * 如果服务器当前不对外开放，服务器预计对外开放时间
     */
    private Date openTime;

    public AbstractGameServer(GameServerConfig serverConfig, String serverThreadName) {
        this.serverConfig = serverConfig;
        this.serverThreadName = serverThreadName + "-" + serverConfig.getServerId();
    }

    @Override
    public void shutdown() {
        if (!shutdown.get()) {
            this.shutdown.set(true);

            LogUtil.info("游戏服 [{}]，线程 [{}] 开始关闭...", getGameName(), getServerThreadName());
            shutdown0();
            LogUtil.info("游戏服 [{}]，线程 [{}] 关闭完成", getGameName(), getServerThreadName());
        }
    }

    /**
     * 执行游戏线程自己的关闭逻辑
     */
    protected abstract void shutdown0();

    /**
     * 返回游戏服通信协议类型
     *
     * @return 刚方法并允许返回null
     */
    public abstract GameServerProtocolEnum getProtocolType();

    @Override
    protected String getServerThreadName() {
        return serverThreadName;
    }

    /**
     * 在线数加一
     */
    protected void incrementOnline() {
        onlineCount.incrementAndGet();
    }

    /**
     * 在线数减一
     */
    protected void decrementOnline() {
        onlineCount.decrementAndGet();
    }

    /**
     * 返回当前在线数
     *
     * @return 当前在线数
     */
    public int getOnlineCount() {
        return onlineCount.get();
    }

    void pauseServer(String tips, Date openTime) {
        pauseServer();
        this.tips = tips;
        this.openTime = openTime;
    }

    @Override
    protected void unPauseServer() {
        super.unPauseServer();
        this.tips = null;
        this.openTime = null;
    }

    /**
     * 启动服务器
     */
    protected void startServer() {
        start();
    }

    public int getGameId() {
        return serverConfig.getGameId();
    }

    public String getGameName() {
        return serverConfig.getGameName();
    }

    public int getServerId() {
        return serverConfig.getServerId();
    }

    public String getServerName() {
        return serverConfig.getServerName();
    }

    public int getPort() {
        return serverConfig.getPort();
    }

    public GameServerConfig getServerConfig() {
        GameServerConfig config = serverConfig;
        return GameServerConfig.builder().serverId(config.getServerId()).serverName(config.getServerName())
                .gameName(config.getGameName()).port(config.getPort()).protocol(config.getProtocol())
                .idleTime(config.getIdleTime()).tcpNoDelay(config.isTcpNoDelay()).build();
    }

    public String getTips() {
        return tips;
    }

    public Date getOpenTime() {
        return openTime;
    }

    public boolean isGmServer() {
        return ServerType.GM.equals(serverConfig.getServerType());
    }
}
