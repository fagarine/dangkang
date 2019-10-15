package cn.laoshini.dk.jit.type;

import java.io.Serializable;
import java.util.List;

import cn.laoshini.dk.constant.BeanTypeEnum;

/**
 * @author fagarine
 */
public interface ITypeBean<T> extends Serializable {

    BeanTypeEnum getType();

    String getName();

    String getDescription();

    Class<T> getValueType();

    T getVal();

    T getDefaultVal();

    /**
     * 值是否为空
     *
     * @return 值是否为空
     */
    default boolean isNull() {
        return getVal() == null && getDefaultVal() == null;
    }

    /**
     * 是否不允许为空
     *
     * @return 返回该值是否不允许为空
     */
    default boolean required() {
        BeanTypeEnum beanType = getType();
        return !BeanTypeEnum.ORDINARY.equals(beanType) && !BeanTypeEnum.COMPOSITE.equals(beanType) && !BeanTypeEnum.LIST
                .equals(beanType) && getDefaultVal() != null;
    }

    default String getValueClassName() {
        BeanTypeEnum beanType = getType();
        if (BeanTypeEnum.ORDINARY.equals(beanType)) {
            return getValueType().getName();
        } else if (BeanTypeEnum.COMPOSITE.equals(beanType) || BeanTypeEnum.LIST.equals(beanType)) {
            return List.class.getName();
        }
        return getValueType().getSimpleName();
    }

    default String getGenericClassName() {
        if (BeanTypeEnum.LIST.equals(getType())) {
            ListBean listBean = (ListBean) this;
            return listBean.getActualClass().getName();
        }
        return null;
    }
}
