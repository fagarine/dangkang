package cn.laoshini.dk.config.center.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author fagarine
 */
@Getter
@Setter
@ToString
public class PropertyHistory {

    /**
     * 唯一id，自增
     */
    private int id;

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

    /**
     * 发布版本号
     */
    private int version;

    /**
     * 参数改变的具体内容
     */
    private List<PropertyChange> content;

    /**
     * 版本状态，对应{@link cn.laoshini.dk.config.center.constant.PropertyStatus}
     */
    private String status;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 发布版本创建时间
     */
    private Date createTime;

    /**
     * 最后更新时间
     */
    private Date updateTime;

    public PropertyHistory() {
    }

    public PropertyHistory(String application, String profile, String label) {
        this.application = application;
        this.profile = profile;
        this.label = label;
    }

    public PropertyHistory(String application, String profile, String label, int version, int size, String operator) {
        this.application = application;
        this.profile = profile;
        this.label = label;
        this.version = version;
        this.content = new ArrayList<>(size);
        this.operator = operator;
    }
}
