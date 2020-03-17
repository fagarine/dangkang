package cn.laoshini.dk.gm.message.server;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import cn.laoshini.dk.annotation.Message;
import cn.laoshini.dk.gm.constant.GmConstants;

/**
 * @author fagarine
 */
@Getter
@Setter
@ToString
@Message(id = PauseGameServerRes.MESSAGE_ID, gm = true)
public class PauseGameServerRes {

    public static final int MESSAGE_ID = GmConstants.GM_HEAD + GmConstants.PAUSE_GAME_SERVER_REQ + 1;
}
