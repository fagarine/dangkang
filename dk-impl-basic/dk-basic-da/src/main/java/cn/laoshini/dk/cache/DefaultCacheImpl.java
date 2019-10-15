package cn.laoshini.dk.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import cn.laoshini.dk.annotation.FunctionVariousWays;
import cn.laoshini.dk.cache.impl.DkCacheGuavaImpl;

/**
 * 当康系统实现的应用内缓存池，使用guava缓存实现
 *
 * @author fagarine
 */
@FunctionVariousWays(singleton = false)
public class DefaultCacheImpl<K, V> implements IDkCache<K, V> {

    private DkCacheGuavaImpl<K, V> delegate;

    /**
     * 缓存池最大容量
     */
    private long maxSize;

    /**
     * 数据失效时间，单位：秒
     */
    private int expireTime;

    public DefaultCacheImpl() {
        delegate = DkCacheGuavaImpl.newCache();
        maxSize = delegate.getMaxSize();
        expireTime = delegate.getExpireTime();
    }

    public DefaultCacheImpl(long maxSize, int expireTime) {
        this.maxSize = maxSize;
        this.expireTime = expireTime;
        delegate = DkCacheGuavaImpl.newCache(maxSize, expireTime);
    }

    @Override
    public V get(K key) {
        if (key == null) {
            return null;
        }
        return delegate.get(key);
    }

    @Override
    public void put(K key, V value) {
        delegate.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        delegate.putAll(map);
    }

    @Override
    public void expire(String key, int seconds) {
        delegate.expire(key, seconds);
    }

    @Override
    public long size() {
        return delegate.size();
    }

    @Override
    public ConcurrentMap<K, V> asMap() {
        return delegate.asMap();
    }

    @Override
    public void cleanUp() {
        delegate.cleanUp();
    }

    public long getMaxSize() {
        return maxSize;
    }

    public int getExpireTime() {
        return expireTime;
    }
}
