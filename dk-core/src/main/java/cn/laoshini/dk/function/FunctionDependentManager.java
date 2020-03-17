package cn.laoshini.dk.function;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.laoshini.dk.annotation.ResourceHolder;
import cn.laoshini.dk.util.CollectionUtil;

/**
 * 关于可配置功能依赖的管理类
 *
 * @author fagarine
 */
@ResourceHolder
public enum FunctionDependentManager {
    /**
     * 使用枚举实现单例
     */
    INSTANCE;

    private static final Map<String, FunctionHolders> FUNCTION_KEY_TO_HOLDERS = new ConcurrentHashMap<>();

    private static final Map<ClassLoader, Set<String>> MODULE_FUNCTION_KEYS = new ConcurrentHashMap<>();

    private static final Map<ClassLoader, Set<String>> MODULE_FUNCTION_KEYS_CACHE = new ConcurrentHashMap<>();

    /**
     * 刷新可配置功能依赖
     */
    public static void refreshFunctionDependent(Collection<String> changedFunctionKeys) {
        if (CollectionUtil.isEmpty(changedFunctionKeys)) {
            return;
        }

        // 重新注入直接依赖
        for (FunctionHolders functionHolders : FUNCTION_KEY_TO_HOLDERS.values()) {
            if (changedFunctionKeys.contains(functionHolders.getFunctionKey())) {
                functionHolders.reinjectDependent();
            }
        }

        // 刷新通过Func封装的依赖
        FuncContainer.refreshAll(changedFunctionKeys);
    }

    /**
     * 注册依赖可配置功能的依赖对象和其被依赖功能的key
     *
     * @param functionKey 功能key
     * @param bean 声明依赖可配置功能的bean
     */
    public static void registerDependent(String functionKey, Object bean) {
        FUNCTION_KEY_TO_HOLDERS.computeIfAbsent(functionKey, FunctionHolders::new).addHolder(bean);
    }

    public static void prepareUnregister(ClassLoader classLoader) {
        Set<String> keys = MODULE_FUNCTION_KEYS.remove(classLoader);
        if (CollectionUtil.isNotEmpty(keys)) {
            MODULE_FUNCTION_KEYS_CACHE.computeIfAbsent(classLoader, cl -> new LinkedHashSet<>()).addAll(keys);
            for (String key : keys) {
                FUNCTION_KEY_TO_HOLDERS.get(key).prepareRemove(classLoader);
            }
        }
    }

    public static void unregister(ClassLoader classLoader) {
        Set<String> keys = MODULE_FUNCTION_KEYS_CACHE.remove(classLoader);
        if (CollectionUtil.isNotEmpty(keys)) {
            for (String key : keys) {
                // 清空模块在缓存中的记录
                FUNCTION_KEY_TO_HOLDERS.get(key).remove(classLoader);
            }
        }
        keys.clear();
    }

}
