package cn.laoshini.dk.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 当康系统游戏服GM功能相关配置项
 *
 * @author fagarine
 */
@Getter
@Setter
@ToString
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConfigurationProperties(prefix = "dk.gm")
public class DangKangGmProperties {

    /**
     * 游戏服进程的配置项中，记录GM服务接口的配置项名称
     */
    private String key = "gmUrl";

    /**
     * GM后台服务器URL根目录
     */
    private String console;
}
