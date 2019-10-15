package cn.laoshini.dk.starter;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.laoshini.dk.annotation.ResourceHolder;
import cn.laoshini.dk.server.AbstractGameServer;

/**
 * @author fagarine
 */
@ResourceHolder
public enum GameServers {
    /**
     * 使用枚举实现单例
     */
    INSTANCE;

    private Map<Integer, AbstractGameServer> idToServers = new ConcurrentHashMap<>();

    public static GameServers getInstance() {
        return INSTANCE;
    }

    public static void putServer(AbstractGameServer server) {
        getInstance().idToServers.put(server.getServerId(), server);
    }

    public static AbstractGameServer getServer(int id) {
        return getInstance().idToServers.get(id);
    }

    public static Collection<Integer> getAllServerId() {
        return getInstance().idToServers.keySet();
    }
}
