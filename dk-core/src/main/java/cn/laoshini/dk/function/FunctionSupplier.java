package cn.laoshini.dk.function;

import java.lang.ref.WeakReference;
import java.util.function.Function;

import cn.laoshini.dk.annotation.ConfigurableFunction;
import cn.laoshini.dk.annotation.FunctionDependent;
import cn.laoshini.dk.annotation.FunctionVariousWays;
import cn.laoshini.dk.domain.common.ArrayTuple;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.ReflectUtil;

/**
 * @author fagarine
 */
final class FunctionSupplier<F> {

    private String name;

    /**
     * 记录实际的参数值
     */
    private WeakReference<F> value;

    /**
     * 如果是通过功能类型获取，记录功能类型
     */
    private WeakReference<Class<F>> type;

    /**
     * 如果是注解式的依赖，记录注解信息
     */
    private WeakReference<FunctionDependent> dependent;

    /**
     * 如果要获取指定实现，记录实现的key
     */
    private String implKey;

    /**
     * 如果是通过功能key获取，记录功能key
     */
    private String functionKey;

    private Function<ArrayTuple<Object, Class>, F> supplier;

    /**
     * 记录实现对象是否是单例
     */
    private boolean singleton;

    private volatile boolean evaluated;

    private FunctionSupplier(String name, String functionKey) {
        this.name = name;
        this.functionKey = functionKey;
    }

    private FunctionSupplier(String name, Class<F> functionInterface) {
        this.name = name;
        this.type = new WeakReference<>(functionInterface);
    }

    private static String getFunctionKey(Class<?> functionInterface) {
        ConfigurableFunction function = functionInterface.getAnnotation(ConfigurableFunction.class);
        if (function == null) {
            throw new BusinessException("not.configurable.function", functionInterface.getName() + "不是可配置功能声明类");
        }
        return functionInterface.getAnnotation(ConfigurableFunction.class).key();
    }

    static <F> FunctionSupplier<F> ofFunctionDependent(String name, Class<F> functionInterface,
            FunctionDependent dependent) {
        FunctionSupplier<F> supplier = new FunctionSupplier<>(name, functionInterface);
        supplier.functionKey = getFunctionKey(functionInterface);
        supplier.setDependent(dependent);
        supplier.supplier = (paramTuple) -> {
            Class<F> type = supplier.getType();
            if (type == null) {
                LogUtil.error("[{}]找不到可配置功能的类型", supplier.getName());
                return null;
            }

            FunctionDependent fd = supplier.getDependent();
            if (fd == null) {
                LogUtil.error("[{}]所声明依赖的[{}]功能的注解对象丢失", supplier.getName(), type.getName());
                return null;
            }

            F function;
            if (paramTuple != null && CollectionUtil.isNotEmpty(paramTuple.getV1())) {
                function = VariousWaysManager
                        .getFunctionImplByAnnotation(type, fd, paramTuple.getV1(), paramTuple.getV2());
            } else {
                function = VariousWaysManager.getFunctionImplByAnnotation(type, fd, null, null);
            }
            supplier.evaluateSingleton(function);
            return function;
        };
        return supplier;
    }

    static <F> FunctionSupplier<F> ofFunctionInterface(String name, Class<F> functionInterface) {
        FunctionSupplier<F> supplier = new FunctionSupplier<>(name, functionInterface);
        supplier.functionKey = getFunctionKey(functionInterface);
        supplier.supplier = (paramTuple) -> {
            Class<F> type = supplier.getType();
            if (type == null) {
                LogUtil.error("[{}]找不到可配置功能的类型", supplier.getName());
                return null;
            }

            F function;
            if (paramTuple != null && CollectionUtil.isNotEmpty(paramTuple.getV1())) {
                function = VariousWaysManager.getCurrentImplWithType(type, paramTuple.getV1(), paramTuple.getV2());
            } else {
                function = VariousWaysManager.getCurrentImpl(type);
            }
            supplier.evaluateSingleton(function);
            return function;
        };
        return supplier;
    }

    static <F> FunctionSupplier<F> ofImplKey(String name, Class<F> functionInterface, String implKey) {
        FunctionSupplier<F> supplier = new FunctionSupplier<>(name, functionInterface);
        supplier.functionKey = getFunctionKey(functionInterface);
        supplier.setImplKey(implKey);
        supplier.supplier = (paramTuple) -> {
            Class<F> type = supplier.getType();
            if (type == null) {
                LogUtil.error("[{}]找不到可配置功能的类型", supplier.getName());
                return null;
            }

            F function;
            String key = supplier.getImplKey();
            if (paramTuple != null && CollectionUtil.isNotEmpty(paramTuple.getV1())) {
                function = VariousWaysManager
                        .getFunctionImplByKeyWithType(type, key, paramTuple.getV1(), paramTuple.getV2());
            } else {
                function = VariousWaysManager.getFunctionImplByKey(type, key);
            }
            supplier.evaluateSingleton(function);
            return function;
        };
        return supplier;
    }

    static <F> FunctionSupplier<F> ofFunctionKey(String name, String functionKey) {
        FunctionSupplier<F> supplier = new FunctionSupplier<>(name, functionKey);
        supplier.supplier = (paramTuple) -> {
            F function;
            String key = supplier.getFunctionKey();
            if (paramTuple != null && CollectionUtil.isNotEmpty(paramTuple.getV1())) {
                function = VariousWaysManager.getCurrentImpl(key, paramTuple.getV1(), paramTuple.getV2());
            } else {
                function = VariousWaysManager.getCurrentImpl(key, null, null);
            }
            supplier.evaluateSingleton(function);
            return function;
        };
        return supplier;
    }

    private void evaluateSingleton(F function) {
        if (!evaluated && function != null) {
            FunctionVariousWays ways = function.getClass().getAnnotation(FunctionVariousWays.class);
            singleton = ways.singleton();
            setValue(function);
        }
    }

    F get(Object... params) {
        if (CollectionUtil.isNotEmpty(params)) {
            return getByParams(new ArrayTuple<>(params, ReflectUtil.getParamClasses(params)));
        }
        return getByParams(null);
    }

    F getByType(Object[] params, Class[] types) {
        if (CollectionUtil.isNotEmpty(params)) {
            if (CollectionUtil.isEmpty(types) || types.length < params.length) {
                types = ReflectUtil.getParamClasses(params);
            }
            return getByParams(new ArrayTuple<>(params, types));
        }
        return getByParams(null);
    }

    private F getByParams(ArrayTuple<Object, Class> paramTuple) {
        // 非单例，每次都创建一个新的实例返回
        if (!singleton) {
            return supplier.apply(paramTuple);
        }

        if (getValue() == null && !evaluated) {
            synchronized (this) {
                if (!evaluated) {
                    setValue(supplier.apply(paramTuple));
                }
            }
        }
        return getValue();
    }

    private F getValue() {
        return value == null ? null : value.get();
    }

    public void setValue(F value) {
        if (value == null) {
            this.value = null;
        } else {
            this.value = new WeakReference<>(value);
        }
        evaluated = true;
    }

    /**
     * 刷新
     */
    void refresh() {
        // 清空当前记录值，等待代码调用时被动加载
        synchronized (this) {
            if (getValue() != null) {
                value.clear();
            }
            value = null;
            singleton = false;
            evaluated = false;
        }
    }

    boolean isValid() {
        return getType() != null || VariousWaysManager.containsFunction(getFunctionKey());
    }

    void clear() {
        value.clear();
        type.clear();
        dependent.clear();
        name = null;
        implKey = null;
        functionKey = null;
        singleton = false;
        evaluated = false;
        supplier = null;
    }

    public Class<F> getType() {
        return type == null ? null : type.get();
    }

    public FunctionDependent getDependent() {
        return dependent == null ? null : dependent.get();

    }

    public void setDependent(FunctionDependent dependent) {
        if (dependent == null) {
            this.dependent = null;
        } else {
            this.dependent = new WeakReference<>(dependent);
        }
    }

    public String getImplKey() {
        return implKey;
    }

    public void setImplKey(String implKey) {
        this.implKey = implKey;
    }

    public String getName() {
        return name;
    }

    public String getFunctionKey() {
        return functionKey;
    }
}
