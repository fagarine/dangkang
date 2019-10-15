package cn.laoshini.dk.server;

import java.util.concurrent.atomic.AtomicInteger;

import cn.laoshini.dk.constant.GameServerProtocolEnum;
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
    private GameServerConfig serverConfig;

    /**
     * 服务器线程名称
     */
    private String serverThreadName;

    /**
     * 记录在线用户数（有效连接数）
     */
    private final AtomicInteger onlineCount = new AtomicInteger();

    public AbstractGameServer(GameServerConfig serverConfig, String serverThreadName) {
        this.serverConfig = serverConfig;
        this.serverThreadName = serverThreadName + "-" + serverConfig.getId();
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

    public Integer getServerId() {
        return serverConfig.getId();
    }

    public String getGameName() {
        return serverConfig.getName();
    }

    public Integer getPort() {
        return serverConfig.getPort();
    }

    public GameServerConfig getServerConfig() {
        return serverConfig;
    }
}
