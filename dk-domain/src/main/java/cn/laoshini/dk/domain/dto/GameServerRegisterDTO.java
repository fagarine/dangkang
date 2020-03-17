package cn.laoshini.dk.domain.dto;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;

/**
 * @author fagarine
 */
@Data
public class GameServerRegisterDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 游戏id
     */
    private int gameId;

    /**
     * 游戏名称
     */
    private String gameName;

    /**
     * 本次注册的游戏服务器，key: serverId, value: serverName
     */
    private Map<Integer, String> servers;
}
