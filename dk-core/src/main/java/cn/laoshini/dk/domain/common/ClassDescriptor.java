package cn.laoshini.dk.domain.common;

import java.io.Serializable;

import lombok.Data;

/**
 * @author fagarine
 */
@Data
public class ClassDescriptor implements Serializable {

    private static final long serialVersionUID = 1L;

    private String className;

    private String simpleName;

    private String type;

    private String comment;
}
