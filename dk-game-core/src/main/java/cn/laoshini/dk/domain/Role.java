package cn.laoshini.dk.domain;

import cn.laoshini.dk.entity.GameRole;

/**
 * 游戏角色，纯内存对象
 *
 * @author fagarine
 */
public class Role extends GameSubject {

    private static final long serialVersionUID = 1L;

    private transient GameRole gameRole;
    /**
     * 玩家当前是否在线
     */
    private boolean online;

    public long getRoleId() {
        if (gameRole == null || gameRole.getRoleId() == null) {
            return 0;
        }
        return gameRole.getRoleId();
    }

    @Override
    public int getGameId() {
        return gameRole.getGameId();
    }

    @Override
    public int getServerId() {
        return gameRole.getServerId();
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public GameRole getGameRole() {
        return gameRole;
    }

    public void setGameRole(GameRole gameRole) {
        this.gameRole = gameRole;
    }
}
