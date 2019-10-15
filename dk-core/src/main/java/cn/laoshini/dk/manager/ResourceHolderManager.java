package cn.laoshini.dk.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.laoshini.dk.annotation.ResourceHolder;
import cn.laoshini.dk.module.loader.ModuleClassLoader;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.ModuleResourceUtil;

/**
 * 资源持有者（用于模块热更新）的管理类；这里选择单例，而非Spring托管，是为了降低容器依赖
 *
 * @author fagarine
 * @see cn.laoshini.dk.annotation.ResourceHolder
 * @see cn.laoshini.dk.module.registry.ModuleResourceHolderRegistry
 */
@ResourceHolder
public enum ResourceHolderManager {
    /**
     * 使用枚举实现单例
     */
    INSTANCE;

    /**
     * 记录模块热更时，旧模块中的资源持有者信息
     */
    private final Map<String, Object> oldHolderCache = new ConcurrentHashMap<>();

    /**
     * 记录模块中被ResourceHolder标记的类, key: holderKey
     */
    private final Map<String, Object> holderMap = new ConcurrentHashMap<>();

    /**
     * 模块热更时，暂时记录模块内的自由持有者对象
     */
    private final Map<ClassLoader, Set<Object>> moduleHolderMap = new ConcurrentHashMap<>();

    public static void registerHolder(String holderKey, Object holder) {
        INSTANCE.holderMap.put(holderKey, holder);

        Object oldHolder = getOldHolder(holderKey);
        if (oldHolder != null) {
            // 拷贝数据
            if (holder instanceof Class) {
                if (oldHolder instanceof Class) {
                    ModuleResourceUtil.deepCopyByClass((Class<?>) holder, (Class<?>) oldHolder);
                } else {
                    LogUtil.error("热更新前的资源持有者 {} 不是静态类，不能兼容，跳过资源拷贝", ((Class) holder).getName());
                }
            } else {
                if (oldHolder instanceof Class) {
                    LogUtil.error("热更新后的资源持有者 {} 不是静态类，不能兼容，跳过资源拷贝", holder.getClass().getName());
                } else {
                    ModuleResourceUtil.deepCopyByExclusive(oldHolder, holder, null);
                }
            }
        }

        // 可插拔模块中的Holder处理
        if (holder.getClass().getClassLoader() instanceof ModuleClassLoader) {
            ClassLoader classLoader = holder.getClass().getClassLoader();
            INSTANCE.moduleHolderMap.computeIfAbsent(classLoader, cl -> new LinkedHashSet<>()).add(holder);
        }
    }

    public static void batchRegister(List<Class<?>> classes) {
        if (CollectionUtil.isNotEmpty(classes)) {
            // 记录模块中的资源持有者，并转移旧模块中资源持有者的数据到新的模块类中
            String holderKey;
            for (Class<?> clazz : classes) {
                Object[] holders = ModuleResourceUtil.getResourceHolderByType(clazz);
                if (holders != null && holders.length > 0) {
                    for (Object holder : holders) {
                        holderKey = ModuleResourceUtil
                                .toHolderKey(holder, clazz.isEnum() ? String.valueOf(holder) : "");
                        registerHolder(holderKey, holder);
                    }
                }
            }
        }
    }

    public static void prepareUnregister(Collection<String> oldHolderKeys) {
        if (CollectionUtil.isEmpty(oldHolderKeys)) {
            return;
        }

        for (String holderKey : oldHolderKeys) {
            if (INSTANCE.holderMap.containsKey(holderKey)) {
                INSTANCE.oldHolderCache.put(holderKey, INSTANCE.holderMap.remove(holderKey));
            }
        }
    }

    public static void unregisterOldHolders() {
        INSTANCE.oldHolderCache.clear();
        INSTANCE.moduleHolderMap.clear();
    }

    public static Object getHolder(String holderKey) {
        return INSTANCE.holderMap.get(holderKey);
    }

    public static Object getOldHolder(String holderKey) {
        return INSTANCE.oldHolderCache.get(holderKey);
    }

    public static Collection<String> getHolderKeys() {
        return INSTANCE.holderMap.keySet();
    }

    public static Map<String, String> getHoldersSnapshot() {
        Map<String, String> map = new HashMap<>(INSTANCE.holderMap.size());
        for (Map.Entry<String, Object> entry : INSTANCE.holderMap.entrySet()) {
            if (entry.getValue() instanceof Class) {
                map.put(entry.getKey(), ((Class) entry.getValue()).getName());
            } else {
                map.put(entry.getKey(), entry.getValue().getClass().getName());
            }
        }
        return map;
    }

    public static Collection<Object> getHolders() {
        return INSTANCE.holderMap.values();
    }

    public static Collection<Object> getHolderByModule(ClassLoader classLoader) {
        return INSTANCE.moduleHolderMap.get(classLoader);
    }
}
