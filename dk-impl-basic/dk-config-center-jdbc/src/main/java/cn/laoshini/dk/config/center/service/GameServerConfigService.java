package cn.laoshini.dk.config.center.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.laoshini.dk.config.center.domain.PropertySource;
import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.util.CollectionUtil;

/**
 * @author fagarine
 */
@Service
public class GameServerConfigService {

    @Resource
    private PropertiesService propertiesService;

    /**
     * 获取指定游戏服进程配置信息
     *
     * @param gameId 游戏id
     * @param serverId 游戏服id
     * @param env 服务器运行环境
     * @return 返回配置信息
     */
    public Map<String, String> getSingleGameConfig(int gameId, int serverId, String env) {
        PropertySource properties = propertiesService
                .getPropertySource(String.valueOf(gameId), String.valueOf(serverId), env);
        return properties.hasProperties() ? properties.getPropertiesMap() : Collections.emptyMap();
    }

    /**
     * 获取指定游戏指定运行环境下的所有进程配置信息
     *
     * @param gameId 游戏id
     * @param env 服务器运行环境
     * @return 返回配置信息
     */
    public Map<String, Map<String, String>> getGameConfigByGameIdAndEnv(int gameId, String env) {
        return propertiesService.getMapByApplicationAndLabel(String.valueOf(gameId), env);
    }

    /**
     * 批量获取多个游戏服同一配置项的值
     *
     * @param gameId 游戏id
     * @param env 服务器运行环境
     * @param paramKey 配置项key
     * @return 返回配置信息
     */
    public Map<String, String> getGameServerParam(int gameId, String env, String paramKey) {
        Map<String, Map<String, String>> params = getGameConfigByGameIdAndEnv(gameId, env);
        Map<String, String> map = new HashMap<>(params.size());
        for (Map.Entry<String, Map<String, String>> entry : params.entrySet()) {
            map.put(entry.getKey(), entry.getValue().get(paramKey));
        }
        return map;
    }

    public void putSingleGameConfig(int gameId, int serverId, String env, Map<String, Object> params, boolean part) {
        propertiesService
                .replaceSinglePropertySource(String.valueOf(gameId), String.valueOf(serverId), env, params, part);
    }

    public void putSingleGameConfig(int gameId, int serverId, Map<String, Object> params, boolean part) {
        propertiesService.replaceSinglePropertySource(String.valueOf(gameId), String.valueOf(serverId),
                Constants.DEFAULT_GAME_SERVER_ENV, params, part);
    }

    public void batchUpdateAssignedKeysByEnvs(int gameId, int serverId, List<String> envs,
            Map<String, Object> properties) {
        propertiesService.batchUpdateAssignedPropertiesByLabels(String.valueOf(gameId), String.valueOf(serverId), envs,
                properties);
    }

    public void batchUpdateAssignedKeysByServerIds(int gameId, String env, List<Integer> serverIds,
            Map<String, Object> properties) {
        List<String> profiles = null;
        if (CollectionUtil.isNotEmpty(serverIds)) {
            profiles = new ArrayList<>(serverIds.size());
            for (Integer serverId : serverIds) {
                profiles.add(String.valueOf(serverId));
            }
        }
        propertiesService.batchUpdateAssignedPropertiesByProfiles(String.valueOf(gameId), env, profiles, properties);
    }

    public void batchUpdateServersByEnv(int gameId, String env, Map<String, Map<String, Object>> properties) {
        propertiesService.batchPutAssignedLabelProperties(String.valueOf(gameId), env, properties);
    }
}
