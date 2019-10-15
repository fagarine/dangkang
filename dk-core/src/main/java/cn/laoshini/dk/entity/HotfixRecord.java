package cn.laoshini.dk.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import cn.laoshini.dk.constant.InnerTableNameConst;
import cn.laoshini.dk.dao.TableKey;
import cn.laoshini.dk.dao.TableMapping;

/**
 * 热修复执行结果记录
 *
 * @author fagarine
 */
@Getter
@Setter
@ToString
@TableMapping(value = InnerTableNameConst.HOTFIX_RECORD, description = "热修复记录")
public class HotfixRecord implements Serializable {

    @TableKey
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
