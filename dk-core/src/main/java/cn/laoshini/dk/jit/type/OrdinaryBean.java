package cn.laoshini.dk.jit.type;

import cn.laoshini.dk.constant.BeanTypeEnum;

/**
 * @author fagarine
 */
public class OrdinaryBean<T> extends AbstractTypeBean<T> {
    @Override
    public BeanTypeEnum getType() {
        return BeanTypeEnum.ORDINARY;
    }

    @Override
    public String toString() {
        return "OrdinaryBean{" + "name='" + name + '\'' + ", valueType=" + valueType + ", val=" + val + ", defaultVal="
                + defaultVal + ", description='" + description + '\'' + '}';
    }
}
