package cn.laoshini.dk.common;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @author fagarine
 */
public class DkApplicationContext extends ClassPathXmlApplicationContext {

    @Override
    protected ConfigurableEnvironment createEnvironment() {
        return new DkEnvironment();
    }
}
