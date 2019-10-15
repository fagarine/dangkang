package cn.laoshini.dk.jit.type;

import cn.laoshini.dk.constant.BeanTypeEnum;

/**
 * @author fagarine
 */
public class LongBean extends AbstractNumericTypeBean<Long> {

    @Override
    public BeanTypeEnum getType() {
        return BeanTypeEnum.LONG;
    }

    @Override
    public Class<Long> getValueType() {
        return required() ? long.class : Long.class;
    }

    @Override
    public String toString() {
        return "LongBean{" + "name='" + name + '\'' + ", valueType=" + valueType + ", val=" + val + ", defaultVal="
                + defaultVal + ", description='" + description + '\'' + '}';
    }
}
