package cn.laoshini.dk.cache;

import java.util.concurrent.ConcurrentHashMap;

import cn.laoshini.dk.exception.CacheException;
import cn.laoshini.dk.util.LogUtil;

/**
 * 缓存池容器管理
 *
 * @author fagarine
 */
public class CacheContainer {

    private static ConcurrentHashMap<String, IDkCache> caches = new ConcurrentHashMap<>();

    private static IDkCache getCache(String cacheKey) {
        if (cacheKey == null) {
            return null;
        }
        return caches.get(cacheKey);
    }

    public static void putCache(String cacheKey, IDkCache cache) {
        if (cacheKey == null) {
            String message = String.format("缓存池的key不能为空, cache:%s", cache.getClass());
            LogUtil.error(message);
            throw new CacheException("cache.key.null", message);
        }

        caches.put(cacheKey, cache);
    }
}
