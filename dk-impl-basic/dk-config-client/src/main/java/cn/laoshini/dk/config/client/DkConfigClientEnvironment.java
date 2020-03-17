package cn.laoshini.dk.config.client;

import java.io.IOException;
import java.util.Properties;

import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.config.client.ConfigServicePropertySourceLocator;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePropertySource;

import cn.laoshini.dk.common.ResourcesHolder;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.StringUtil;

/**
 * @author fagarine
 */
public class DkConfigClientEnvironment extends StandardEnvironment {

    private static final String[] CONFIG_CLIENT_FILE = { "bootstrap", "config-client" };

    private static final String[] FILE_SUFFIX = { ".properties", ".yml", ".yaml" };

    @Override
    protected void customizePropertySources(MutablePropertySources propertySources) {
        super.customizePropertySources(propertySources);

        loadConfigProperties(this);
    }

    private void loadConfigProperties(ConfigurableEnvironment environment) {
        Resource resource = null;
        ResourceLoader resourceLoader = new DefaultResourceLoader(this.getClass().getClassLoader());
        String configFile = ResourcesHolder.getConfigClientFile();
        if (StringUtil.isNotEmptyString(configFile)) {
            resource = resourceLoader.getResource(configFile);
        }
        if (resource == null || !resource.exists() || !resource.isFile()) {
            for (String prefix : CONFIG_CLIENT_FILE) {
                for (String suffix : FILE_SUFFIX) {
                    configFile = prefix + suffix;
                    resource = resourceLoader.getResource(configFile);
                    if (resource.exists() && resource.isFile()) {
                        break;
                    }
                }
            }
        }

        if (!resource.exists() || !resource.isFile()) {
            throw new BusinessException("config.client.file", "找不到有效的配置中心客户端配置文件");
        }

        MutablePropertySources propertySources = environment.getPropertySources();
        try {
            // 加载config client配置文件信息到environment中
            LogUtil.start("config client file:" + resource.getFile());
            ResourcePropertySource propertySource = new ResourcePropertySource(resource);
            propertySources.addLast(propertySource);
        } catch (IOException e) {
            LogUtil.error(e, "读取config client配置信息出错");
        }

        // 初始化配置中心服务与数据
        propertySources.addLast(initConfigServicePropertySourceLocator(environment));
    }

    /**
     * 初始化配置中心服务与数据
     *
     * @param environment environment
     * @return 返回从配置中心拉取到的配置信息
     */
    private PropertySource<?> initConfigServicePropertySourceLocator(ConfigurableEnvironment environment) {
        String uri = environment.getProperty("dk.config.server");
        String name = environment.getProperty("dk.config.name");
        String label = environment.getProperty("dk.config.label");
        String profile = environment.getProperty("dk.config.profile");
        LogUtil.start("application name:{}, uri:{}, profile:{}, label:{}", name, uri, profile, label);

        // 创建配置中心客户端配置数据
        ConfigClientProperties configClientProperties = new ConfigClientProperties(environment);
        Properties properties = new Properties();
        if (StringUtil.isEmptyString(uri)) {
            throw new BusinessException("config.server.url", "配置中心URL未配置，配置项：dk.config.server");
        }
        configClientProperties.setUri(uri.split(","));
        if (StringUtil.isEmptyString(name)) {
            throw new BusinessException("config.client.name", "配置中心客户端name未配置，配置项：dk.config.name");
        }
        configClientProperties.setName(name);
        properties.put("spring.cloud.config.name", name);
        if (StringUtil.isEmptyString(profile)) {
            throw new BusinessException("config.client.profile", "配置中心客户端profile未配置，配置项：dk.config.profile");
        }
        configClientProperties.setProfile(profile);
        properties.put("spring.cloud.config.profile", profile);
        if (StringUtil.isNotEmptyString(label)) {
            configClientProperties.setLabel(label);
            properties.put("spring.cloud.config.label", label);
        }

        environment.getPropertySources().addLast(new PropertiesPropertySource("springCloudConfigClient", properties));

        // 定位Environment属性并拉取数据
        ConfigServicePropertySourceLocator configServicePropertySourceLocator = new ConfigServicePropertySourceLocator(
                configClientProperties);
        PropertySource<?> propertySource = configServicePropertySourceLocator.locate(environment);

        LogUtil.start("config propertySource:" + propertySource.getName());
        for (PropertySource ps : ((CompositePropertySource) propertySource).getPropertySources()) {
            LogUtil.start(ps.getName() + ":" + ((MapPropertySource) ps).getSource());
        }

        return propertySource;
    }
}
