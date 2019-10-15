package cn.laoshini.dk.jit.type;

import cn.laoshini.dk.constant.BeanTypeEnum;

/**
 * @author fagarine
 */
public class ShortBean extends AbstractNumericTypeBean<Short> {
    @Override
    public BeanTypeEnum getType() {
        return BeanTypeEnum.SHORT;
    }

    @Override
    public Class<Short> getValueType() {
        return required() ? short.class : Short.class;
    }

    @Override
    public String toString() {
        return "ShortBean{" + "name='" + name + '\'' + ", valueType=" + valueType + ", val=" + val + ", defaultVal="
                + defaultVal + ", description='" + description + '\'' + '}';
    }
}
