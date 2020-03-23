package cn.laoshini.dk.starter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import cn.laoshini.dk.constant.GameServerProtocolEnum;

/**
 * 游戏服快速相关启动配置项，游戏服配置信息
 *
 * @author fagarine
 */
@Getter
@Setter
@ToString
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConfigurationProperties(prefix = "dk.game.server")
public class DangKangGameServerProperties {

    /**
     * 游戏服id
     */
    private int id;

    /**
     * 游戏服名称
     */
    private String name = "当康大穰";

    /**
     * 游戏服监听端口号
     */
    private int port;

    /**
     * 游戏服务器使用什么协议通信（HTTP,TCP,UDP等）
     */
    private GameServerProtocolEnum protocol = GameServerProtocolEnum.TCP;

}
