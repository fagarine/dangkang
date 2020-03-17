package cn.laoshini.dk.config.center.domain;

import java.lang.reflect.Field;

import cn.laoshini.dk.config.center.annotation.Property;
import cn.laoshini.dk.config.center.service.PropertiesService;
import cn.laoshini.dk.util.StringUtil;

/**
 * 记录被@{@link Property}标记了的{@link Field}的信息
 *
 * @author fagarine
 */
public class PropertyField {

    private Field field;

    private Class<?> classType;

    /**
     * 记录默认参数名称，即该字段对应在配置项中的参数名称
     */
    private String propertyKey;

    public PropertyField(Field field, Class<?> classType) {
        this.field = field;
        this.classType = classType;

        // 获取字段对应在配置项中的参数名称
        Property annotation = field.getAnnotation(Property.class);
        String paramName = annotation == null ? null : annotation.value();
        // 如果注解中未指明该字段使用的名称，则使用字段本身的名称
        if (StringUtil.isEmptyString(paramName)) {
            paramName = field.getName();
        }
        this.propertyKey = paramName;
    }

    /**
     * 获取字段在对象中的值，以String形式返回
     *
     * @param object
     * @return
     */
    public String getValueWithString(Object object) {
        return PropertiesService.getFieldWithString(field, object);
    }

    public Field getField() {
        return field;
    }

    public Class<?> getClassType() {
        return classType;
    }

    public String getPropertyKey() {
        return propertyKey;
    }
}
