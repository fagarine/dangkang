package cn.laoshini.dk.common;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.FileUtil;
import cn.laoshini.dk.util.LogUtil;

/**
 * @author fagarine
 */
class DkEnvironment extends StandardEnvironment {

    @Override
    protected void customizePropertySources(MutablePropertySources propertySources) {
        super.customizePropertySources(propertySources);

        String[] locations = ResourcesHolder.getPropertyLocations();
        if (CollectionUtil.isNotEmpty(locations)) {
            Resource resource;
            ResourceLoader resourceLoader = new DefaultResourceLoader(this.getClass().getClassLoader());
            for (String location : locations) {
                LogUtil.debug("开始加载用户指定的配置文件:" + location);

                resource = resourceLoader.getResource(location);
                if (!resource.exists() || !resource.isFile()) {
                    LogUtil.error("未找到用户指定的配置文件:" + location);
                    continue;
                }

                Properties properties = new Properties();
                try {
                    if (FileUtil.isYamlFile(resource.getFilename())) {
                        // yaml配置文件，转换为Properties对象
                        YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();
                        factoryBean.setResources(resource);
                        properties = factoryBean.getObject();
                    } else {
                        properties.load(resource.getInputStream());
                    }

                    PropertiesReader propertiesReader = PropertiesReader.newReaderByProperties(properties);
                    propertySources.addLast(new PropertiesPropertySource(location, propertiesReader.getProperties()));
                } catch (IOException e) {
                    LogUtil.error(e, "读取用户指定的配置文件出错:" + location);
                }

            }
        }

    }
}
