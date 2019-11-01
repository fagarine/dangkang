package cn.laoshini.dk.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.InputStreamResource;

import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.FileUtil;

/**
 * @author fagarine
 */
class DkEnvironment extends StandardEnvironment {
    @Override
    protected void customizePropertySources(MutablePropertySources propertySources) {
        super.customizePropertySources(propertySources);

        String[] locations = ResourcesHolder.getPropertyLocations();
        if (CollectionUtil.isNotEmpty(locations)) {
            for (String location : locations) {
                URL url = DkEnvironment.class.getResource("/" + location);
                String filepath = url.getFile();
                File file = new File(filepath);
                if (!file.exists()) {
                    continue;
                }

                Properties properties = new Properties();
                try (InputStream inputStream = new FileInputStream(file)) {
                    if (FileUtil.isYamlFile(file.getName())) {
                        // yaml配置文件，转换为Properties对象
                        YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();
                        factoryBean.setResources(new InputStreamResource(inputStream));
                        properties = factoryBean.getObject();
                    } else {
                        properties.load(inputStream);
                    }

                    PropertiesReader propertiesReader = PropertiesReader.newReaderByProperties(properties);
                    propertySources
                            .addLast(new PropertiesPropertySource(file.getName(), propertiesReader.getProperties()));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
