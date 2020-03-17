package cn.laoshini.dk.config.center.web;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.cloud.bus.endpoint.RefreshBusEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.laoshini.dk.config.center.service.GameServerConfigService;
import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.domain.responese.Result;
import cn.laoshini.dk.util.LogUtil;

/**
 * 游戏服配置信息相关
 *
 * @author fagarine
 */
@RestController
@RequestMapping("/game/server")
public class GameServerConfigController {

    @Resource
    private GameServerConfigService gameServerConfigService;

    @Resource
    private RefreshBusEndpoint refreshBusEndpoint;

    /**
     * 获取指定游戏id下所有游戏服进程配置信息
     *
     * @param gameId 游戏id
     * @return 返回配置信息
     */
    @GetMapping("/{gameId}/servers")
    public Result<Map<String, Map<String, String>>> getGameConfigByGameId(@PathVariable("gameId") int gameId) {
        LogUtil.info("game server list, gameId: {}", gameId);
        Map<String, Map<String, String>> servers;
        try {
            servers = gameServerConfigService.getGameConfigByGameIdAndEnv(gameId, Constants.DEFAULT_GAME_SERVER_ENV);
        } catch (Exception e) {
            LogUtil.error("批量获取配置信息出错", e);
            return Result.fail("获取配置信息出错", e);
        }
        return Result.success(servers);
    }

    /**
     * 获取指定游戏id下所有游戏服进程配置信息
     *
     * @param gameId 游戏id
     * @param env 服务器运行环境
     * @return 返回配置信息
     */
    @GetMapping("/{gameId}/{env}/servers")
    public Result<Map<String, Map<String, String>>> getGameConfigByGameIdAndEnv(@PathVariable("gameId") int gameId,
            @PathVariable("env") String env) {
        LogUtil.info("game server list, gameId: {}, env: {}", gameId, env);
        Map<String, Map<String, String>> servers;
        try {
            servers = gameServerConfigService.getGameConfigByGameIdAndEnv(gameId, env);
        } catch (Exception e) {
            LogUtil.error("批量获取配置信息出错", e);
            return Result.fail("获取配置信息出错", e);
        }
        return Result.success(servers);
    }

    /**
     * 获取指定游戏下，所有服务器的单项配置信息
     *
     * @param gameId 游戏id
     * @param key 配置项key
     * @return 返回配置信息
     */
    @GetMapping("/{gameId}/param/{key}")
    public Result<Map<String, String>> getGameServerParam(@PathVariable("gameId") int gameId,
            @PathVariable("key") String key) {
        LogUtil.info("get game server param, gameId: {}, key: {}", gameId, key);
        Map<String, String> config;
        try {
            config = gameServerConfigService.getGameServerParam(gameId, Constants.DEFAULT_GAME_SERVER_ENV, key);
        } catch (Exception e) {
            LogUtil.error("获取游戏单项配置信息出错", e);
            return Result.fail("获取游戏单项配置信息出错", e);
        }
        return Result.success(config);
    }

    /**
     * 获取指定游戏，指定运行环境下，所有服务器的单项配置信息
     *
     * @param gameId 游戏id
     * @param env 服务器运行环境
     * @param key 配置项key
     * @return 返回配置信息
     */
    @GetMapping("/{gameId}/{env}/param/{key}")
    public Result<Map<String, String>> getGameServerParamByEnv(@PathVariable("gameId") int gameId,
            @PathVariable("env") String env, @PathVariable("key") String key) {
        LogUtil.info("get game server param, gameId: {}, env: {}, key: {}", gameId, env, key);
        Map<String, String> config;
        try {
            config = gameServerConfigService.getGameServerParam(gameId, env, key);
        } catch (Exception e) {
            LogUtil.error("获取游戏单项配置信息出错", e);
            return Result.fail("获取游戏单项配置信息出错", e);
        }
        return Result.success(config);
    }

    /**
     * 获取指定游戏服进程配置信息
     *
     * @param gameId 游戏id
     * @param serverId 游戏服id
     * @return 返回配置信息
     */
    @GetMapping("/{gameId}/{serverId}")
    public Result<Map<String, String>> getSingleGameServerConfig(@PathVariable("gameId") int gameId,
            @PathVariable("serverId") int serverId) {
        LogUtil.info("get single game server, gameId: {}, serverId: {}", gameId, serverId);
        Map<String, String> config;
        try {
            config = gameServerConfigService.getSingleGameConfig(gameId, serverId, Constants.DEFAULT_GAME_SERVER_ENV);
        } catch (Exception e) {
            LogUtil.error("获取游戏服配置信息出错", e);
            return Result.fail("获取游戏服配置信息出错", e);
        }
        return Result.success(config);
    }

    /**
     * 获取指定游戏服进程和进程运行环境配置信息（适用于同一serverId在不同环境表示不同进程的项目，不推荐）
     *
     * @param gameId 游戏id
     * @param serverId 游戏服id
     * @param env 服务器运行环境
     * @return 返回配置信息
     */
    @GetMapping("/{gameId}/{serverId}/{env}")
    public Result<Map<String, String>> getSingleGameServerConfigByEnv(@PathVariable("gameId") int gameId,
            @PathVariable("serverId") int serverId, @PathVariable("env") String env) {
        LogUtil.info("get single game server, gameId: {}, serverId: {}, env:{}", gameId, serverId, env);
        Map<String, String> config;
        try {
            config = gameServerConfigService.getSingleGameConfig(gameId, serverId, env);
        } catch (Exception e) {
            LogUtil.error("获取游戏服配置信息出错", e);
            return Result.fail("获取游戏服配置信息出错", e);
        }
        return Result.success(config);
    }

    /**
     * 写入（全量覆盖，数据库中，不包含在传入配置项中的将被删除）单个游戏服的配置信息
     *
     * @param gameId 游戏id
     * @param serverId 游戏服id
     * @param env 运行环境
     * @param params 配置项信息
     * @return 返回执行结果
     */
    @PutMapping("/single/{gameId}/{serverId}/{env}")
    public Result putSingleGameServerConfig(@PathVariable("gameId") int gameId, @PathVariable("serverId") int serverId,
            @PathVariable("env") String env, @RequestBody Map<String, Object> params) {
        LogUtil.info("put single game server, gameId: {}, serverId: {}, env:{}", gameId, serverId, env);
        try {
            gameServerConfigService.putSingleGameConfig(gameId, serverId, env, params, false);
        } catch (Exception e) {
            LogUtil.error("写入单个游戏服配置信息出错", e);
            return Result.fail("执行出错", e);
        }
        return Result.success();
    }

    /**
     * 写入（全量覆盖，数据库中，不包含在传入配置项中的将被删除）单个游戏服的配置信息
     *
     * @param gameId 游戏id
     * @param serverId 游戏服id
     * @param params 配置项信息
     * @return 返回执行结果
     */
    @PutMapping("/single/{gameId}/{serverId}")
    public Result putSingleGameServerConfig(@PathVariable("gameId") int gameId, @PathVariable("serverId") int serverId,
            @RequestBody Map<String, Object> params) {
        LogUtil.info("put single game server default env, gameId: {}, serverId: {}", gameId, serverId);
        try {
            gameServerConfigService.putSingleGameConfig(gameId, serverId, params, false);
        } catch (Exception e) {
            LogUtil.error("写入单个游戏服配置信息出错", e);
            return Result.fail("执行出错", e);
        }
        return Result.success();
    }

    /**
     * 写入单个游戏服的配置信息（只处理传入的配置项信息）
     *
     * @param gameId 游戏id
     * @param serverId 游戏服id
     * @param env 运行环境
     * @param params 配置项信息
     * @return 返回执行结果
     */
    @PutMapping("/single/part/{gameId}/{serverId}/{env}")
    public Result putSingleGameServerPartConfig(@PathVariable("gameId") int gameId,
            @PathVariable("serverId") int serverId, @PathVariable("env") String env,
            @RequestBody Map<String, Object> params) {
        LogUtil.info("put single game server part, gameId: {}, serverId: {}, env:{}", gameId, serverId, env);
        try {
            gameServerConfigService.putSingleGameConfig(gameId, serverId, env, params, true);
        } catch (Exception e) {
            LogUtil.error("写入单个游戏服配置信息出错", e);
            return Result.fail("执行出错", e);
        }
        return Result.success();
    }

    /**
     * 批量更新指定游戏指定运行环境下，传入的游戏服的全量配置信息
     *
     * @param gameId 游戏id
     * @param env 运行环境
     * @param configs 需要更新的游戏服配置信息，key1: 游戏服id，key2: 配置项key
     * @return 返回执行结果
     */
    @PutMapping("/batch/{gameId}/{env}")
    public Result batchUpdateConfigsByEnv(@PathVariable("gameId") int gameId, @PathVariable("env") String env,
            @RequestBody Map<String, Map<String, Object>> configs) {
        LogUtil.info("batch game server configs, gameId: {}, env: {}, configs:{}", gameId, env, configs);
        try {
            gameServerConfigService.batchUpdateServersByEnv(gameId, env, configs);
        } catch (Exception e) {
            LogUtil.error("批量写入指定运行环境下多个游戏服全量配置信息出错", e);
            return Result.fail("执行出错", e);
        }
        return Result.success();
    }

    /**
     * 批量更新指定游戏指定运行环境下，所有游戏服的部分配置信息（只处理传入的配置项信息）
     *
     * @param gameId 游戏id
     * @param env 运行环境
     * @param params 要更新的配置信息
     * @return 返回执行结果
     */
    @PutMapping("/batch/part/{gameId}/{env}")
    public Result batchUpdatePropertiesByServers(@PathVariable("gameId") int gameId, @PathVariable("env") String env,
            @RequestBody Map<String, Object> params) {
        LogUtil.info("batch game server, gameId: {}, env: {}, params:{}", gameId, env, params);
        try {
            gameServerConfigService.batchUpdateAssignedKeysByServerIds(gameId, env, null, params);
        } catch (Exception e) {
            LogUtil.error("批量写入指定运行环境下游戏服部分配置信息出错", e);
            return Result.fail("执行出错", e);
        }
        return Result.success();
    }

    /**
     * 批量更新指定游戏指定游戏服id，所有环境下的指定配置信息
     *
     * @param gameId 游戏id
     * @param serverId 游戏服id
     * @param params 要更新的配置信息
     * @return 返回执行结果
     */
    @PutMapping("batch/envs/{gameId}/{serverId}")
    public Result batchUpdatePropertiesByEnvs(@PathVariable("gameId") int gameId,
            @PathVariable("serverId") int serverId, @RequestBody Map<String, Object> params) {
        LogUtil.info("batch game server, gameId: {}, serverId: {}, params:{}", gameId, serverId, params);
        try {
            gameServerConfigService.batchUpdateAssignedKeysByEnvs(gameId, serverId, null, params);
        } catch (Exception e) {
            LogUtil.error("批量写入指定id游戏服部分配置信息出错", e);
            return Result.fail("执行出错", e);
        }
        return Result.success();
    }

    /**
     * 手动刷新配置信息（推送到各游戏服进程）
     *
     * @return 返回执行结果
     */
    @PostMapping("/refresh")
    public Result refreshGameServerConfigs() {
        // 调用Spring Cloud Bus的refresh方法，向各监听进程推送
        try {
            refreshBusEndpoint.busRefresh();
        } catch (Exception e) {
            LogUtil.error("刷新配置信息出错", e);
            return Result.fail("刷新配置信息出错", e);
        }
        return Result.success();
    }

}
