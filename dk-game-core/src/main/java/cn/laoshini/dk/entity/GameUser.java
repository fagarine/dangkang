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
 * 游戏用户数据，表对象
 *
 * @author fagarine
 */
@Getter
@Setter
@ToString
@TableMapping(value = InnerTableNameConst.GAME_USER, description = "游戏用户记录")
public class GameUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableKey
    private Long userId;
    /**
     * 渠道号
     */
    private int platNo;
    /**
     * 子渠道号（某些聚合平台有子渠道号）
     */
    private int childPlatNo;
    /**
     * 用户在对应的渠道平台的唯一id，用于第三方渠道登录
     */
    private String platId;
    /**
     * 登录名（自有渠道）
     */
    private String loginName;
    /**
     * 登录密码（自有渠道）
     */
    private String password;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 手机号码
     */
    private String phoneNumber;
    /**
     * 昵称，显示名称
     */
    private String nickname;
    /**
     * 头像地址
     */
    private String icon;
    /**
     * 最后登录游戏id
     */
    private Integer loginGameId;
    /**
     * 最后登录时间
     */
    private Date loginTime;
    /**
     * 帐号状态
     */
    private Short status;
    /**
     * 如果帐号被锁，解除锁定时间
     */
    private Date unlockTime;
    /**
     * 注册ip
     */
    private String registerIp;
    /**
     * 设备号
     */
    private String deviceNo;
    /**
     * 注册时间
     */
    private Date createTime;
    /**
     * 数据更新时间
     */
    private Date lastUpdateTime;
}
