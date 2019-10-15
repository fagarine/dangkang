package cn.laoshini.dk.transform.injection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 可配置功能注入接口定义
 *
 * @author fagarine
 */
public interface IConfigurableFunctionInjector {

    /**
     * 记录容器启动时，可配置功能未初始完成前，需要注入可配置功能的对象
     */
    Set<Object> WAIT_INJECTION_BEANS = new CopyOnWriteArraySet<>();

    /**
     * 添加依赖功能注入的对象到集合中
     *
     * @param bean 依赖可配置功能的对象
     */
    default void add(Object bean) {
        if (bean != null) {
            WAIT_INJECTION_BEANS.add(bean);
        }
    }

    /**
     * 获取等待注入可配置功能的对象
     *
     * @return 该方法不会返回null
     */
    default Collection<Object> getWaitInjectionBeans() {
        return new ArrayList<>(WAIT_INJECTION_BEANS);
    }

    /**
     * 清空所有等待注入的对象
     */
    default void clear() {
        WAIT_INJECTION_BEANS.clear();
    }

    /**
     * 根据对象和指定字段名称，注入对应的可配置功能依赖
     *
     * @param bean 依赖可配置功能的对象
     * @param fieldName 声明依赖的字段名称
     */
    void injectField(Object bean, String fieldName);
}
