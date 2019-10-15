package cn.laoshini.dk.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * @author fagarine
 */
@Getter
@Setter
@ToString
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConfigurationProperties(prefix = "dk.id")
public class DangKangIdProperties {

    /**
     * 选择id自增器的实现方式
     */
    private String incrementer = "DEFAULT";

    /**
     * 选择生成用户id的实现方式
     */
    private String user = "DEFAULT";

    /**
     * 选择生成游戏角色id的实现方式
     */
    private String role = "DEFAULT";
}
