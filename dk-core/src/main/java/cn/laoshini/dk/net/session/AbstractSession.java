package cn.laoshini.dk.net.session;

import java.util.HashMap;
import java.util.Map;

import cn.laoshini.dk.util.CollectionUtil;

/**
 * 当康系统网络连接会话对象抽象类
 *
 * @param <T> 连接通道类型
 * @author fagarine
 */
public abstract class AbstractSession<T> {

    private static final String SESSION_ID_KEY = "SESSION ID";

    private static final String HTTP_CONNECT_KEY = "HTTP CONNECT";

    private static final String HTTP_KEEP_ALIVE_KEY = "HTTP KEEP ALIVE";

    private Map<String, Object> keyToAttrs = new HashMap<>();

    protected T channel;

    public AbstractSession(T channel) {
        this.channel = channel;
    }

    /**
     * 获取连接对象的ip
     *
     * @return 连接对象的ip
     */
    public abstract String getIp();

    /**
     * 是否已连接成功
     *
     * @return 是否已连接成功
     */
    public abstract boolean isConnect();

    /**
     * 关闭连接
     */
    public abstract void close();

    /**
     * 发送消息
     *
     * @param message 消息内容
     */
    public abstract void sendMessage(Object message);

    public T getChannel() {
        return channel;
    }

    public Object getAttr(String key) {
        return keyToAttrs.get(key);
    }

    public void addFlag(String key) {
        keyToAttrs.put(key, "");
    }

    public boolean containsAttr(String key) {
        return keyToAttrs.containsKey(key);
    }

    public void add(String key, Object v) {
        keyToAttrs.put(key, v);
    }

    public void remove(String key) {
        keyToAttrs.remove(key);
    }

    public void putAttrs(Map<String, Object> attrs) {
        if (CollectionUtil.isNotEmpty(attrs)) {
            keyToAttrs.putAll(attrs);
        }
    }

    public void clear() {
        keyToAttrs.clear();
    }

    public void setId(long id) {
        keyToAttrs.put(SESSION_ID_KEY, id);
    }

    public Long getId() {
        return containsAttr(SESSION_ID_KEY) ? (Long) keyToAttrs.get(SESSION_ID_KEY) : 0L;
    }

    public void setHttpConnect(boolean isKeepAlive) {
        addFlag(HTTP_CONNECT_KEY);
        if (isKeepAlive) {
            addFlag(HTTP_KEEP_ALIVE_KEY);
        }
    }

    public boolean isHttpConnect() {
        return containsAttr(HTTP_CONNECT_KEY);
    }

    public boolean isHttpKeepAlive() {
        return containsAttr(HTTP_KEEP_ALIVE_KEY);
    }
}
