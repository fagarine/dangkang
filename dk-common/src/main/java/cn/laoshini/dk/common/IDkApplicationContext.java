package cn.laoshini.dk.common;

import org.springframework.context.ConfigurableApplicationContext;

/**
 * 当康容器上下文SPI接口
 *
 * @author fagarine
 */
public interface IDkApplicationContext extends ConfigurableApplicationContext {

    /**
     * 传入用户项目中Spring配置文件路径
     *
     * @param locations 配置文件路径
     */
    void configLocations(String... locations);

    /**
     * 获取当康容器启动依赖的Spring配置文件
     *
     * @return 返回配置文件路径
     */
    String[] dependentSpringConfigs();
}
