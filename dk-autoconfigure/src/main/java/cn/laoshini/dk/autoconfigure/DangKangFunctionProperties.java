package cn.laoshini.dk.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 当康系统可配置功能相关配置项
 *
 * @author fagarine
 */
@Getter
@Setter
@ToString
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConfigurationProperties(prefix = "dk.function")
public class DangKangFunctionProperties {

    /**
     * 是否允许功能的实现类缺失；注意：允许缺失的情况下，如果找不到功能的实现类，系统也不会报错，这可能导致调用处的NullPointerException
     */
    private boolean vacant = true;
}
