package cn.laoshini.dk.jit.type;

import cn.laoshini.dk.constant.BeanTypeEnum;

/**
 * @author fagarine
 */
public class BooleanBean extends AbstractTypeBean<Boolean> {

    @Override
    public BeanTypeEnum getType() {
        return BeanTypeEnum.BOOLEAN;
    }

    @Override
    public Class<Boolean> getValueType() {
        return required() ? boolean.class : Boolean.class;
    }

    @Override
    public String toString() {
        return "BooleanBean{" + "name='" + name + '\'' + ", valueType=" + valueType + ", val=" + val + ", defaultVal="
                + defaultVal + ", description='" + description + '\'' + '}';
    }
}
