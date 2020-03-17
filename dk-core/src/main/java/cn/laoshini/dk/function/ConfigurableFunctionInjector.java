package cn.laoshini.dk.function;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import cn.laoshini.dk.annotation.ConfigurableFunction;
import cn.laoshini.dk.annotation.FunctionDependent;
import cn.laoshini.dk.annotation.FunctionVariousWays;
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
    private static ConfigurableFunctionInjector instance = new ConfigurableFunctionInjector();

    private ConfigurableFunctionInjector() {
        ConfigurableFunctionInjectorProxy.setDelegate(this);
    }

    public static ConfigurableFunctionInjector getInstance() {
        return instance;
    }

    /**
     * 给所有依赖可配置功能的对象，注入可配置功能（首次注入），该方法需要在外置模块加载完成，所有可配置功能已初始化完成后执行
     */
    public static void injectWaitBeans() {
        // Spring托管对象和资源持有者对象的依赖，已经包含在这里面，不需要单独处理
        for (Object holder : getInstance().getWaitInjectionBeans()) {
            injectFunctionToBean(holder);
        }
        getInstance().clear();
    }

    /**
     * 查找模块内所有受容器管理的对象中，对可配置功能的依赖，并注入依赖
     */
    @Deprecated
    public static void findAndInjectFunctionByModule(ModuleClassLoader classLoader) {
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
        List<Field> fields = new LinkedList<>();
        boolean isStatic = bean instanceof Class;
        Class<?> clazz = isStatic ? (Class<?>) bean : bean.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (isStatic && !Modifier.isStatic(field.getModifiers())) {
                continue;
            }

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
     * 重新注入可配置功能依赖对象的值
     *
     * @param bean 依赖可配置功能的对象
     * @param field 声明依赖的变量
     */
    static void reinjectFieldDependent(Object bean, Field field) {
        FunctionDependent dependent = field.getAnnotation(FunctionDependent.class);
        Object impl = VariousWaysManager.getFunctionImplByAnnotation(field.getType(), dependent, null, null);
        injectFunction(bean, field, impl, dependent);
    }

    /**
     * 注入可配置功能依赖（首次注入）
     *
     * @param bean 依赖可配置功能的对象
     * @param field 声明依赖的变量
     */
    private static void injectFunction(Object bean, Field field) {
        if (Modifier.isFinal(field.getModifiers())) {
            throw new BusinessException("function.field.final", "可配置功能的依赖变量不能使用final修饰:" + field.getName());
        }

        // 检查Field的依赖是否符合要求
        Class<?> functionInterface = validateDependentFunction(field);

        Object impl;
        FunctionDependent dependent = field.getAnnotation(FunctionDependent.class);
        if (Func.class.equals(field.getType())) {
            // Func形式依赖的注入
            impl = Func.ofDependent(functionInterface, dependent);
        } else {
            // 直接依赖注入
            impl = VariousWaysManager.getFunctionImplByAnnotation(functionInterface, dependent, null, null);
        }

        // 注解注入允许返回空（如果注解中不允许为空，会在前面获取实现对象时抛出异常），仅当返回不为空时注入
        if (impl != null) {
            // 记录依赖于可配置功能的field
            ConfigurableFunction function = functionInterface.getAnnotation(ConfigurableFunction.class);
            FunctionDependentManager.registerDependent(function.key(), field);

            injectFunction(bean, field, impl, dependent);
        }
    }

    private static void injectFunction(Object bean, Field field, Object impl, FunctionDependent dependent) {
        // 注入值
        ReflectUtil.setFieldValue(bean, field, impl);

        // 执行功能注入后逻辑
        if (StringUtil.isNotEmptyString(dependent.initMethod())) {
            try {
                ReflectUtil.invokeMethodAnyway(bean, dependent.initMethod());
            } catch (NoSuchMethodException e) {
                String msg;
                if (bean instanceof Class) {
                    // 静态变量注入
                    String name = ((Class) bean).getName();
                    msg = String
                            .format("未找到功能注入后需要执行的static方法, class:%s, field:%s, initMethod:%s", name, field.getName(),
                                    dependent.initMethod());
                } else {
                    msg = String.format("未找到功能注入后需要执行的方法, class:%s, field:%s, initMethod:%s", bean.getClass().getName(),
                            field.getName(), dependent.initMethod());
                }
                throw new BusinessException("execute.method.missing", msg, e);
            } catch (Exception e) {
                String msg = String
                        .format("功能注入后需要执行的方法执行出错, class:%s, field:%s, initMethod:%s", bean.getClass().getName(),
                                field.getName(), dependent.initMethod());
                throw new BusinessException("after.execute.error", msg, e);
            }
        }
    }

    private static Class<?> validateDependentFunction(Field field) {
        Class<?> functionInterface = field.getType();
        String className = field.getDeclaringClass().getName();
        if (Func.class.equals(functionInterface)) {
            // 使用Func延迟依赖加载功能，需要判断Func中的具体类型
            Class<?> genericType = ReflectUtil.getFieldGenericType(field);
            if (genericType == null || Object.class.equals(genericType) || genericType.isArray() || genericType
                    .isAnnotation()) {
                String message = String.format("[%s.%s]使用Func依赖的[%s]不是可配置功能类", className, field.getName(),
                        functionInterface.getName());
                throw new BusinessException("function.dependent.error", message);
            }
            functionInterface = genericType;
        }
        if (!functionInterface.isAnnotationPresent(ConfigurableFunction.class)) {
            // 不允许用户直接依赖功能的实现类
            if (functionInterface.isAnnotationPresent(FunctionVariousWays.class)) {
                String message = String
                        .format("[%s.%s]依赖的[%s]了可配置功能的实现类，Field的类型应当为可配置功能的声明类，而不是实现类", className, field.getName(),
                                functionInterface.getName());
                throw new BusinessException("function.impl.dependent", message);
            }

            String message = String
                    .format("[%s.%s]依赖的[%s]不是可配置功能类，可配置功能类应当被@ConfigurableFunction标记", className, field.getName(),
                            functionInterface.getName());
            throw new BusinessException("function.need.register", message);
        }
        return functionInterface;
    }

    @Override
    public void injectField(Object bean, String fieldName) {
        if (!VariousWaysManager.isInitialized()) {
            add(bean);
            LogUtil.start("可配置功能还未初始化，等待初始化后再注入, class:{}, field:{}", bean.getClass().getName(), fieldName);
            return;
        }

        try {
            Class<?> clazz;
            if (bean instanceof Class) {
                clazz = (Class<?>) bean;
            } else {
                clazz = bean.getClass();
            }
            Field field = clazz.getDeclaredField(fieldName);

            // 只注入未赋值的
            if (ReflectUtil.getFieldValue(bean, field) == null) {
                injectFunction(bean, field);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
