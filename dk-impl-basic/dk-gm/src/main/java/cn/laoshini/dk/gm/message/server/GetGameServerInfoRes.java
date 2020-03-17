package cn.laoshini.dk.gm.message.server;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import cn.laoshini.dk.annotation.Message;
import cn.laoshini.dk.domain.dto.GameServerInfoDTO;
import cn.laoshini.dk.gm.constant.GmConstants;

/**
 * @author fagarine
 */
@Getter
@Setter
@ToString
@Message(id = GetGameServerInfoRes.MESSAGE_ID, gm = true)
public class GetGameServerInfoRes {

    public static final int MESSAGE_ID = GmConstants.GM_HEAD + GmConstants.GET_GAME_SERVER_INFO_REQ + 1;

    private List<GameServerInfoDTO> servers;
}
