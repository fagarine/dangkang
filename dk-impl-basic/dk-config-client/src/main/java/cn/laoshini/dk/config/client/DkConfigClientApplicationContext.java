package cn.laoshini.dk.config.client;

import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import cn.laoshini.dk.common.IDkApplicationContext;

/**
 * @author fagarine
 */
public class DkConfigClientApplicationContext extends GenericXmlApplicationContext implements IDkApplicationContext {

    @Override
    protected ConfigurableEnvironment createEnvironment() {
        // 使用自定义Environment对象
        return new DkConfigClientEnvironment();
    }

    @Override
    public void configLocations(String... locations) {
        super.load(locations);
    }

    @Override
    public String[] dependentSpringConfigs() {
        return new String[] { "applicationContext-dk-config.xml" };
    }
}
