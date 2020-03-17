package cn.laoshini.dk.support;

import java.util.Collection;
import java.util.function.Supplier;

import lombok.ToString;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;

import cn.laoshini.dk.common.SpringContextHolder;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.StringUtil;

/**
 * 参数提供者，如果一个参数允许用户通过多种方式设置，但是又不知道用户具体使用了哪种方式，可以通过该类来获取用户实际提供的值
 * <p>
 * 目前已支持的参数提供方式：
 * <ol>
 * <li>直接提供值</li>
 * <li>通过{@link Supplier}的Lambda表达式提供</li>
 * <li>通过配置项参数提供（分为默认配置项参数和自定义配置项参数两种）</li>
 * </ol>
 * </p>
 *
 * @author fagarine
 */
@ToString
public class ParamSupplier<T> implements IPropertyRefreshable {

    /**
     * 参数名称
     */
    private String name;

    /**
     * 记录实际的参数值
     */
    private T value;

    /**
     * 参数类型
     */
    private Class<T> type;

    /**
     * 如果参数通过方法来获取，则将方法记录在Lambda表达式中
     */
    private Supplier<T> supplier;

    /**
     * 如果参数拥有默认的Lambda表达式，记录该表达式
     */
    private Supplier<T> defaultSupplier;

    /**
     * 如果参数通过配置项提供，记录配置项的key
     */
    private String propertyKey;

    /**
     * 如果参数拥有默认的配置项key，记录该key
     */
    private String defaultPropertyKey;

    private volatile boolean evaluated;

    private ParamSupplier() {
        RefresherContainer.addPropertyRefresher(this);
    }

    public static <T> ParamSupplier<T> of(String name, Class<T> type) {
        ParamSupplier<T> supplier = new ParamSupplier<>();
        supplier.name = name;
        supplier.type = type;
        return supplier;
    }

    public static <T> ParamSupplier<T> ofValue(String name, Class<T> type, T value) {
        ParamSupplier<T> supplier = new ParamSupplier<>();
        supplier.name = name;
        supplier.type = type;
        supplier.setValue(value);
        supplier.evaluated = true;
        return supplier;
    }

    public static <T> ParamSupplier<T> ofSupplier(String name, Class<T> type, Supplier<T> valueSupplier) {
        ParamSupplier<T> supplier = new ParamSupplier<>();
        supplier.name = name;
        supplier.type = type;
        supplier.supplier = valueSupplier;
        return supplier;
    }

    public static <T> ParamSupplier<T> ofDefaultSupplier(String name, Class<T> type, Supplier<T> defaultSupplier) {
        ParamSupplier<T> supplier = new ParamSupplier<>();
        supplier.name = name;
        supplier.type = type;
        supplier.defaultSupplier = defaultSupplier;
        return supplier;
    }

    public static <T> ParamSupplier<T> ofProperty(String name, Class<T> type, String propertyKey) {
        ParamSupplier<T> supplier = new ParamSupplier<>();
        supplier.name = name;
        supplier.type = type;
        supplier.propertyKey = propertyKey;
        return supplier;
    }

    public static <T> ParamSupplier<T> ofDefaultProperty(String name, Class<T> type, String defaultPropertyKey) {
        ParamSupplier<T> supplier = new ParamSupplier<>();
        supplier.name = name;
        supplier.type = type;
        supplier.defaultPropertyKey = defaultPropertyKey;
        return supplier;
    }

    /**
     * 获取参数的值
     *
     * @return 可能返回null
     */
    public T get() {
        if (getValue() == null && !evaluated) {
            synchronized (this) {
                if (!evaluated) {
                    boolean success = false;
                    // 优先从Lambda表达式中获取
                    if (supplier != null) {
                        setValue(supplier.get());
                        success = true;
                        LogUtil.debug("[{}]通过Lambda表达式读取到参数:[{}]", name, getValue());
                    } else if (StringUtil.isNotEmptyString(propertyKey)) {
                        // 如果用户传入了配置项key，尝试从配置项中获取
                        String property = SpringContextHolder.getProperty(propertyKey);
                        if (property != null) {
                            setValue(TypeUtils.cast(property, type, ParserConfig.getGlobalInstance()));
                            success = true;
                            LogUtil.debug("[{}]通过配置项[{}]读取到参数:[{}]", name, propertyKey, getValue());
                        } else {
                            LogUtil.info("[{}]通过配置项[{}]未能读取到参数", name, propertyKey);
                        }
                    }

                    // 如果未通过以上方式获取到参数值，但是参数拥有默认的Lambda表达式，尝试通过表达式获取
                    if (!success && defaultSupplier != null) {
                        setValue(defaultSupplier.get());
                        success = true;
                        LogUtil.info("[{}]未能成功获取参数值，通过默认Lambda表达式读取到参数:[{}]", name, getValue());
                    }

                    // 如果以上方式获取参数值失败，但是参数拥有默认的配置项key，尝试从默认配置项中获取
                    if (!success && StringUtil.isNotEmptyString(defaultPropertyKey)) {
                        String property = SpringContextHolder.getProperty(defaultPropertyKey);
                        if (property != null) {
                            setValue(TypeUtils.cast(property, type, ParserConfig.getGlobalInstance()));
                            LogUtil.debug("[{}]通过默认配置项[{}]读取到参数:[{}]", name, defaultPropertyKey, getValue());
                        } else {
                            LogUtil.info("[{}]通过默认配置项[{}]未能读取到参数，参数值获取失败", name, defaultPropertyKey);
                        }
                    }

                    evaluated = true;
                }
            }
        }
        return getValue();
    }

    /**
     * 重新获取参数值，并返回
     *
     * @param propertyKeys 需要刷新的配置项key
     */
    @Override
    public void refresh(Collection<String> propertyKeys) {
        String key;
        if ((key = propertyKey) == null && (key = defaultPropertyKey) == null) {
            return;
        }
        if (propertyKeys != null && !propertyKeys.contains(key)) {
            return;
        }

        synchronized (this) {
            value = null;
            evaluated = false;
            get();
        }
    }

    private T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
        evaluated = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<T> getType() {
        return type;
    }

    public void setType(Class<T> type) {
        this.type = type;
    }

    public Supplier<T> getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public Supplier<T> getDefaultSupplier() {
        return defaultSupplier;
    }

    public void setDefaultSupplier(Supplier<T> defaultSupplier) {
        this.defaultSupplier = defaultSupplier;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public void setPropertyKey(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public String getDefaultPropertyKey() {
        return defaultPropertyKey;
    }

    public void setDefaultPropertyKey(String defaultPropertyKey) {
        this.defaultPropertyKey = defaultPropertyKey;
    }
}
