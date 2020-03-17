package cn.laoshini.dk.domain.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 热修复执行结果记录DTO
 *
 * @author fagarine
 */
@Data
public class HotfixRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;

    /**
     * 热更key，用于区分是否是同一次热更操作，可以是热更说明信息等
     */
    private String hotfixKey;

    /**
     * 热更类名
     */
    private String className;

    /**
     * 热更时间
     */
    private Date hotfixTime;

    /**
     * 热更结果
     */
    private String result;

    /**
     * 描述信息
     */
    private String desc;
}
