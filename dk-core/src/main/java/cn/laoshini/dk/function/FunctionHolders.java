package cn.laoshini.dk.function;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import cn.laoshini.dk.annotation.FunctionDependent;
import cn.laoshini.dk.util.CollectionUtil;

/**
 * @author fagarine
 */
final class FunctionHolders {

    private final String functionKey;

    private final Set<Object> holders = Collections.newSetFromMap(new WeakHashMap<>());

    private final Set<Object> cache = Collections.newSetFromMap(new WeakHashMap<>());

    FunctionHolders(String functionKey) {
        this.functionKey = functionKey;
    }

    void reinjectDependent() {
        for (Object holder : holders) {
            boolean isStatic = holder instanceof Class;
            Class<?> clazz = isStatic ? (Class<?>) holder : holder.getClass();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (isStatic && !Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                if (field.isAnnotationPresent(FunctionDependent.class)) {
                    if (Func.class.equals(field.getType())) {
                        // 使用Func封装的依赖，统一经过FuncCache处理
                        continue;
                    }

                    // 重新注入依赖
                    ConfigurableFunctionInjector.reinjectFieldDependent(holder, field);
                }
            }
        }
    }

    void addHolder(Object bean) {
        holders.add(bean);
    }

    void prepareRemove(ClassLoader classLoader) {
        Set<Object> beans = holders.stream().filter(o -> o != null && o.getClass().getClassLoader().equals(classLoader))
                .collect(Collectors.toSet());
        if (CollectionUtil.isNotEmpty(beans)) {
            holders.removeAll(beans);
            cache.addAll(beans);
        }
    }

    void remove(ClassLoader classLoader) {
        cache.removeIf(o -> o != null && o.getClass().getClassLoader().equals(classLoader));
    }

    String getFunctionKey() {
        return functionKey;
    }
}
