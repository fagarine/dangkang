package cn.laoshini.dk.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 系统默认关系数据库DAO相关配置项，使用系统内部实现的DAO，如果想要自己实现，不需要这些配置
 *
 * @author fagarine
 */
@Getter
@Setter
@ToString
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConfigurationProperties(prefix = "dk.rdb")
public class DangKangRdbProperties {

    /**
     * 选择关系数据库数据访问对象的实现方式
     */
    private String dao = "DEFAULT";

    /**
     * 选择内部关系数据库访问对象的管理对象实现方式
     */
    private String manager = "DEFAULT";

    /**
     * 选择内建DAO表实例管理对象的实现方式
     */
    private String entityManager = "DEFAULT";

    /**
     * 数据库驱动类
     */
    private String driver = "com.mysql.cj.jdbc.Driver";

    /**
     * 数据库连接URL
     */
    private String url;

    /**
     * 数据库用户名
     */
    private String username;

    /**
     * 数据库连接密码
     */
    private String password;

}
