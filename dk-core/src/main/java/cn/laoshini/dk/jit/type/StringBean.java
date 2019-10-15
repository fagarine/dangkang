package cn.laoshini.dk.jit.type;

import cn.laoshini.dk.constant.BeanTypeEnum;

/**
 * @author fagarine
 */
public class StringBean extends AbstractTypeBean<String> {

    @Override
    public BeanTypeEnum getType() {
        return BeanTypeEnum.STRING;
    }

    @Override
    public Class<String> getValueType() {
        return String.class;
    }

    @Override
    public String toString() {
        return "StringBean{" + "name='" + name + '\'' + ", valueType=" + valueType + ", val=" + val + ", defaultVal="
                + defaultVal + ", description='" + description + '\'' + '}';
    }
}
