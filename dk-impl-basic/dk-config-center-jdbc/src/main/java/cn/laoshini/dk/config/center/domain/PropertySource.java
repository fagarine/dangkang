package cn.laoshini.dk.config.center.domain;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import cn.laoshini.dk.util.CollectionUtil;

/**
 * @author fagarine
 */
@Getter
@Setter
@ToString
public class PropertySource {

    /**
     * 应用名称
     */
    private String application;

    /**
     * 应用profile
     */
    private String profile;

    /**
     * 标签
     */
    private String label;

    /**
     * 需要更新的配置项
     */
    private Map<String, String> propertiesMap;

    public PropertySource(String application, String profile, String label) {
        this.application = application;
        this.profile = profile;
        this.label = label;
    }

    public PropertySource(String application, String profile, String label, Map<String, String> propertiesMap) {
        this.application = application;
        this.profile = profile;
        this.label = label;
        this.propertiesMap = propertiesMap;
    }

    /**
     * 是否有需要更新的配置项
     */
    public boolean hasProperties() {
        return CollectionUtil.isNotEmpty(propertiesMap);
    }
}
