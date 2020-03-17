package cn.laoshini.dk.config.center.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author fagarine
 */
@Getter
@Setter
@ToString
public class PropertyChange {

    /**
     * 配置信息的key
     */
    private String key;

    /**
     * 配置信息在表中的id
     */
    private Integer pid;

    /**
     * 发布前的value
     */
    private String oldValue;

    /**
     * 发布后的value
     */
    private String newValue;

    /**
     * 单个配置操作，对应{@link cn.laoshini.dk.config.center.constant.PropertyOperation}
     */
    private String operation;

    public PropertyChange() {
    }

    public PropertyChange(String key, String newValue) {
        this.key = key;
        this.newValue = newValue;
    }

    public PropertyChange(String key, Integer pid, String oldValue, String newValue, String operation) {
        this.key = key;
        this.pid = pid;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.operation = operation;
    }
}
