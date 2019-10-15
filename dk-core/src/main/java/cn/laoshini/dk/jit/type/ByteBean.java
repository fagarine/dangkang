package cn.laoshini.dk.jit.type;

import cn.laoshini.dk.constant.BeanTypeEnum;

/**
 * @author fagarine
 */
public class ByteBean extends AbstractNumericTypeBean<Byte> {
    @Override
    public BeanTypeEnum getType() {
        return BeanTypeEnum.BYTE;
    }

    @Override
    public Class<Byte> getValueType() {
        return required() ? byte.class : Byte.class;
    }

    @Override
    public String toString() {
        return "ByteBean{" + "name='" + name + '\'' + ", valueType=" + valueType + ", val=" + val + ", defaultVal="
                + defaultVal + ", description='" + description + '\'' + '}';
    }
}
