package cn.laoshini.dk.expression;

import cn.laoshini.dk.domain.dto.ExpressionDescriptorDTO;

/**
 * @author fagarine
 */
public abstract class BaseExpressionProcessor implements IExpressionProcessor {

    /**
     * 表达式描述信息
     */
    protected ExpressionDescriptorDTO descriptor;

    public ExpressionDescriptorDTO getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(ExpressionDescriptorDTO descriptor) {
        this.descriptor = descriptor;
    }
}
