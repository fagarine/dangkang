package cn.laoshini.dk.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 当康系统基础配置项
 *
 * @author fagarine
 */
@Getter
@Setter
@ToString
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConfigurationProperties(prefix = "dk")
public class DangKangBasicProperties {

    /**
     * 消息最大响应时间（毫秒），超过该时间将会记录消息处理信息到错误日志中
     */
    private int maxResponse = 20;

    /**
     * 系统默认DAO的实现方式
     */
    private String defaultDao = "DEFAULT";

    /**
     * 是否开启当康系统即时编译相关功能
     */
    private boolean jit = false;

    /**
     * 是否开启当康系统表达式逻辑相关功能
     */
    private boolean expression = false;

    /**
     * 外置模块文件默认存放目录（相对项目根目录）
     */
    private String module = "/modules";

    /**
     * 存放热修复类文件的根目录
     */
    private String hotfix = "/hotfix";

}
