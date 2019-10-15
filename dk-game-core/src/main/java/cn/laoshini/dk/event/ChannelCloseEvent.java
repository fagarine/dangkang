package cn.laoshini.dk.event;

import cn.laoshini.dk.domain.Player;

/**
 * 网络会话关闭事件
 *
 * @author fagarine
 */
public class ChannelCloseEvent {

    private final Player player;

    /**
     * @param player 玩家对象
     */
    public ChannelCloseEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
