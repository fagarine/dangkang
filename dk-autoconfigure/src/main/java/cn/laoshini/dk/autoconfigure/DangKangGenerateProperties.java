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
@ConfigurationProperties(prefix = "dk.generate")
public class DangKangGenerateProperties {

    /**
     * 是否自动创建消息处理Handler类，默认为false；如果设置为true，当有新的Handler表达式创建时，将会创建一个对应的Handler类，并将表达式转变为其中的代码
     */
    private boolean handler = false;
}
