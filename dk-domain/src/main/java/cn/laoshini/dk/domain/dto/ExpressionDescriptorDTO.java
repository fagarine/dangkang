package cn.laoshini.dk.domain.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * 表达式信息
 *
 * @author fagarine
 */
@Data
public class ExpressionDescriptorDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 表达式类型，参见：{@link cn.laoshini.dk.constant.ExpressionConstant.ExpressionCodeType}
     */
    private String type;

    /**
     * 表达式代码
     */
    private String expression;

    /**
     * 如果需要暂时保存执行结果，记录的参数名称
     */
    private String recordParam;

}
