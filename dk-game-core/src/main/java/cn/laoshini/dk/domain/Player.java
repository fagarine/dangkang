package cn.laoshini.dk.domain;

import cn.laoshini.dk.constant.UserStatusEnum;
import cn.laoshini.dk.entity.GameUser;

/**
 * 玩家（帐号）对象，纯内存对象
 *
 * @author fagarine
 */
public class Player extends GameSubject {

    private static final long serialVersionUID = 1L;

    private transient GameUser user;

    public boolean isValid() {
        if (user == null || user.getStatus() == null) {
            return false;
        }
        return UserStatusEnum.isValidStatus(user.getStatus());
    }

    public long getUserId() {
        if (user == null || user.getUserId() == null) {
            return 0;
        }
        return user.getUserId();
    }

    public GameUser getUser() {
        return user;
    }

    public void setUser(GameUser user) {
        this.user = user;
    }

    @Override
    public int getGameId() {
        return user.getLoginGameId();
    }

    @Override
    public int getServerId() {
        return 0;
    }
}
