package cn.laoshini.dk.dao;

import java.sql.Driver;

import javax.sql.DataSource;

import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import cn.laoshini.dk.condition.ConditionalOnPropertyExists;
import cn.laoshini.dk.exception.BusinessException;

/**
 * 当康系统默认DAO专用DataSource配置，仅在开启配置的情况下创建实例
 *
 * @author fagarine
 */
@Getter
@Setter
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnPropertyExists(prefix = "dk.rdb", name = { "url", "username", "password" })
public class InnerGameDataSourceConfig {

    @Value("${dk.rdb.driver}")
    private String driverClass;

    @Value("${dk.rdb.url}")
    private String url;

    @Value("${dk.rdb.username}")
    private String user;

    @Value("${dk.rdb.password}")
    private String password;

    @Bean(name = "innerGameDataSource")
    public DataSource innerGameDataSource() {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends Driver> clazz = (Class<? extends Driver>) Class.forName(driverClass);
            SqlBuilder.initDbType(driverClass);
            return new SimpleDriverDataSource(clazz.newInstance(), url, user, password);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new BusinessException("db.driver.error", "默认DAO配置的SQL驱动类不正确:" + driverClass);
        }
    }

    @Bean(name = "innerGameTransactionManager")
    public DataSourceTransactionManager gameTransactionManager() {
        return new DataSourceTransactionManager(innerGameDataSource());
    }

    @Bean(name = "innerGameJdbcTemplate")
    public JdbcTemplate innerGameJdbcTemplate() {
        return new JdbcTemplate(innerGameDataSource());
    }
}
