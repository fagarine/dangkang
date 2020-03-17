package cn.laoshini.dk.console.web;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.laoshini.dk.domain.dto.GameServerRegisterDTO;
import cn.laoshini.dk.domain.msg.RespMessage;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.LogUtil;

/**
 * @author fagarine
 */
@RestController
@RequestMapping("/dk/server")
public class DkGameServerController {

    @PostMapping("/register")
    public RespMessage gameServerRegister(@RequestBody GameServerRegisterDTO dto) {
        Map<Integer, String> servers = dto.getServers();
        if (CollectionUtil.isNotEmpty(servers)) {
            // 只打印游戏服注册记录，不执行其他操作
            for (Map.Entry<Integer, String> entry : servers.entrySet()) {
                LogUtil.info("游戏[{}]的[{}区]服务器[{}]注册到后台!", dto.getGameName(), entry.getKey(), entry.getValue());
            }
        }
        return new RespMessage();
    }

}
