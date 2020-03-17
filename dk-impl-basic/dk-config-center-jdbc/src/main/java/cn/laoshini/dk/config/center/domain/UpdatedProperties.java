package cn.laoshini.dk.config.center.domain;

import java.util.Map;

import cn.laoshini.dk.util.CollectionUtil;

/**
 * @author fagarine
 */
public class UpdatedProperties {
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

    public UpdatedProperties() {
    }

    public UpdatedProperties(String profile, String label) {
        this.profile = profile;
        this.label = label;
    }

    public UpdatedProperties(String profile, String label, Map<String, String> propertiesMap) {
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

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<String, String> getPropertiesMap() {
        return propertiesMap;
    }

    public void setPropertiesMap(Map<String, String> propertiesMap) {
        this.propertiesMap = propertiesMap;
    }

}
