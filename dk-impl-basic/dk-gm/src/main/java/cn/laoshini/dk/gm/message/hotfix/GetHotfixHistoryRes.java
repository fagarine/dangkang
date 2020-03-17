package cn.laoshini.dk.gm.message.hotfix;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import cn.laoshini.dk.annotation.Message;
import cn.laoshini.dk.domain.dto.HotfixRecordDTO;
import cn.laoshini.dk.gm.constant.GmConstants;

/**
 * @author fagarine
 */
@Getter
@Setter
@ToString
@Message(id = GetHotfixHistoryRes.MESSAGE_ID, gm = true)
public class GetHotfixHistoryRes {

    public static final int MESSAGE_ID = GmConstants.GM_HEAD + GmConstants.GET_HOTFIX_HISTORY_REQ + 1;

    /**
     * 页码，从1开始
     */
    private Integer pageNo;

    /**
     * 单页数据条数
     */
    private Integer pageSize;

    /**
     * 总数据条数
     */
    private Long total;

    /**
     * hotfix执行记录
     */
    private List<HotfixRecordDTO> records;
}
