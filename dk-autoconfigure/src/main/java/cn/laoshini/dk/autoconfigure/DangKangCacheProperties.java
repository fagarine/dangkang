package cn.laoshini.dk.autoconfigure;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 应用内缓存相关配置项
 *
 * @author fagarine
 */
@Getter
@Setter
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConfigurationProperties(prefix = "dk.cache")
public class DangKangCacheProperties {

    /**
     * 选择应用内缓存数据访问对象的实现方式
     */
    private String dao = "DEFAULT";

}
