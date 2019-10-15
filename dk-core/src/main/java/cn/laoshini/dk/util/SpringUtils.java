package cn.laoshini.dk.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.util.StringUtils;

import cn.laoshini.dk.common.SpringContextHolder;

/**
 * Spring相关功能工具类
 *
 * @author fagarine
 */
public class SpringUtils {
    private SpringUtils() {
    }

    /**
     * 加载新类或热更新类到Spring容器
     *
     * @param clazz 类
     */
    public static <T> T registerSpringBean(Class<T> clazz) {
        DefaultListableBeanFactory beanFactory = SpringContextHolder.getDefaultListableBeanFactory();
        String beanName = StringUtils.uncapitalize(clazz.getSimpleName());

        try {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);

            Object origin = beanFactory.getBean(beanName);
            // 对象已存在容器中，热更新
            beanDefinition.setBeanClassName(clazz.getName());
            beanFactory.registerBeanDefinition(beanName, beanDefinition);

            // 热更时拷贝原对象的数据到新对象中
            Object bean = beanFactory.getBean(beanName);
            ModuleResourceUtil.deepCopyByExclusive(origin, bean, parseClassSpringDepends(clazz));
        } catch (NoSuchBeanDefinitionException e) {
            // 根据类型创建一个新的bean，注册到Spring容器中
            BeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClassName(clazz.getName());
            beanFactory.registerBeanDefinition(beanName, beanDefinition);
        }

        return (T) beanFactory.getBean(beanName);
    }

    public static void removeBean(String beanName) {
        DefaultListableBeanFactory beanFactory = SpringContextHolder.getDefaultListableBeanFactory();
        beanFactory.removeBeanDefinition(beanName);
    }

    public static boolean isSpringBeanClass(Class<?> clazz) {
        if (ReflectHelper.isSpringAnnotationPresent(clazz)) {
            return true;
        }

        String[] depends = parseClassSpringDependNames(clazz);
        return depends.length > 0;
    }

    /**
     * 从类中找出明确标记为依赖Spring注入的变量名称
     *
     * @param clazz 指定类型
     * @return 返回所有符合条件的变量名称
     */
    public static Set<String> parseClassSpringDepends(Class<?> clazz) {
        Set<String> depends = new LinkedHashSet<>();

        // 从参数查找
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Autowired.class) || field.isAnnotationPresent(Resource.class)) {
                depends.add(field.getName());
            }
        }

        // 从setter方法查找
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("set")) {
                if (method.isAnnotationPresent(Autowired.class) || method.isAnnotationPresent(Resource.class)) {
                    for (Parameter parameter : method.getParameters()) {
                        depends.add(parameter.getName());
                    }
                }
            }
        }
        return depends;
    }

    private static String[] parseClassSpringDependNames(Class<?> clazz) {
        Set<String> depends = new LinkedHashSet<>();

        // 先从构造方法查找
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            for (Parameter parameter : constructor.getParameters()) {
                depends.add(parameter.getName());
            }
        }

        // 再从参数查找
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Autowired.class)) {
                if (field.isAnnotationPresent(Qualifier.class)) {
                    depends.add(field.getAnnotation(Qualifier.class).value());
                } else {
                    depends.add(field.getName());
                }
            } else if (field.isAnnotationPresent(Resource.class)) {
                depends.add(field.getAnnotation(Resource.class).name());
            }
        }

        // 再从setter方法查找
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Autowired.class) || method.getName().startsWith("set")) {
                for (Parameter parameter : method.getParameters()) {
                    depends.add(parameter.getName());
                }
            }
        }
        return depends.toArray(new String[0]);
    }

}
