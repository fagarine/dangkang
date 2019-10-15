package cn.laoshini.dk.constant;

import io.netty.util.AttributeKey;

import cn.laoshini.dk.domain.Player;

/**
 * 项目中关于netty AttributeKey
 *
 * @author fagarine
 */
public class AttributeKeyConstant {

    /**
     * 玩家帐号对象key
     */
    public static final AttributeKey<Player> PLAYER = AttributeKey.valueOf("GAME_PLAYER");

    /**
     * 玩家角色对象key
     */
    public static final AttributeKey<Player> ROLE = AttributeKey.valueOf("GAME_ROLE");

    /**
     * 进入消息序列化
     */
    public static final AttributeKey<Integer> REQ_MSG_ORDER = AttributeKey.valueOf("REQ_MSG_ORDER");
}
