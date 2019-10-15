package cn.laoshini.dk.manager;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 当康系统（不包括游戏功能服务）在Spring容器初始化时，分阶段相关初始化任务
 * Spring容器初始化顺序参见{@link org.springframework.beans.factory.BeanFactory}
 *
 * @author fagarine
 */
//@Component
public final class DangKangStagedInitializer implements InitializingBean, BeanPostProcessor, DisposableBean {

    @Override
    public void afterPropertiesSet() {
        // 当康系统自定义注解处理
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        return bean;
    }

    @Override
    public void destroy() throws Exception {

    }
}
