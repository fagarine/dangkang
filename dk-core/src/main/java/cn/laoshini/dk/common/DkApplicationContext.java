package cn.laoshini.dk.common;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @author fagarine
 */
public class DkApplicationContext extends ClassPathXmlApplicationContext implements IDkApplicationContext {

    @Override
    protected ConfigurableEnvironment createEnvironment() {
        return new DkEnvironment();
    }

    @Override
    public void configLocations(String... locations) {
        super.setConfigLocations(locations);
    }

    @Override
    public String[] dependentSpringConfigs() {
        return new String[] { "applicationContext-dk-default.xml" };
    }
}
