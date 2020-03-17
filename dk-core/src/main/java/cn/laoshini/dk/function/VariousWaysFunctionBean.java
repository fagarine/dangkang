package cn.laoshini.dk.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.StringUtils;

import cn.laoshini.dk.annotation.FunctionVariousWays;
import cn.laoshini.dk.common.SpringContextHolder;
import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.ReflectUtil;
import cn.laoshini.dk.util.StringUtil;

/**
 * 多实现功能记录类（内存对象）
 *
 * @author fagarine
 */
final class VariousWaysFunctionBean<FunctionType> {

    /**
     * 功能定义类，父类
     */
    private Class<FunctionType> interfaceClass;

    /**
     * 功能对应的配置项key，用来在配置项中选择使用哪个具体的实现
     */
    private String functionConfigurationKey;

    /**
     * 记录所有已实现该功能的类
     */
    private Map<String, Class<? extends FunctionType>> implementedMap = new ConcurrentHashMap<>();

    /**
     * 当前选择的实现类的FunctionVariousWays注解信息
     */
    private FunctionVariousWays currentImplAnnotation;

    /**
     * 当前选择的实现的名称
     */
    private String currentImplName;

    /**
     * 当前选择的实现类
     */
    private Class<? extends FunctionType> currentImplClass;

    /**
     * 配置项选择的实现key
     */
    private String configValue;

    /**
     * 记录当前默认实现是否已变更
     */
    private boolean currentImplChanged;

    VariousWaysFunctionBean(Class<FunctionType> interfaceClass, String functionConfigurationKey) {
        this.interfaceClass = interfaceClass;
        this.functionConfigurationKey = functionConfigurationKey;
    }

    /**
     * 添加一个实现类记录
     *
     * @param name 实现的名称，功能内所有实现方式唯一
     * @param implClass 实现类
     */
    void addImplement(String name, Class<? extends FunctionType> implClass) {
        implementedMap.compute(name.toUpperCase(), (k, oldValue) -> {
            if (oldValue != null && !oldValue.equals(implClass)) {
                if (!oldValue.getName().equalsIgnoreCase(implClass.getName())) {
                    // 实现key相同的，日志记录提醒
                    LogUtil.error("不同的实现方式注册了同一实现key, key:[{}], class1:{}, class2:{}，前者将被后者覆盖", k, oldValue.getName(),
                            implClass.getName());
                }
            }
            return implClass;
        });
    }

    /**
     * 设置当前被选择的具体实现
     *
     * @param implName 实现的名称
     * @param vacant 是否允许功能的实现类缺失
     */
    void setSelectedImpl(String implName, boolean vacant) {
        if (implName == null) {
            this.currentImplName = Constants.DEFAULT_PROPERTY_NAME;
        } else {
            this.currentImplName = implName.toUpperCase();
        }

        Class<? extends FunctionType> originImpl = currentImplClass;
        String key = functionConfigurationKey;
        String className = interfaceClass.getName();
        currentImplClass = implementedMap.get(this.currentImplName);
        if (currentImplClass == null) {
            this.currentImplAnnotation = null;

            // 未找到对应实现类
            if (implName != null) {
                throw new BusinessException("function.impl.not.found",
                        String.format("未找到[%s]对应功能类[%s]的key为[%s]的实现类", key, className, implName));
            }

            if (implementedMap.isEmpty()) {
                // 如果没有找到任何实现类，在不允许实现类缺失的情况下，抛出异常
                if (!vacant) {
                    throw new BusinessException("function.impl.vacant",
                            String.format("未找到[%s]对应功能类[%s]的任何实现类", key, className));
                } else {
                    LogUtil.error("未找到[{}]对应功能类[{}]的任何实现类，当前为允许实现类缺失模式，不抛出异常", key, className);
                }
            } else {
                // 如果用户没有配置选择哪个实现，又找不到缺省实现方式，则随机选择一个实现对象
                this.currentImplName = new ArrayList<>(implementedMap.keySet()).get(0);
                this.currentImplClass = implementedMap.get(this.currentImplName);
                this.currentImplAnnotation = currentImplClass.getAnnotation(FunctionVariousWays.class);
            }
        } else {
            this.currentImplAnnotation = currentImplClass.getAnnotation(FunctionVariousWays.class);
        }
        currentImplChanged = originImpl != currentImplClass;
    }

    /**
     * 获取当前实现类的实例，如果是单例，直接返回单例对象，否则新建一个对象返回
     *
     * @param initArgs 如果不是单例模式，传入对象初始化参数
     * @return 该方法不会返回null，但可能抛出异常
     */
    FunctionType getCurrentImpl(Object... initArgs) {
        if (currentIsSingleton()) {
            return SpringContextHolder.getBean(currentImplClass);
        }

        FunctionType impl = ReflectUtil.newInstance(currentImplClass, initArgs);
        if (impl == null) {
            throw new BusinessException("function.impl.create.fail",
                    String.format("类[%s]的实例创建失败，参数:%s", currentImplClass, Arrays.toString(initArgs)));
        }
        return impl;
    }

    /**
     * 获取当前实现类的实例，如果是单例，直接返回单例对象，否则新建一个对象返回
     * <p>
     * 与{@link #getCurrentImpl(Object...)}的区别，该方法指定了参数的具体类型，如果参数是原始数据类型，如int，需要通过这样的方法实现
     * </p>
     *
     * @param initArgs 如果不是单例模式，传入对象初始化参数
     * @param argTypes 初始化参数的类型
     * @return 该方法不会返回null，但可能抛出异常
     */
    FunctionType getCurrentImpl(Object[] initArgs, Class<?>[] argTypes) {
        if (currentIsSingleton()) {
            return SpringContextHolder.getBean(currentImplClass);
        }

        FunctionType impl = ReflectUtil.newInstance(currentImplClass, initArgs, argTypes);
        if (impl == null) {
            throw new BusinessException("function.impl.create.fail",
                    String.format("类[%s]的实例创建失败，参数:%s", currentImplClass.getName(), Arrays.toString(initArgs)));
        }
        return impl;
    }

    /**
     * 根据实现类的key获取对应的实例对象
     *
     * @param key 实现类注册的key
     * @param initArgs 如果不是单例模式，传入对象初始化参数
     * @return 该方法不会返回null，但可能抛出异常
     */
    FunctionType getImplByKey(String key, Object[] initArgs, Class[] types) {
        Class<? extends FunctionType> clazz = implementedMap.get(key.toUpperCase());
        if (clazz == null) {
            throw new BusinessException("function.impl.not.found",
                    String.format("未找到[%s]对应功能的key为[%s]的实现类", functionConfigurationKey, key));
        }

        return getFunctionImpl(clazz, false, initArgs, types);
    }

    private FunctionType getFunctionImpl(Class<? extends FunctionType> clazz, boolean nullable, Object[] initArgs,
            Class[] types) {
        FunctionVariousWays annotation = clazz.getAnnotation(FunctionVariousWays.class);
        if (annotation.singleton()) {
            String beanName = StringUtils.uncapitalize(clazz.getSimpleName());
            return SpringContextHolder.getBean(beanName);
        } else {
            FunctionType impl = ReflectUtil.newInstanceByType(clazz, initArgs, types);
            if (impl == null && !nullable) {
                // 实例创建失败，向上抛出异常
                throw new BusinessException("function.impl.create.fail",
                        String.format("类[%s]的实例创建失败，参数:%s", clazz.getName(), Arrays.toString(initArgs)));
            }
            return impl;
        }
    }

    FunctionType getImplByAnnotation(String key, boolean nullable, Object[] initArgs, Class[] types) {
        String k = null;
        Class<? extends FunctionType> clazz = currentImplClass;
        if (StringUtil.isNotEmptyString(key)) {
            k = key.toUpperCase();
            clazz = implementedMap.get(k);
        }

        if (clazz == null) {
            // 如果用户没有设置key，该功能又不存在缺省实现，则使用当前选择的实现类
            if (k == null) {
                // 如果当前默认实现类为null，返回null
                if (currentImplClass == null) {
                    return null;
                }
                clazz = currentImplClass;
            } else {
                // 找不到用户指定的实现，返回null
                return null;
            }
        }

        return getFunctionImpl(clazz, nullable, initArgs, types);
    }

    /**
     * 当前选择的实现类是否是单例
     *
     * @return 仅在当前实现不为空且为单例时，返回true
     */
    boolean currentIsSingleton() {
        return this.currentImplAnnotation != null && this.currentImplAnnotation.singleton();
    }

    /**
     * 注销指定名称的实现类，返回是否还有可用的实现类
     *
     * @param key 实现类的key
     * @return 返回该功能是否已无可用的实现类
     */
    boolean unregisterImpl(String key) {
        implementedMap.remove(key);
        return implementedMap.isEmpty();
    }

    /**
     * 传入的key是否是当前选择实现类的key
     *
     * @param key 实现类的key
     * @return 传入的key是否是当前选择实现类的key
     */
    boolean isCurrentKey(String key) {
        return currentImplName != null && currentImplName.equalsIgnoreCase(key);
    }

    /**
     * 判断当前是否没有可用实现类
     *
     * @return 返回该功能是否已无可用的实现类
     */
    boolean isEmptyImpl() {
        return implementedMap.isEmpty();
    }

    String getInterfaceName() {
        return getInterfaceClass().getName();
    }

    Class<FunctionType> getInterfaceClass() {
        return interfaceClass;
    }

    void setInterfaceClass(Class<FunctionType> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    String getFunctionConfigurationKey() {
        return functionConfigurationKey;
    }

    void setFunctionConfigurationKey(String functionConfigurationKey) {
        this.functionConfigurationKey = functionConfigurationKey;
    }

    Map<String, Class<? extends FunctionType>> getImplementedMap() {
        return implementedMap;
    }

    String getCurrentImplName() {
        return currentImplName;
    }

    boolean isCurrentImplChanged() {
        return currentImplChanged;
    }

    void setCurrentImplChanged(boolean currentImplChanged) {
        this.currentImplChanged = currentImplChanged;
    }
}
