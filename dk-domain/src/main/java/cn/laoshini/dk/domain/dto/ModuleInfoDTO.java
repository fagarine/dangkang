package cn.laoshini.dk.domain.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 外置模块信息DTO
 *
 * @author fagarine
 */
@Data
public class ModuleInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模块名称
     */
    private String name;

    /**
     * 模块文件路径
     */
    private String file;

    /**
     * 文件最后修改时间
     */
    private Date lastModified;

    /**
     * 模块最后加载成功时间
     */
    private Date lastLoaded;
}
