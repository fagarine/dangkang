package cn.laoshini.dk.cache.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import cn.laoshini.dk.cache.IDkCache;

/**
 * 极简缓存池实现类，使用guava缓存池实现
 *
 * @author fagarine
 */
public class DkCacheGuavaImpl<K, V> implements IDkCache<K, V> {

    private static final long DEFAULT_MAX_SIZE = 2 << 10;

    private static final int DEFAULT_EXPIRE_SEC = 3600;

    /**
     * 缓存池最大容量
     */
    private long maxSize;

    /**
     * 数据失效时间，单位：秒
     */
    private int expireTime;

    private Cache<K, V> delegate;

    private DkCacheGuavaImpl() {
    }

    public static <K, V> DkCacheGuavaImpl<K, V> newCache() {
        return newCache(DEFAULT_MAX_SIZE, DEFAULT_EXPIRE_SEC);
    }

    public static <K, V> DkCacheGuavaImpl<K, V> newCache(long maxSize, int expireTime) {
        DkCacheGuavaImpl<K, V> cache = new DkCacheGuavaImpl<>();
        cache.maxSize = maxSize;
        cache.expireTime = expireTime;
        cache.delegate = CacheBuilder.newBuilder().maximumSize(maxSize).expireAfterAccess(expireTime, TimeUnit.SECONDS)
                .build();
        return cache;
    }

    @Override
    public V get(K key) {
        if (key == null) {
            return null;
        }
        return delegate.getIfPresent(key);
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
        // 该实现类不具有单独对key设置失效时间的功能
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
