package cn.laoshini.dk.common;

import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;

import cn.laoshini.dk.util.StringUtil;

/**
 * 记录和提供Spring上下文对象
 *
 * @author fagarine
 */
public class SpringContextHolder {

    private static SpringContextHolder instance = new SpringContextHolder();
    private ApplicationContext applicationContext;
    private ClassLoader originClassLoader;
    private boolean allowOverriding;

    private SpringContextHolder() {
        // 记录当前类实例，方便通过静态方法访问
        instance = this;
    }

    public static SpringContextHolder getInstance() {
        return instance;
    }

    public static ApplicationContext getContext() {
        return instance.applicationContext;
    }

    public static void setContext(ApplicationContext applicationContext) {
        instance.applicationContext = applicationContext;
        DefaultListableBeanFactory beanFactory = getDefaultListableBeanFactory();
        instance.originClassLoader = beanFactory.getBeanClassLoader();
        instance.allowOverriding = beanFactory.isAllowBeanDefinitionOverriding();
    }

    public static boolean isInitialized() {
        return getContext() != null;
    }

    public static DefaultListableBeanFactory getDefaultListableBeanFactory() {
        ConfigurableApplicationContext context = (ConfigurableApplicationContext) getContext();
        return (DefaultListableBeanFactory) context.getBeanFactory();
    }

    public static StandardEnvironment getEnvironment() {
        return (StandardEnvironment) getContext().getEnvironment();
    }

    public static void addProperties(String name, Properties properties) {
        getEnvironment().getPropertySources().addLast(new PropertiesPropertySource(name, properties));
    }

    public static void removePropertiesList(List<String> propertySourceNames) {
        MutablePropertySources mps = getEnvironment().getPropertySources();
        for (String name : propertySourceNames) {
            mps.remove(name);
        }
    }

    public static void setSpringCurrentClassLoader(ClassLoader beanClassLoader) {
        DefaultListableBeanFactory beanFactory = getDefaultListableBeanFactory();
        beanFactory.setBeanClassLoader(beanClassLoader);
        beanFactory.setAllowBeanDefinitionOverriding(true);
    }

    public static void resetSpringCurrentClassLoader() {
        DefaultListableBeanFactory beanFactory = getDefaultListableBeanFactory();
        beanFactory.setBeanClassLoader(instance.originClassLoader);
        beanFactory.setAllowBeanDefinitionOverriding(instance.allowOverriding);
    }

    public static <T> T getBean(Class<T> requiredType) {
        return getContext().getBean(requiredType);
    }

    public static <T> T getBean(String className) {
        return (T) getContext().getBean(className);
    }

    public static String getProperty(String key) {
        return getEnvironment().getProperty(key);
    }

    public static String getStringProperty(String key, String defaultValue) {
        String value = getEnvironment().getProperty(key);
        if (StringUtil.isEmptyString(value)) {
            return defaultValue;
        }
        return value;
    }

    public static boolean getBoolProperty(String key, boolean defaultValue) {
        String value = getEnvironment().getProperty(key);
        if (StringUtil.isEmptyString(value)) {
            return defaultValue;
        }
        return Boolean.TRUE.toString().equalsIgnoreCase(value);
    }

    public static int getIntProperty(String key, int defaultValue) {
        String value = getEnvironment().getProperty(key);
        if (StringUtil.isEmptyString(value)) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

}
