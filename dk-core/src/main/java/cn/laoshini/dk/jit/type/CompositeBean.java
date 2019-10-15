package cn.laoshini.dk.jit.type;

import java.util.Collections;
import java.util.List;

import cn.laoshini.dk.constant.BeanTypeEnum;

/**
 * @author fagarine
 */
public class CompositeBean extends AbstractTypeBean<List<ITypeBean>> {

    public CompositeBean() {
        super();
        setDefaultVal(Collections.emptyList());
    }

    @Override
    public BeanTypeEnum getType() {
        return BeanTypeEnum.COMPOSITE;
    }

    @Override
    public String toString() {
        return "CompositeBean{" + "name='" + name + '\'' + ", valueType=" + valueType + ", val=" + val + ", defaultVal="
                + defaultVal + ", description='" + description + '\'' + '}';
    }
}
