package cn.laoshini.dk.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 外置模块配置项（外置模块项目专用）
 *
 * @author fagarine
 */
@Getter
@Setter
@ToString
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConfigurationProperties(prefix = "dk.module")
public class DangKangModuleProperties {

    /**
     * 模块名称（仅外置模块需要配置）
     */
    private String name;

    /**
     * 模块描述信息（仅外置模块需要配置）
     */
    private String description;

}

