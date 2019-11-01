package cn.laoshini.dk.common;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import cn.laoshini.dk.annotation.ResourceHolder;
import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.StringUtil;

/**
 * 资源管理中心（只相当于运行时缓存使用，且不停机不失效）
 *
 * @author fagarine
 */
@ResourceHolder
public enum ResourcesHolder {
    /**
     * 枚举单例
     */
    INSTANCE;

    private final ConcurrentMap<Object, Object> resources = new ConcurrentHashMap<>();

    private static ConcurrentMap<Object, Object> getResources() {
        return INSTANCE.resources;
    }

    public static void put(Object key, Object value) {
        getResources().put(key, value);
    }

    public static boolean contains(Object key) {
        return getResources().containsKey(key);
    }

    public static <T> T get(Object key) {
        return (T) getResources().get(key);
    }

    public static <T> T remove(Object key) {
        return (T) getResources().remove(key);
    }

    public static void setPackagePrefixes(Collection<String> packagePrefixes) {
        put(Constants.PACKAGE_PREFIXES_RESOURCE_KEY, packagePrefixes);
    }

    public static String[] addPackagePrefixes(String[] packagePrefixes) {
        Collection<String> prefixes = get(Constants.PACKAGE_PREFIXES_RESOURCE_KEY);
        if (prefixes == null) {
            prefixes = new LinkedHashSet<>();
            put(Constants.PACKAGE_PREFIXES_RESOURCE_KEY, prefixes);
        }
        if (CollectionUtil.isNotEmpty(packagePrefixes)) {
            for (String packagePrefix : packagePrefixes) {
                if (StringUtil.isNotEmptyString(packagePrefix)) {
                    prefixes.add(packagePrefix.trim());
                }
            }
        }
        return prefixes.toArray(new String[0]);
    }

    public static Collection<String> getPackagePrefixes() {
        return get(Constants.PACKAGE_PREFIXES_RESOURCE_KEY);
    }

    public static String[] getPackagePrefixesAsArray() {
        Collection<String> prefixes = get(Constants.PACKAGE_PREFIXES_RESOURCE_KEY);
        if (prefixes == null) {
            return new String[0];
        }

        return prefixes.toArray(new String[0]);
    }

    public static void setSpringLocations(String[] springLocations) {
        put(Constants.SPRING_LOCATIONS_RESOURCE_KEY, springLocations);
    }

    public static String[] getSpringLocations() {
        return get(Constants.SPRING_LOCATIONS_RESOURCE_KEY);
    }

    public static void setStartArgs(String[] startArgs) {
        put(Constants.START_ARGS_RESOURCE_KEY, startArgs);
    }

    public static String[] getStartArgs() {
        return get(Constants.START_ARGS_RESOURCE_KEY);
    }

    public static void setPropertyLocations(String[] propertyLocations) {
        put(Constants.PROPERTY_RESOURCE_KEY, propertyLocations);
    }

    public static String[] getPropertyLocations() {
        return get(Constants.PROPERTY_RESOURCE_KEY);
    }
}
