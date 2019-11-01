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
 * 混合类游戏服（多个游戏的所有服务器在一个进程中）配置信息
 *
 * @author fagarine
 */
@Getter
@Setter
@ToString
@TableMapping(value = InnerTableNameConst.MIXED_GAME_SERVER_INFO, description = "混合类游戏服信息配置表")
public class MixedGameServerInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 游戏服所属游戏id，放在serverId前面是为了使用键值对数据库时，生成key时gameId在前面
     */
    @TableKey
    private Integer gameId;
    /**
     * 服务器id，所有游戏服唯一
     */
    @TableKey
    private Integer serverId;
    /**
     * 服务器名称
     */
    private String serverName;
    /**
     * 服务器类型
     */
    private Integer serverType;
    /**
     * 服务器状态
     */
    private Integer status;
    /**
     * 如果服务器当前不对外开放，提示信息
     */
    private String tips;
    /**
     * 如果服务器当前不对外开放，服务器预计对外开放时间
     */
    private Date openTime;
}
