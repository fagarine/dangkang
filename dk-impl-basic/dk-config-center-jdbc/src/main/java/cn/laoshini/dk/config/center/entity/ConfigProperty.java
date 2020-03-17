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
public class ConfigProperty {

    /**
     * 唯一id，自增
     */
    private int id;

    /**
     * 配置信息的key，对应配置文件中的key
     */
    private String key;

    /**
     * 配置信息的value，对应配置文件中的value
     */
    private String value;

    /**
     * 应用名
     */
    private String application;

    /**
     * 分支
     */
    private String profile;

    /**
     * 标签
     */
    private String label;
}
