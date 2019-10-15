package cn.laoshini.dk.domain.common;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * @author fagarine
 */
@Data
public class MethodDescriptor implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private String comment;

    private List<Tuple<String, String>> params;

}
