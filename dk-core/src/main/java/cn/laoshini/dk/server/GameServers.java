package cn.laoshini.dk.server;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import cn.laoshini.dk.annotation.ResourceHolder;
import cn.laoshini.dk.constant.GameServerStatus;
import cn.laoshini.dk.domain.dto.GameServerInfoDTO;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.util.LogUtil;

/**
 * @author fagarine
 */
@ResourceHolder
public enum GameServers {
    /**
     * 使用枚举实现单例
     */
    INSTANCE;

    private static final ThreadFactory THREAD_FACTORY = new BasicThreadFactory.Builder()
            .namingPattern("dk-server-timer-pool-%d")
            .uncaughtExceptionHandler((t, e) -> LogUtil.error(String.format("定时任务[%s]执行出错", t.getName()), e)).build();

    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = new ScheduledThreadPoolExecutor(2,
            THREAD_FACTORY);

    private Map<Integer, AbstractGameServer> idToServer = new ConcurrentHashMap<>();

    private Map<Integer, AbstractGameServer> idToGmServer = new ConcurrentHashMap<>();

    public static GameServers getInstance() {
        return INSTANCE;
    }

    public static void putServer(AbstractGameServer server) {
        if (server.isGmServer()) {
            getInstance().idToGmServer.put(server.getServerId(), server);
        } else {
            checkServerIdDuplicated(server.getServerId(), server.getServerName());
            getInstance().idToServer.put(server.getServerId(), server);
        }
    }

    public static void checkServerIdDuplicated(int serverId, String serverName) {
        if (containsRunningServer(serverId)) {
            AbstractGameServer origin = getServerById(serverId);
            String format = String
                    .format("游戏服id重复, id:[%d], 已有服务器:[%s], 新服务器:[%s]", serverId, origin.getServerName(), serverName);
            throw new BusinessException("server.id.duplicated", format);
        }
    }

    public static Collection<Integer> getAllServerId() {
        return getInstance().idToServer.keySet();
    }

    public static AbstractGameServer getServerById(int id) {
        return getInstance().idToServer.get(id);
    }

    public static boolean containsServer(int serverId) {
        return INSTANCE.idToServer.containsKey(serverId);
    }

    public static boolean containsRunningServer(int serverId) {
        AbstractGameServer server = getServerById(serverId);
        return server != null && server.isRunning();
    }

    public static List<GameServerInfoDTO> getServerInfos(Predicate<AbstractGameServer> filter) {
        List<GameServerInfoDTO> infos = new LinkedList<>();
        for (AbstractGameServer server : INSTANCE.idToServer.values()) {
            if (filter != null && !filter.test(server)) {
                continue;
            }

            infos.add(serverToInfo(server));
        }
        return infos;
    }

    /**
     * 暂停游戏服务器业务线程
     */
    public static void pauseServers(String tips, Date openTime, Predicate<AbstractGameServer> filter) {
        long delay = openTime == null ? 0 : openTime.getTime() - System.currentTimeMillis();
        boolean needSchedule = delay > 0;
        List<Integer> serverIds = new LinkedList<>();
        for (AbstractGameServer server : INSTANCE.idToServer.values()) {
            if (filter == null || filter.test(server)) {
                server.pauseServer(tips, openTime);
                if (needSchedule) {
                    serverIds.add(server.getServerId());
                }
            }
        }

        // 开启定时开放的任务
        if (needSchedule && !serverIds.isEmpty()) {
            SCHEDULED_EXECUTOR.schedule(new GameServerUnPauseWorker(serverIds), delay, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 取消游戏服务器业务线程的暂停状态
     */
    public static void releaseServers(Predicate<AbstractGameServer> filter) {
        for (AbstractGameServer server : INSTANCE.idToServer.values()) {
            if (filter == null || filter.test(server)) {
                server.unPauseServer();
            }
        }
    }

    public static void pauseById(int serverId, String tips, Date openTime) {
        AbstractGameServer server = INSTANCE.idToServer.get(serverId);
        if (server != null) {
            server.pauseServer(tips, openTime);
            long delay = openTime == null ? 0 : openTime.getTime() - System.currentTimeMillis();
            if (delay > 0) {
                SCHEDULED_EXECUTOR.schedule(new GameServerUnPauseWorker(Lists.newArrayList(serverId)), delay,
                        TimeUnit.MILLISECONDS);
            }
        }
    }

    public static void releaseById(int serverId) {
        AbstractGameServer server = INSTANCE.idToServer.get(serverId);
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * 关闭游戏服务器业务线程
     */
    public static void shutdownServers(Predicate<AbstractGameServer> filter) {
        for (AbstractGameServer server : INSTANCE.idToServer.values()) {
            if (filter == null || filter.test(server)) {
                server.shutdown();
            }
        }
    }

    /**
     * 关闭游戏服务器业务线程
     */
    public static void shutdownServerById(int serverId) {
        AbstractGameServer server = INSTANCE.idToServer.get(serverId);
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * 启动游戏服务器业务线程
     */
    public static void startupServers(Predicate<AbstractGameServer> filter) {
        for (AbstractGameServer server : INSTANCE.idToServer.values()) {
            if (filter == null || filter.test(server)) {
                if (server.isShutdown()) {
                    server.startServer();
                }
            }
        }
    }

    /**
     * 启动游戏服务器业务线程
     */
    public static void startupServerById(int serverId) {
        AbstractGameServer server = INSTANCE.idToServer.get(serverId);
        if (server != null) {
            server.startServer();
        }
    }

    private static GameServerInfoDTO serverToInfo(AbstractGameServer server) {
        GameServerInfoDTO info = new GameServerInfoDTO();
        info.setGameId(server.getGameId());
        info.setGameName(server.getGameName());
        info.setServerId(server.getServerId());
        info.setServerName(server.getServerName());
        info.setPort(server.getPort());
        info.setProtocol(server.getProtocolType().name());
        if (server.isRunning()) {
            info.setStatus(GameServerStatus.RUN.getCode());
        } else if (server.isShutdown()) {
            info.setStatus(GameServerStatus.CLOSE.getCode());
        } else {
            info.setStatus(GameServerStatus.PAUSE.getCode());
            info.setTips(server.getTips());
            info.setOpenTime(server.getOpenTime());
        }

        info.setStartTime(new Date(server.getStartTime()));
        return info;
    }
}
