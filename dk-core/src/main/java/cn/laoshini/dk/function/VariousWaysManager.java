package cn.laoshini.dk.function;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;

import org.springframework.beans.BeansException;

import cn.laoshini.dk.annotation.ConfigurableFunction;
import cn.laoshini.dk.annotation.FunctionDependent;
import cn.laoshini.dk.annotation.FunctionVariousWays;
import cn.laoshini.dk.autoconfigure.DangKangFunctionProperties;
import cn.laoshini.dk.common.SpringContextHolder;
import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.util.ClassUtil;
import cn.laoshini.dk.util.ReflectHelper;
import cn.laoshini.dk.util.SpringUtils;
import cn.laoshini.dk.util.StringUtil;

/**
 * 可配置功能多实现管理类，关于可配置功能类，参见:{@link ConfigurableFunction}
 *
 * @author fagarine
 * @see ConfigurableFunction
 * @see FunctionVariousWays
 */
public enum VariousWaysManager {
    /**
     * 使用枚举实现单例
     */
    INSTANCE;

    /**
     * 记录所有已被ConfigurableFunction注解标记的可配置功能，key: 供以定义类
     */
    private final Map<Class<?>, VariousWaysFunctionBean> interfaceToFunction = new ConcurrentHashMap<>();

    /**
     * 记录单例实现类的单例对象，缓存当前实现类的实例，快速返回
     */
    private final Map<Class, Object> singletonFunctions = new ConcurrentHashMap<>();

    private final AtomicBoolean initialized = new AtomicBoolean();

    /**
     * 返回指定功能类的当前实现类对象
     *
     * @param functionInterface 可配置功能定义类
     * @param initArgs 如果当前被选择的实现类不是单例模式，需要传入其构造器需要的参数
     * @param <F> 可配置功能定义类的类型
     * @return 返回当前实现类对象，该方法不会返回null，但可能抛出异常
     */
    public static <F> F getCurrentImpl(Class<F> functionInterface, Object... initArgs) {
        F function = (F) INSTANCE.singletonFunctions.get(functionInterface);
        if (function == null) {
            function = getFunctionCurrentImpl(functionInterface, initArgs);
            if (function != null && currentIsSingleton(functionInterface)) {
                INSTANCE.singletonFunctions.put(functionInterface, function);
            }
        }
        return function;
    }

    public static <Function> Function getCurrentImpl(String key) {
        if (StringUtil.isEmptyString(key)) {
            return null;
        }

        for (VariousWaysFunctionBean bean : INSTANCE.interfaceToFunction.values()) {
            if (key.equals(bean.getFunctionConfigurationKey()) && !bean.isEmptyImpl()) {
                return (Function) bean.getCurrentImpl();
            }
        }
        return null;
    }

    /**
     * 返回指定功能类的当前实现类对象
     *
     * @param functionInterface 可配置功能定义类
     * @param initArgs 如果当前被选择的实现类不是单例模式，需要传入其构造器需要的参数
     * @param <FunctionType> 可配置功能定义类的类型
     * @return 返回当前实现类对象，该方法不会返回null，但可能抛出异常
     */
    @SuppressWarnings("unchecked")
    public static <FunctionType> FunctionType getFunctionCurrentImpl(Class<FunctionType> functionInterface,
            Object... initArgs) {
        VariousWaysFunctionBean<FunctionType> variousWaysFunctionBean = INSTANCE.interfaceToFunction
                .get(functionInterface);
        if (variousWaysFunctionBean == null) {
            throw new BusinessException("function.no.register", "该类不是可配置功能类: " + functionInterface.getName());
        }
        return variousWaysFunctionBean.getCurrentImpl(initArgs);
    }

    /**
     * 返回指定功能类的当前实现类对象
     *
     * @param functionInterface 可配置功能定义类
     * @param initArgs 如果当前被选择的实现类不是单例模式，需要传入其构造器需要的参数
     * @param argTypes 构造器需要的参数的类型，如果参数是原始数据类型，如int，需要通过该方法指定类型
     * @param <FunctionType> 可配置功能定义类的类型
     * @return 返回当前实现类对象，该方法不会返回null，但可能抛出异常
     */
    public static <FunctionType> FunctionType getFunctionCurrentImpl(Class<FunctionType> functionInterface,
            Object[] initArgs, Class<?>[] argTypes) {
        VariousWaysFunctionBean<FunctionType> variousWaysFunctionBean = INSTANCE.interfaceToFunction
                .get(functionInterface);
        if (variousWaysFunctionBean == null) {
            throw new BusinessException("function.no.register", "该类不是可配置功能类: " + functionInterface.getName());
        }
        return variousWaysFunctionBean.getCurrentImpl(initArgs, argTypes);
    }

    /**
     * 返回指定功能类，指定的实现类对象
     *
     * @param functionInterface 可配置功能定义类
     * @param key 实现类的key
     * @param initArgs 如果该实现类不是单例模式，需要传入其构造器需要的参数
     * @param <FunctionType> 可配置功能定义类的类型
     * @return 返回实现类对象，该方法不会返回null，但可能抛出异常
     */
    @SuppressWarnings("unchecked")
    public static <FunctionType> FunctionType getFunctionImplByKey(Class<FunctionType> functionInterface, String key,
            Object... initArgs) {
        VariousWaysFunctionBean<FunctionType> variousWaysFunctionBean = INSTANCE.interfaceToFunction
                .get(functionInterface);
        if (variousWaysFunctionBean == null) {
            throw new BusinessException("function.no.register", "该类不是可配置功能类: " + functionInterface.getName());
        }
        return variousWaysFunctionBean.getImplByKey(key, initArgs);
    }

    /**
     * 根据功能依赖注解，查找依赖的功能实现对象
     *
     * @param functionInterface 可配置功能定义类
     * @param mapping 功能依赖注解
     * @param <FunctionType> 可配置功能定义类的类型
     * @return 对象不存在时，仅在注解中配置了允许返回空，返回null，否则抛出异常
     */
    public static <FunctionType> FunctionType getFunctionImplByAnnotation(Class<FunctionType> functionInterface,
            FunctionDependent mapping) {
        VariousWaysFunctionBean<FunctionType> variousWaysFunctionBean = INSTANCE.interfaceToFunction
                .get(functionInterface);
        if (variousWaysFunctionBean == null) {
            throw new BusinessException("function.no.register", "该类不是可配置功能类: " + functionInterface.getName());
        }
        try {
            return variousWaysFunctionBean.getImplByKey(mapping.value());
        } catch (BusinessException e) {
            // Spring托管对象不存在，如果不允许返回空，继续抛出异常
            if (!mapping.nullable()) {
                throw e;
            }
        } catch (BeansException e) {
            // Spring托管对象不存在，如果不允许返回空，继续抛出异常
            if (!mapping.nullable()) {
                throw new BusinessException("function.impl.missing", "未找到指定功能实现", e);
            }
        }
        return null;
    }

    /**
     * 传入功能类当前的实现类是否是单例
     *
     * @param functionInterface 功能定义接口类
     * @return 仅在是可配置功能，且当前实现不为空，且实现类为单例时，返回true
     */
    public static boolean currentIsSingleton(Class<?> functionInterface) {
        VariousWaysFunctionBean<?> variousWaysFunctionBean = INSTANCE.interfaceToFunction.get(functionInterface);
        return variousWaysFunctionBean != null && variousWaysFunctionBean.currentIsSingleton();
    }

    /**
     * 可配置功能实现类注销，如果该功能已没有可用的实现类，将会连同该功能一起注销
     *
     * @param functionInterface 可配置功能定义类
     * @param key 实现类的key
     * @param <FunctionType> 可配置功能定义类的类型
     */
    @SuppressWarnings("unchecked")
    public static <FunctionType> void unregisterFunctionImpl(Class<FunctionType> functionInterface, String key) {
        VariousWaysFunctionBean<FunctionType> variousWaysFunctionBean = INSTANCE.interfaceToFunction
                .get(functionInterface);
        if (variousWaysFunctionBean.isCurrentKey(key)) {
            // 移除缓存记录
            INSTANCE.singletonFunctions.remove(functionInterface);
        }

        if (variousWaysFunctionBean.unregisterImpl(key)) {
            // 如果该功能已没有可用的实现类，从记录中移除
            INSTANCE.interfaceToFunction.remove(functionInterface);
        }
    }

    /**
     * 批量注销可配置功能实现类
     *
     * @param configurableFunctionMap 已注册的功能实现类相关记录，key: 功能定义类, value: 实现类的key
     */
    public static void batchUnregister(Map<Class<?>, String> configurableFunctionMap) {
        if (configurableFunctionMap != null && !configurableFunctionMap.isEmpty()) {
            for (Map.Entry<Class<?>, String> entry : configurableFunctionMap.entrySet()) {
                unregisterFunctionImpl(entry.getKey(), entry.getValue());
            }
        }
    }

    public static Map<String, String> getValidFunctions() {
        Map<String, String> functions = new HashMap<>(INSTANCE.interfaceToFunction.size());
        for (VariousWaysFunctionBean bean : INSTANCE.interfaceToFunction.values()) {
            if (bean.isEmptyImpl()) {
                continue;
            }
            functions.put(bean.getFunctionConfigurationKey(), bean.getInterfaceName());
        }
        return functions;
    }

    /**
     * 在jar包中查找并注册指定目录下，所有可配置功能（加载外置模块时调用）
     *
     * @param jarFile jar包对象
     * @param classLoader 类加载器
     * @param packageName 包前缀
     * @return 返回本次注册的功能和实现类的key
     */
    public static Map<Class<?>, String> findAndRegisterVariousWaysClassesInJar(JarFile jarFile, ClassLoader classLoader,
            String packageName) {
        findAndRegisterConfigurableFunctionsInJar(jarFile, classLoader, packageName);

        List<Class<?>> classes = ClassUtil
                .getAllClassByAnnotationInJarFile(jarFile, classLoader, packageName, FunctionVariousWays.class);
        return variousWaysClassProcess(classes);
    }

    /**
     * 查找并注册指定目录下，所有可配置功能（系统启动时调用）
     *
     * @param classLoader 类加载器
     * @param packageNames 包前缀
     */
    public static void findAndRegisterVariousWaysClasses(ClassLoader classLoader, String[] packageNames) {
        findAndRegisterConfigurableFunctions(classLoader, packageNames);

        List<Class<?>> classes = ClassUtil
                .getClassByAnnotationInPackages(classLoader, packageNames, FunctionVariousWays.class);
        variousWaysClassProcess(classes);

        INSTANCE.initialized.set(true);
    }

    private static Map<Class<?>, String> variousWaysClassProcess(List<Class<?>> classes) {
        Map<Class<?>, String> registerMap = new HashMap<>(classes.size());
        for (Class<?> clazz : classes) {
            if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
                continue;
            }

            FunctionVariousWays functionVariousWays = clazz.getAnnotation(FunctionVariousWays.class);
            boolean noFunction = registerIfInterfaceIsFunction(registerMap, clazz, functionVariousWays);

            if (noFunction) {
                noFunction = registerIfParentIsFunction(registerMap, clazz, functionVariousWays);
            }

            if (noFunction) {
                throw new BusinessException("function.no.register",
                        String.format("可配置功能未注册, 已标记的实现类:%s", clazz.getName()));
            }
        }

        // 刷新各功能当前使用的实现方式
        refreshSelectedImpl();

        return registerMap;
    }

    private static boolean registerIfInterfaceIsFunction(Map<Class<?>, String> registerMap, Class<?> clazz,
            FunctionVariousWays functionVariousWays) {
        boolean noFunction = true;
        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces != null && interfaces.length > 0) {
            for (Class<?> interfaceClass : interfaces) {
                if (interfaceClass.isAnnotationPresent(ConfigurableFunction.class)) {
                    registerFunctionImpl(interfaceClass, clazz, functionVariousWays);
                    registerMap.put(interfaceClass, functionVariousWays.value());
                    noFunction = false;
                }

                noFunction &= registerIfInterfaceIsFunction(registerMap, interfaceClass, functionVariousWays);
            }
        }
        return noFunction;
    }

    private static boolean registerIfParentIsFunction(Map<Class<?>, String> registerMap, Class<?> clazz,
            FunctionVariousWays functionVariousWays) {
        boolean noFunction = true;
        Class<?> superClass = clazz.getSuperclass();
        if (!Object.class.equals(superClass)) {
            if (superClass.isAnnotationPresent(ConfigurableFunction.class)) {
                registerFunctionImpl(superClass, clazz, functionVariousWays);
                registerMap.put(superClass, functionVariousWays.value());
                noFunction = false;
            }

            // 递归查找父类
            noFunction &= registerIfParentIsFunction(registerMap, superClass, functionVariousWays);

            // 查找父类的接口类
            noFunction &= registerIfInterfaceIsFunction(registerMap, superClass, functionVariousWays);
        }
        return noFunction;
    }

    /**
     * 刷新各功能当前使用的实现方式
     */
    public static void refreshSelectedImpl() {
        boolean vacant = SpringContextHolder.getBean(DangKangFunctionProperties.class).isVacant();
        for (VariousWaysFunctionBean variousWaysFunctionBean : INSTANCE.interfaceToFunction.values()) {
            String property = SpringContextHolder.getProperty(variousWaysFunctionBean.getFunctionConfigurationKey());
            if (property == null) {
                property = Constants.DEFAULT_PROPERTY_NAME;
            }
            variousWaysFunctionBean.setSelectedImpl(property, vacant);
        }
    }

    private static <F> void registerFunctionImpl(Class<F> functionClass, Class<?> implClass,
            FunctionVariousWays implAnnotation) {
        if (!functionClass.isAssignableFrom(implClass)) {
            throw new IllegalArgumentException("方法错误调用");
        }

        @SuppressWarnings("unchecked")
        VariousWaysFunctionBean<F> variousWaysFunctionBean = INSTANCE.interfaceToFunction.get(functionClass);
        String implKey = implAnnotation.value();
        if (StringUtil.isEmptyString(implKey)) {
            throw new BusinessException("function.name.null", String.format("功能的实现名称不能为空, key:%s, 功能定义类:%s, 实现类:%s",
                    variousWaysFunctionBean.getFunctionConfigurationKey(), functionClass.getName(),
                    implClass.getName()));
        }

        @SuppressWarnings("unchecked")
        Class<? extends F> implClazz = (Class<? extends F>) implClass;

        // 单例实现类的对象由Spring容器管理
        if (implAnnotation.singleton()) {
            // 如果已被Spring相关注解标记，跳过对象初始化
            if (!ReflectHelper.isSpringAnnotationPresent(implClazz)) {
                try {
                    SpringContextHolder.getBean(implClazz);
                } catch (BeansException e) {
                    SpringUtils.registerSpringBean(implClazz);
                }
            }
        }

        // 记录实现类
        variousWaysFunctionBean.addImplement(implKey, implClazz);
    }

    private static void findAndRegisterConfigurableFunctions(ClassLoader classLoader, String[] packageNames) {
        List<Class<?>> classes = ClassUtil
                .getClassByAnnotationInPackages(classLoader, packageNames, ConfigurableFunction.class);
        registerConfigurableFunctions(classes);
    }

    private static void findAndRegisterConfigurableFunctionsInJar(JarFile jarFile, ClassLoader classLoader,
            String packageName) {
        List<Class<?>> classes = ClassUtil
                .getAllClassByAnnotationInJarFile(jarFile, classLoader, packageName, ConfigurableFunction.class);
        registerConfigurableFunctions(classes);
    }

    private static void registerConfigurableFunctions(List<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            final String propertyKey = clazz.getAnnotation(ConfigurableFunction.class).key();
            if (StringUtil.isEmptyString(propertyKey)) {
                throw new BusinessException("function.key.null",
                        String.format("功能对应的配置项key不能为空, key:%s, 功能定义类:%s", propertyKey, clazz.getName()));
            }
            INSTANCE.interfaceToFunction.computeIfAbsent(clazz, k -> new VariousWaysFunctionBean<>(k, propertyKey));
        }
    }

    public static boolean isInitialized() {
        return INSTANCE.initialized.get();
    }
}
