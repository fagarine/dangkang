package cn.laoshini.dk.jit.type;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;

import cn.laoshini.dk.constant.BeanTypeEnum;
import cn.laoshini.dk.util.ReflectUtil;

/**
 * @author fagarine
 */
public class TypeBeanFactory {

    private static final Map<BeanTypeEnum, Class<? extends ITypeBean>> VALUE_TYPE_CLASS_MAP = new EnumMap<>(
            BeanTypeEnum.class);

    static {
        VALUE_TYPE_CLASS_MAP.put(BeanTypeEnum.BOOLEAN, BooleanBean.class);
        VALUE_TYPE_CLASS_MAP.put(BeanTypeEnum.BYTE, ByteBean.class);
        VALUE_TYPE_CLASS_MAP.put(BeanTypeEnum.SHORT, ShortBean.class);
        VALUE_TYPE_CLASS_MAP.put(BeanTypeEnum.INTEGER, IntegerBean.class);
        VALUE_TYPE_CLASS_MAP.put(BeanTypeEnum.LONG, LongBean.class);
        VALUE_TYPE_CLASS_MAP.put(BeanTypeEnum.DOUBLE, DoubleBean.class);
        VALUE_TYPE_CLASS_MAP.put(BeanTypeEnum.STRING, StringBean.class);
        VALUE_TYPE_CLASS_MAP.put(BeanTypeEnum.ORDINARY, OrdinaryBean.class);
        VALUE_TYPE_CLASS_MAP.put(BeanTypeEnum.LIST, ListBean.class);
        VALUE_TYPE_CLASS_MAP.put(BeanTypeEnum.COMPOSITE, CompositeBean.class);
    }

    private static ITypeBean newTypeBean(BeanTypeEnum beanTypeEnum) {
        return ReflectUtil.newInstance(VALUE_TYPE_CLASS_MAP.get(beanTypeEnum));
    }

    public static CompositeBean buildTypeBean(String name, String description, List<TypeBeanRecord> records) {
        CompositeBean compositeBean = new CompositeBean();
        compositeBean.setName(name);
        compositeBean.setDescription(description);
        if (records != null && !records.isEmpty()) {
            ITypeBean typeBean;
            compositeBean.setVal(new ArrayList<>(records.size()));
            for (TypeBeanRecord record : records) {
                typeBean = newTypeBean(record.getType());
                BeanUtils.copyProperties(record, typeBean);

                compositeBean.getVal().add(typeBean);
            }
        }
        return compositeBean;
    }
}
