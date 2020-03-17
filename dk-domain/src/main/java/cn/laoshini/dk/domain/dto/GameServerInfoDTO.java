package cn.laoshini.dk.domain.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * @author fagarine
 */
@Data
public class GameServerInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 游戏id
     */
    private Integer gameId;

    /**
     * 游戏名称
     */
    private String gameName;

    /**
     * 唯一id
     */
    private Integer serverId;

    /**
     * 服务器名称
     */
    private String serverName;

    /**
     * 游戏服务器占用端口
     */
    private Integer port;

    /**
     * 游戏服务器使用什么协议通信（HTTP,TCP,UDP等），类型参见：GameServerProtocolEnum
     */
    private String protocol;

    /**
     * 服务器状态，类型参见：GameServerStatus
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

    /**
     * 服务器启动时间
     */
    private Date startTime;

}
