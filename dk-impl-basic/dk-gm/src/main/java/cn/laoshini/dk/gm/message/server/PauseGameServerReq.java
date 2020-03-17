package cn.laoshini.dk.gm.message.server;

import java.util.Date;

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
@Message(id = PauseGameServerReq.MESSAGE_ID, gm = true)
public class PauseGameServerReq {

    public static final int MESSAGE_ID = GmConstants.GM_HEAD + GmConstants.PAUSE_GAME_SERVER_REQ;

    private String tips;

    private Date openTime;
}
