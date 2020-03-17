package cn.laoshini.dk.manager;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.laoshini.dk.annotation.ResourceHolder;
import cn.laoshini.dk.module.loader.ModuleClassLoader;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.SpringUtils;

/**
 * 系统中的Spring托管对象记录管理
 *
 * @author fagarine
 */
@ResourceHolder
public enum SpringBeanManager {
    /**
     * 使用枚举实现单例
     */
    INSTANCE;

    private final Map<String, Class<?>> springBeanNameCache = new LinkedHashMap<>();

    private final Map<String, Class<?>> springBeanMap = new ConcurrentHashMap<>();

    private final Map<ClassLoader, Set<Class<?>>> moduleClassMap = new ConcurrentHashMap<>();

    private final Map<ClassLoader, Set<String>> moduleSpringBeanMap = new ConcurrentHashMap<>();

    public static void registerBean(String beanName, Class<?> clazz) {
        INSTANCE.springBeanMap.put(beanName, clazz);

        // 可插拔模块中的类处理
        if (clazz.getClassLoader() instanceof ModuleClassLoader) {
            INSTANCE.moduleClassMap.computeIfAbsent(clazz.getClassLoader(), cl -> new LinkedHashSet<>()).add(clazz);
            INSTANCE.moduleSpringBeanMap.computeIfAbsent(clazz.getClassLoader(), cl -> new LinkedHashSet<>())
                    .add(beanName);
        }
    }

    public static void prepareUnregister(ClassLoader classLoader) {
        INSTANCE.springBeanNameCache.clear();

        INSTANCE.moduleClassMap.remove(classLoader);
        Collection<String> beanNames = INSTANCE.moduleSpringBeanMap.remove(classLoader);
        if (CollectionUtil.isNotEmpty(beanNames)) {
            for (String beanName : beanNames) {
                if (INSTANCE.springBeanMap.containsKey(beanName)) {
                    INSTANCE.springBeanNameCache.put(beanName, INSTANCE.springBeanMap.remove(beanName));
                }
            }
            beanNames.clear();
        }
    }

    public static void cancelPrepareUnregister() {
        for (Map.Entry<String, Class<?>> entry : INSTANCE.springBeanNameCache.entrySet()) {
            registerBean(entry.getKey(), entry.getValue());
        }
        INSTANCE.springBeanNameCache.clear();
    }

    public static void unregister() {
        for (String beanName : INSTANCE.springBeanNameCache.keySet()) {
            // 在热更新后模块已不存在的Spring托管对象，认定为无效对象，从容器移除（注销操作在新模块注册操作之后，所以这里比较的是新模块加载后的结果）
            if (!INSTANCE.springBeanMap.containsKey(beanName)) {
                SpringUtils.removeBean(beanName);
            }
        }

        INSTANCE.springBeanNameCache.clear();
    }

    public static Collection<String> getBeanNames() {
        return INSTANCE.springBeanMap.keySet();
    }

    public static Collection<Class<?>> getSpringClasses() {
        return INSTANCE.springBeanMap.values();
    }

    public static Collection<Class<?>> getSpringClassByModule(ModuleClassLoader classLoader) {
        return INSTANCE.moduleClassMap.get(classLoader);
    }
}
