package cn.laoshini.dk.starter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import cn.laoshini.dk.annotation.MessageHandle;
import cn.laoshini.dk.domain.msg.IMessage;
import cn.laoshini.dk.net.handler.IMessageHandler;
import cn.laoshini.dk.register.IMessageHandlerRegister;
import cn.laoshini.dk.register.IMessageRegister;

/**
 * 游戏服快速启动相关配置项，游戏项目配置信息
 *
 * @author fagarine
 */
@Getter
@Setter
@ToString
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConfigurationProperties(prefix = "dk.game")
public class DangKangGameProperties {

    /**
     * 游戏id
     */
    private int id = 1;

    /**
     * 游戏名称
     */
    private String name = "当康游戏";

    /**
     * 用户项目的Spring配置文件路径
     */
    private String[] springConfigs;

    /**
     * 用户项目的类扫描路径前缀
     */
    private String[] packagePrefixes;

    /**
     * 游戏消息类注册器，系统通过该注册器查找并注册消息类
     */
    private Class<IMessageRegister> messageRegister;

    /**
     * 游戏消息处理类注册器，系统通过该注册器查找并注册消息处理类
     */
    private Class<IMessageHandlerRegister> messageHandlerRegister;

    /**
     * 游戏消息的包路径前缀，使用这种方式注册的消息类，必须有一个名为{@link IMessage#ID_METHOD getId()}的静态方法
     * 或名为{@link IMessage#ID_FIELD MESSAGE_ID}的静态变量，系统才能读取到消息类对应的消息id。
     */
    private String[] messagePackages;

    /**
     * 游戏消息处理类的包路径前缀，使用这种方式注册的消息处理类必须是{@link IMessageHandler}的实现类，
     * 且被{@link MessageHandle @MessageHandle}注解标记。
     */
    private String[] messageHandlerPackages;

}
