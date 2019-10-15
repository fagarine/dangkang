package cn.laoshini.dk.jit.type;

import cn.laoshini.dk.constant.BeanTypeEnum;

/**
 * @author fagarine
 */
public class IntegerBean extends AbstractNumericTypeBean<Integer> {

    @Override
    public BeanTypeEnum getType() {
        return BeanTypeEnum.INTEGER;
    }

    @Override
    public Class<Integer> getValueType() {
        return required() ? int.class : Integer.class;
    }

    @Override
    public String toString() {
        return "IntegerBean{" + "name='" + name + '\'' + ", valueType=" + valueType + ", val=" + val + ", defaultVal="
                + defaultVal + ", description='" + description + '\'' + '}';
    }
}
