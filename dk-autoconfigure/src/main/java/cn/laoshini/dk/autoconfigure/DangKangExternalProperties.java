package cn.laoshini.dk.autoconfigure;

import java.util.List;

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
@ConfigurationProperties(prefix = "dk.ext")
public class DangKangExternalProperties {

    /**
     * 用户游戏项目的需要扫描到的包路径前缀，可以填写多个
     */
    private List<String> packagePrefix;

    /**
     * 外部游戏功能注册功能实现类选择
     */
    private String register = "DEFAULT";
}
