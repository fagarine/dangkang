package cn.laoshini.dk.domain.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 表达式代码块
 *
 * @author fagarine
 */
@Data
public class ExpressionBlockDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 块注释
     */
    private String comment;

    /**
     * 是否有返回值
     */
    private Boolean returned;

    /**
     * 表达式详情
     */
    private List<ExpressionDescriptorDTO> expDescriptors;

    public int size() {
        return expDescriptors == null ? 0 : expDescriptors.size();
    }
}
