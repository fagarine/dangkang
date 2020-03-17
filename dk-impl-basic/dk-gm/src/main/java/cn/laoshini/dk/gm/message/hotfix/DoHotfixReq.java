package cn.laoshini.dk.gm.message.hotfix;

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
@Message(id = DoHotfixReq.MESSAGE_ID, gm = true)
public class DoHotfixReq {

    public static final int MESSAGE_ID = GmConstants.GM_HEAD + GmConstants.DO_HOTFIX_REQ;

    private String hotfixKey;
}
