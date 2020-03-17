package cn.laoshini.dk.config.client;

import org.springframework.cloud.bus.event.RefreshListener;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring cloud bus自动刷新相关功能支持
 *
 * @author fagarine
 */
@Configuration
public class CloudBusRefresher {

    @Bean
    public RefreshScope refreshScope() {
        return new RefreshScope();
    }

    @Bean
    public ContextRefresher contextRefresher(ConfigurableApplicationContext context, RefreshScope refreshScope) {
        // 使用自定义刷新对象
        return new CustomerContextRefresher(context, refreshScope);
    }

    @Bean
    public RefreshListener refreshListener(ContextRefresher contextRefresher) {
        return new RefreshListener(contextRefresher);
    }
}
