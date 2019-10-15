package cn.laoshini.dk.jit.type;

import cn.laoshini.dk.constant.BeanTypeEnum;

/**
 * @author fagarine
 */
public final class TypeBeanRecord extends AbstractNumericTypeBean {

    private int beanTypeCode;

    @Override
    public BeanTypeEnum getType() {
        return BeanTypeEnum.codeOf(beanTypeCode);
    }

    public int getBeanTypeCode() {
        return beanTypeCode;
    }

    public void setBeanTypeCode(int beanTypeCode) {
        this.beanTypeCode = beanTypeCode;
    }

    @Override
    public String toString() {
        return "TypeBeanRecord{" + "beanTypeCode=" + beanTypeCode + "} " + super.toString();
    }
}
