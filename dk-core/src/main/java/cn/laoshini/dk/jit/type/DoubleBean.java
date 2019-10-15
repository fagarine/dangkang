package cn.laoshini.dk.jit.type;

import cn.laoshini.dk.constant.BeanTypeEnum;

/**
 * @author fagarine
 */
public class DoubleBean extends AbstractNumericTypeBean<Double> {

    @Override
    public BeanTypeEnum getType() {
        return BeanTypeEnum.DOUBLE;
    }

    @Override
    public Class<Double> getValueType() {
        return required() ? double.class : Double.class;
    }

    @Override
    public String toString() {
        return "DoubleBean{" + "name='" + name + '\'' + ", valueType=" + valueType + ", val=" + val + ", defaultVal="
                + defaultVal + ", description='" + description + '\'' + '}';
    }
}
