package cn.laoshini.dk.gm.message.module;

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
@Message(id = GetModuleListReq.MESSAGE_ID, gm = true)
public class GetModuleListReq {

    public static final int MESSAGE_ID = GmConstants.GM_HEAD + GmConstants.GET_MODULE_LIST_REQ;
}
