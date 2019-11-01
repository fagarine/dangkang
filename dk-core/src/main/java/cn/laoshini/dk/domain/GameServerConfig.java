package cn.laoshini.dk.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import cn.laoshini.dk.constant.GameServerProtocolEnum;
import cn.laoshini.dk.constant.MessageFormatEnum;

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
     * 唯一id
     */
    private int id;

    /**
     * 游戏名称
     */
    private String name;

    /**
     * TCP游戏服务器占用端口，仅在使用TCP通信时有效
     */
    private int port;

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
