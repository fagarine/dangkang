package cn.laoshini.dk.server;

import java.util.List;

import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.LogUtil;

/**
 * @author fagarine
 */
class GameServerUnPauseWorker implements Runnable {

    private List<Integer> serverIds;

    GameServerUnPauseWorker(List<Integer> serverIds) {
        this.serverIds = serverIds;
    }

    @Override
    public void run() {
        if (CollectionUtil.isEmpty(serverIds)) {
            return;
        }

        for (Integer serverId : serverIds) {
            AbstractGameServer server = GameServers.getServerById(serverId);
            if (server.isRunning()) {
                LogUtil.info("服务器[{}]:[{}]当前未停止对外服务，跳过开放服务操作", server.getServerName(), server.getServerId());
            } else if (server.isPaused()) {
                if (server.isShutdown()) {
                    LogUtil.info("服务器[{}]:[{}]当前已关闭，跳过开放服务操作", server.getServerName(), server.getServerId());
                } else {
                    server.unPauseServer();
                    LogUtil.info("服务器[{}]:[{}]已对外开放服务", server.getServerName(), server.getServerId());
                }
            } else {
                LogUtil.info("服务器[{}]:[{}]当前已关闭，跳过开放服务操作", server.getServerName(), server.getServerId());
            }
        }
    }
}
