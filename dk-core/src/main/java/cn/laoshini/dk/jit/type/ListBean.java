package cn.laoshini.dk.jit.type;

import java.util.List;

import cn.laoshini.dk.constant.BeanTypeEnum;

/**
 * @author fagarine
 */
public class ListBean<T> extends AbstractTypeBean<List<T>> {

    private Class<?> actualClass;

    @Override
    public BeanTypeEnum getType() {
        return BeanTypeEnum.LIST;
    }

    public Class<?> getActualClass() {
        return actualClass;
    }

    public void setActualClass(Class<?> actualClass) {
        this.actualClass = actualClass;
    }

    @Override
    public String toString() {
        return "ListBean{" + "actualClass=" + actualClass + ", name='" + name + '\'' + ", valueType=" + valueType
                + ", val=" + val + ", defaultVal=" + defaultVal + ", description='" + description + '\'' + '}';
    }
}
