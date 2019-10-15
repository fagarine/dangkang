package cn.laoshini.dk.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import cn.laoshini.dk.annotation.ConfigurableFunction;

/**
 * 当康系统公用缓存池接口定义
 *
 * @author fagarine
 */
@ConfigurableFunction(key = "dk.cache.dao")
public interface IDkCache<K, V> {

    /**
     * 从缓存中查找值
     *
     * @param key key
     * @return 返回查找结果
     */
    V get(K key);

    /**
     * 设置缓存的值
     *
     * @param key key
     * @param value value
     */
    void put(K key, V value);

    /**
     * 将Map中的所有数据存入缓存中
     *
     * @param map 键值对
     */
    void putAll(Map<? extends K, ? extends V> map);

    /**
     * 设置失效时长
     *
     * @param key key
     * @param seconds 失效时长，单位：秒
     */
    void expire(String key, int seconds);

    /**
     * 当前缓存池数据长度
     *
     * @return 返回数据长度
     */
    long size();

    /**
     * 以Map形式返回缓存池中的所有数据
     *
     * @return 以Map形式返回缓存池中的所有数据
     */
    ConcurrentMap<K, V> asMap();

    /**
     * 清空所有数据
     */
    void cleanUp();

}
