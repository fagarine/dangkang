package cn.laoshini.dk.function;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import cn.laoshini.dk.annotation.FunctionDependent;
import cn.laoshini.dk.common.SpringContextHolder;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.manager.ResourceHolderManager;
import cn.laoshini.dk.manager.SpringBeanManager;
import cn.laoshini.dk.module.loader.ModuleClassLoader;
import cn.laoshini.dk.transform.injection.ConfigurableFunctionInjectorProxy;
import cn.laoshini.dk.transform.injection.IConfigurableFunctionInjector;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.ReflectUtil;
import cn.laoshini.dk.util.StringUtil;

/**
 * 可配置功能注入工具类
 *
 * @author fagarine
 */
public class ConfigurableFunctionInjector implements IConfigurableFunctionInjector {
    private ConfigurableFunctionInjector() {
        ConfigurableFunctionInjectorProxy.setDelegate(this);
    }

    private static ConfigurableFunctionInjector instance = new ConfigurableFunctionInjector();

    public static ConfigurableFunctionInjector getInstance() {
        return instance;
    }

    /**
     * 查找所有受容器管理的对象中，对可配置功能的依赖，并注意依赖
     */
    public static void findAndRejectFunctionDependencies() {
        // Spring托管对象的依赖，已经包含在这里面，不需要单独处理
        for (Object bean : getInstance().getWaitInjectionBeans()) {
            injectFunctionToBean(bean);
        }
        getInstance().clear();

        for (Object holder : ResourceHolderManager.getHolders()) {
            injectFunctionToBean(holder);
        }
    }

    /**
     * 查找模块内所有受容器管理的对象中，对可配置功能的依赖，并注意依赖
     */
    public static void findAndRejectFunctionByModule(ModuleClassLoader classLoader) {
        Collection<Class<?>> classes = SpringBeanManager.getSpringClassByModule(classLoader);
        if (CollectionUtil.isNotEmpty(classes)) {
            for (Class<?> springClass : classes) {
                injectSpringFunctionField(springClass);
            }
        }

        Collection<Object> holders = ResourceHolderManager.getHolderByModule(classLoader);
        if (CollectionUtil.isNotEmpty(holders)) {
            for (Object holder : holders) {
                injectFunctionToBean(holder);
            }
        }
    }

    /**
     * 注入可配置功能的依赖
     *
     * @param bean 依赖可配置功能的对象
     */
    private static void injectFunctionToBean(Object bean) {
        Class<?> clazz = bean.getClass();
        List<Field> fields = new LinkedList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(FunctionDependent.class)) {
                fields.add(field);
            }
        }

        if (!fields.isEmpty()) {
            for (Field field : fields) {
                injectFunction(bean, field);
            }
        }
    }

    /**
     * 注入Spring托管类对可配置功能的依赖
     *
     * @param springClass Spring托管类
     */
    private static void injectSpringFunctionField(Class<?> springClass) {
        List<Field> fields = new LinkedList<>();
        for (Field field : springClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(FunctionDependent.class)) {
                fields.add(field);
            }
        }

        if (!fields.isEmpty()) {
            Object bean = SpringContextHolder.getBean(springClass);
            for (Field field : fields) {
                injectFunction(bean, field);
            }
        }
    }

    /**
     * 注入可配置功能依赖
     *
     * @param bean 依赖可配置功能的对象
     * @param field 声明依赖的变量
     */
    private static void injectFunction(Object bean, Field field) {
        FunctionDependent dependent = field.getAnnotation(FunctionDependent.class);
        Object impl = VariousWaysManager.getFunctionImplByAnnotation(field.getType(), dependent);
        if (impl != null) {
            // 注解注入式允许返回空，仅当返回不为空时注入
            ReflectUtil.setFieldValue(bean, field, impl);

            // 执行功能注入后逻辑
            if (StringUtil.isNotEmptyString(dependent.afterExecute())) {
                try {
                    ReflectUtil.invokeMethodAnyway(bean, dependent.afterExecute());
                } catch (NoSuchMethodException e) {
                    String msg = String
                            .format("未找到功能注入后需要执行的方法, class:%s, field:%s, afterExecute:%s", bean.getClass().getName(),
                                    field.getName(), dependent.afterExecute());
                    throw new BusinessException("execute.method.missing", msg, e);
                } catch (Exception e) {
                    String msg = String
                            .format("功能注入后需要执行的方法执行出错, class:%s, field:%s, afterExecute:%s", bean.getClass().getName(),
                                    field.getName(), dependent.afterExecute());
                    throw new BusinessException("after.execute.error", msg, e);
                }
            }
        }
    }

    @Override
    public void injectField(Object bean, String fieldName) {
        if (!VariousWaysManager.isInitialized()) {
            add(bean);
            LogUtil.start("可配置功能还未初始化，等待初始化后再注入, class:{}, field:{}", bean.getClass().getName(), fieldName);
            return;
        }

        try {
            Field field = bean.getClass().getDeclaredField(fieldName);

            // 只注入未赋值的
            if (ReflectUtil.getFieldValue(bean, field) != null) {
                injectFunction(bean, field);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
