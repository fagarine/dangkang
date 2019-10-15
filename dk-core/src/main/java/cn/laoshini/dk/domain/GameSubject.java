package cn.laoshini.dk.domain;

import java.io.Serializable;

import cn.laoshini.dk.net.session.AbstractSession;

/**
 * 游戏主体，消息到达后的接收方，一般为玩家
 *
 * @author fagarine
 */
public class GameSubject implements Serializable {

    private static final long serialVersionUID = 1L;

    private AbstractSession session;

    public int getGameId() {
        return 0;
    }

    public int getServerId() {
        return 0;
    }

    public AbstractSession getSession() {
        return session;
    }

    public void setSession(AbstractSession session) {
        this.session = session;
    }
}
