package cn.laoshini.dk.console.web;

import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.laoshini.dk.console.service.GameServerService;
import cn.laoshini.dk.util.LogUtil;

/**
 * @author fagarine
 */
@RestController
@RequestMapping("/dk/gm")
public class DkGmController {

    @Resource
    private GameServerService gameServerService;

    @PostMapping("/pause/{gameId}/{serverId}")
    public boolean pauseServer(@PathVariable("gameId") int gameId, @PathVariable("serverId") int serverId, String tips,
            long openTimeMills) {
        LogUtil.info("pause single game server, gameId: {}, serverId: {}, tips: {}, openTime: {}", gameId, serverId,
                tips, openTimeMills);
        return gameServerService.pauseServer(gameId, serverId, tips, new Date(openTimeMills));
    }

    @PostMapping("/pause/{gameId}/batch")
    public Map<Integer, Boolean> batchPauseServer(@PathVariable("gameId") int gameId, String tips, long openTimeMills) {
        LogUtil.info("batch pause game server, gameId: {}, tips: {}, openTime: {}", gameId, tips, openTimeMills);
        return gameServerService.batchPauseServer(gameId, tips, new Date(openTimeMills));
    }

    @PostMapping("/release/{gameId}/{serverId}")
    public boolean releaseServer(@PathVariable("gameId") int gameId, @PathVariable("serverId") int serverId) {
        LogUtil.info("release single game server, gameId: {}, serverId: {}", gameId, serverId);
        return gameServerService.releaseServer(gameId, serverId);
    }

    @PostMapping("/release/{gameId}/batch")
    public Map<Integer, Boolean> batchReleaseServer(@PathVariable("gameId") int gameId) {
        LogUtil.info("batch release game server, gameId: {}", gameId);
        return gameServerService.batchReleaseServer(gameId);
    }

}
