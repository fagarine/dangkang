package cn.laoshini.dk.transform;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 类字节码缓存接口
 *
 * @author fagarine
 */
interface IClassByteCodeCache {

    /**
     * 缓存一些常用类的字节码信息
     */
    Map<String, byte[]> BYTE_CODE_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取类名对应类的字节码，先尝试从缓存中查找
     *
     * @param className 类名
     * @return 返回类的字节码
     */
    default byte[] getClassBytes(String className) {
        return BYTE_CODE_CACHE.computeIfAbsent(className, this::classToBytes);
    }

    /**
     * 通过类名查找类，并返回类的字节码数据
     *
     * @param className 类名
     * @return 返回类的字节码
     */
    byte[] classToBytes(String className);
}
