package cn.laoshini.dk.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 当康系统配置中心相关配置项
 *
 * @author fagarine
 */
@Getter
@Setter
@ToString
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConfigurationProperties(prefix = "dk.config")
public class DangKangConfigCenterProperties {

    /**
     * 配置中心URL
     */
    private String server;

    /**
     * 项目名称（业务项目或者游戏项目的名称，例如游戏id）
     */
    private String name;

    /**
     * 项目profile（例如游戏项目默认使用serverId作为profile）
     */
    private String profile;

    /**
     * 项目标签（游戏项目的label表示运行环境，配置文件中可不填，默认为master）
     */
    private String label = "master";

}
