package cn.laoshini.dk.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import cn.laoshini.dk.constant.GameServerProtocolEnum;
import cn.laoshini.dk.constant.MessageFormatEnum;
import cn.laoshini.dk.constant.ServerType;

/**
 * @author fagarine
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameServerConfig {

    /**
     * 游戏id
     */
    private int gameId;

    /**
     * 游戏名称
     */
    private String gameName;

    /**
     * 游戏服唯一id
     */
    private int serverId;

    /**
     * 游戏服名称
     */
    private String serverName;

    /**
     * 游戏服务器占用端口
     */
    private int port;

    /**
     * 服务器类型
     */
    private ServerType serverType;

    /**
     * 游戏服务器使用什么协议通信（HTTP,TCP,UDP等），类型参见：GameServerProtocolEnum
     */
    private GameServerProtocolEnum protocol;

    /**
     * 游戏通信消息格式，当康系统提供的可选方案有JSON格式、当康系统自定义消息格式、Protobuf消息格式
     */
    private MessageFormatEnum format;

    /**
     * 连接最大空闲时间，单位：秒，超过该时间没有消息到达，将断开连接
     */
    private int idleTime;

    /**
     * 使用TCP连接时，消息是否立即发送
     */
    private boolean tcpNoDelay;
}
