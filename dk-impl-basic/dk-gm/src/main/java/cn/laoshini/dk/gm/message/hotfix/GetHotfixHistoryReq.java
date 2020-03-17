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
@Message(id = GetHotfixHistoryReq.MESSAGE_ID, gm = true)
public class GetHotfixHistoryReq {

    public static final int MESSAGE_ID = GmConstants.GM_HEAD + GmConstants.GET_HOTFIX_HISTORY_REQ;

    /**
     * 页码，从1开始
     */
    private Integer pageNo;

    /**
     * 单页数据条数
     */
    private Integer pageSize;
}
