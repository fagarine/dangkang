package cn.laoshini.dk.listener;

import java.util.Collection;

/**
 * 对应配置项有更新的事件
 *
 * @author fagarine
 */
public class PropertyChangedEvent extends DkContextEvent {

    /**
     * 记录改变了的配置项key
     */
    private Collection<String> changedPropertyKeys;

    public PropertyChangedEvent(Collection<String> changedPropertyKeys) {
        this.changedPropertyKeys = changedPropertyKeys;
    }

    public PropertyChangedEvent() {
        this(null);
    }

    public Collection<String> getChangedPropertyKeys() {
        return changedPropertyKeys;
    }
}
