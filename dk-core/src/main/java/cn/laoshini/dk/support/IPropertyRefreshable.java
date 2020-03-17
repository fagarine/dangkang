package cn.laoshini.dk.support;

import java.util.Collection;

/**
 * 依赖于配置项参数，并且在配置项参数变更后，需要刷新数据的对象
 *
 * @author fagarine
 */
public interface IPropertyRefreshable extends IRefreshable {

    /**
     * 刷新依赖于传入的配置项key的数据
     *
     * @param propertyKeys 需要刷新数据的配置项key
     */
    void refresh(Collection<String> propertyKeys);

    /**
     * 刷新任何依赖配置参数的数据
     */
    @Override
    default void refresh() {
        refresh(null);
    }
}
