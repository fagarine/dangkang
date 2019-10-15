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
 * 游戏角色数据，表对象
 *
 * @author fagarine
 */
@Getter
@Setter
@ToString
@TableMapping(value = InnerTableNameConst.GAME_ROLE, description = "游戏角色记录")
public class GameRole implements Serializable {

    /**
     * 角色id
     */
    @TableKey
    private Long roleId;
    /**
     * 所属用户id
     */
    @TableKey
    private Long userId;
    /**
     * 角色名称
     */
    private String roleName;
    /**
     * 头像
     */
    private int portrait;
    /**
     * 当前等级
     */
    private int roleLv;
    /**
     * 当前经验值
     */
    private long exp;
    /**
     * vip等级
     */
    private int vip;
    /**
     * 所属游戏id
     */
    private Integer gameId;
    /**
     * 所属服务器id
     */
    private Integer serverId;
    /**
     * 最后登录时间
     */
    private Date loginTime;
    /**
     * 最后登出时间
     */
    private Date logoutTime;
    /**
     * 角色状态
     */
    private Integer status;
    /**
     * 如果角色当前为非正常状态，记录解除当前状态的时间
     */
    private Date liftTime;
}
