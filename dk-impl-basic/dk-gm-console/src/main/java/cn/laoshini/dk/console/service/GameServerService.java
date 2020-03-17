package cn.laoshini.dk.console.service;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import cn.laoshini.dk.console.util.HttpUtil;
import cn.laoshini.dk.constant.GameCodeEnum;
import cn.laoshini.dk.domain.dto.ModuleInfoDTO;
import cn.laoshini.dk.domain.msg.ReqMessage;
import cn.laoshini.dk.domain.msg.RespMessage;
import cn.laoshini.dk.domain.responese.Result;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.gm.message.hotfix.DoHotfixReq;
import cn.laoshini.dk.gm.message.hotfix.DoHotfixRes;
import cn.laoshini.dk.gm.message.hotfix.GetHotfixHistoryReq;
import cn.laoshini.dk.gm.message.hotfix.GetHotfixHistoryRes;
import cn.laoshini.dk.gm.message.module.GetModuleListReq;
import cn.laoshini.dk.gm.message.module.GetModuleListRes;
import cn.laoshini.dk.gm.message.module.ReloadModulesReq;
import cn.laoshini.dk.gm.message.module.ReloadModulesRes;
import cn.laoshini.dk.gm.message.module.RemoveModuleReq;
import cn.laoshini.dk.gm.message.module.RemoveModuleRes;
import cn.laoshini.dk.gm.message.server.PauseGameServerReq;
import cn.laoshini.dk.gm.message.server.PauseGameServerRes;
import cn.laoshini.dk.gm.message.server.ReleaseGameServerReq;
import cn.laoshini.dk.gm.message.server.ReleaseGameServerRes;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.StringUtil;

/**
 * @author fagarine
 */
@Service
public class GameServerService {

    @Value("${dk.config.server:#{dangKangConfigCenterProperties.server}}")
    private String configServerUrl;

    @Value("${dk.gm.key:#{dangKangGmProperties.key}}")
    private String gmUrlKey;

    private String gameServerConfigUrlPrefix;

    /**
     * 根据游戏服id，获取对应的GM服务器接收GM请求的URL
     *
     * @param gameId 游戏id
     * @param serverId 游戏服id
     * @return 如果未找到，返回null
     */
    public String getGmUrlByGameServerId(int gameId, int serverId) {
        return getGmUrlByGameServerIdAndEnv(gameId, serverId, null);
    }

    /**
     * 根据游戏服务器id和游戏服运行环境，获取对应的GM服务器接收GM请求的URL
     *
     * @param gameId 游戏id
     * @param serverId 游戏服id
     * @param env 游戏服环境标识
     * @return 如果未找到，返回null
     */
    public String getGmUrlByGameServerIdAndEnv(int gameId, int serverId, String env) {
        String url = getSingleGameServerConfigUrl(gameId, serverId, env);
        Result<Map<String, String>> result;
        try {
            result = HttpUtil.requestResult(url, HttpMethod.GET, null);
        } catch (Exception e) {
            LogUtil.error(String.format("获取游戏服配置信息出错, gameId:%d, serverId:%d, env:%s", gameId, serverId, env), e);
            throw new BusinessException("http.request.error", "获取游戏服配置信息出错", e);
        }

        Map<String, String> params = result.extractReturn();
        if (CollectionUtil.isEmpty(params)) {
            throw new BusinessException("game.server.config", "未找到对应服务器的配置信息");
        }
        String gmUrl = params.get(gmUrlKey);
        if (StringUtil.isEmptyString(gmUrl)) {
            throw new BusinessException("gm.url.missing", "未找到对应服务器的GM服务URL");
        }
        return gmUrl;
    }

    /**
     * 根据游戏id，获取所有游戏服的GM服务器接收GM请求的URL
     *
     * @param gameId 游戏id
     * @return 如果未找到，返回null
     */
    public Map<String, String> batchGetGmUrlByGameId(int gameId) {
        return batchGetGmUrlByGameIdAndEnv(gameId, null);
    }

    /**
     * 根据游戏id和游戏服运行环境，获取所有游戏服的GM服务器接收GM请求的URL
     *
     * @param gameId 游戏id
     * @param env 游戏服环境标识
     * @return 如果未找到，返回null
     */
    public Map<String, String> batchGetGmUrlByGameIdAndEnv(int gameId, String env) {
        String url = getBatchGmUrl(gameId, env);
        Result<Map<String, String>> result;
        try {
            result = HttpUtil.requestResult(url, HttpMethod.GET, null);
        } catch (Exception e) {
            LogUtil.error(String.format("获取游戏服配置信息出错, gameId:%d, env:%s", gameId, env), e);
            throw new BusinessException("http.request.error", "获取游戏服配置信息出错", e);
        }
        Map<String, String> params = result.extractReturn();
        return params == null ? Collections.emptyMap() : params;
    }

    private ReqMessage<PauseGameServerReq> createPauseServerReq(String tips, Date openTime) {
        ReqMessage<PauseGameServerReq> req = new ReqMessage<>();
        req.setData(new PauseGameServerReq());
        req.getData().setTips(tips);
        req.getData().setOpenTime(openTime);
        return req;
    }

    private boolean sendAndGetPauseServerResult(String gmUrl, ReqMessage<PauseGameServerReq> req) {
        RespMessage<PauseGameServerRes> res;
        try {
            res = HttpUtil.gmRequest(gmUrl, req);
        } catch (Exception e) {
            LogUtil.error("停止游戏服对外服务出错, gmUrl:" + gmUrl, e);
            throw new BusinessException("http.request.error", "停止游戏服对外服务出错", e);
        }

        if (GameCodeEnum.OK.getCode() != res.getCode()) {
            LogUtil.error("停止游戏服对外服务失败：" + res.getParams());
        }
        return GameCodeEnum.OK.getCode() == res.getCode();
    }

    public boolean pauseServer(int gameId, int serverId, String tips, Date openTimeMills) {
        String gmUrl = getGmUrlByGameServerId(gameId, serverId);
        ReqMessage<PauseGameServerReq> req = createPauseServerReq(tips, openTimeMills);
        return sendAndGetPauseServerResult(gmUrl, req);
    }

    public Map<Integer, Boolean> batchPauseServer(int gameId, String tips, Date openTimeMills) {
        Map<String, String> gmUrlMap = batchGetGmUrlByGameId(gameId);
        if (CollectionUtil.isNotEmpty(gmUrlMap)) {
            ReqMessage<PauseGameServerReq> req = createPauseServerReq(tips, openTimeMills);

            Map<Integer, Boolean> result = new HashMap<>(gmUrlMap.size());
            for (Map.Entry<String, String> entry : gmUrlMap.entrySet()) {
                int serverId = Integer.parseInt(entry.getKey());
                String gmUrl = entry.getValue();
                boolean succeed = false;
                if (StringUtil.isNotEmptyString(gmUrl)) {
                    try {
                        succeed = sendAndGetPauseServerResult(gmUrl, req);
                    } catch (BusinessException e) {
                        // do nothing
                    }
                }
                result.put(serverId, succeed);
            }
            return result;
        }
        throw new BusinessException("gm.url.missing", "未找到任何游戏服的GM服务URL");
    }

    private boolean sendAndGetReleaseServerResult(String gmUrl, ReqMessage<ReleaseGameServerReq> req) {
        RespMessage<ReleaseGameServerRes> res;
        try {
            res = HttpUtil.gmRequest(gmUrl, req);
        } catch (Exception e) {
            LogUtil.error("重新开启游戏服对外服务出错, gmUrl:" + gmUrl, e);
            throw new BusinessException("http.request.error", "重新开启游戏服对外服务出错", e);
        }

        if (GameCodeEnum.OK.getCode() != res.getCode()) {
            LogUtil.error("重新开启游戏服对外服务失败：" + res.getParams());
        }
        return GameCodeEnum.OK.getCode() == res.getCode();
    }

    public boolean releaseServer(int gameId, int serverId) {
        String gmUrl = getGmUrlByGameServerId(gameId, serverId);
        ReqMessage<ReleaseGameServerReq> req = new ReqMessage<>();
        req.setData(new ReleaseGameServerReq());
        return sendAndGetReleaseServerResult(gmUrl, req);
    }

    public Map<Integer, Boolean> batchReleaseServer(int gameId) {
        Map<String, String> gmUrlMap = batchGetGmUrlByGameId(gameId);
        if (CollectionUtil.isNotEmpty(gmUrlMap)) {
            ReqMessage<ReleaseGameServerReq> req = new ReqMessage<>();
            req.setData(new ReleaseGameServerReq());

            Map<Integer, Boolean> result = new HashMap<>(gmUrlMap.size());
            for (Map.Entry<String, String> entry : gmUrlMap.entrySet()) {
                int serverId = Integer.parseInt(entry.getKey());
                String gmUrl = entry.getValue();
                boolean succeed = false;
                if (StringUtil.isNotEmptyString(gmUrl)) {
                    try {
                        succeed = sendAndGetReleaseServerResult(gmUrl, req);
                    } catch (BusinessException e) {
                        // do nothing
                    }
                }
                result.put(serverId, succeed);
            }
            return result;
        }
        throw new BusinessException("gm.url.missing", "未找到任何游戏服的GM服务URL");
    }

    public List<ModuleInfoDTO> getModuleList(int gameId, int serverId) {
        String gmUrl = getGmUrlByGameServerId(gameId, serverId);
        ReqMessage<GetModuleListReq> req = new ReqMessage<>();
        req.setData(new GetModuleListReq());

        RespMessage<GetModuleListRes> res;
        try {
            res = HttpUtil.gmRequest(gmUrl, req);
        } catch (Exception e) {
            LogUtil.error("获取游戏服外置模块信息出错, gmUrl:" + gmUrl, e);
            throw new BusinessException("http.request.error", "获取游戏服外置模块信息出错", e);
        }
        return res.getData().getModules();
    }

    public boolean reloadModules(int gameId, int serverId) {
        String gmUrl = getGmUrlByGameServerId(gameId, serverId);
        ReqMessage<ReloadModulesReq> req = new ReqMessage<>();
        req.setData(new ReloadModulesReq());

        RespMessage<ReloadModulesRes> res;
        try {
            res = HttpUtil.gmRequest(gmUrl, req);
        } catch (Exception e) {
            LogUtil.error("重加载游戏服外置模块信息出错, gmUrl:" + gmUrl, e);
            throw new BusinessException("http.request.error", "重加载游戏服外置模块信息出错", e);
        }

        if (GameCodeEnum.OK.getCode() != res.getCode()) {
            LogUtil.error("重加载游戏服外置模块信息失败：" + res.getParams());
        }
        return GameCodeEnum.OK.getCode() == res.getCode();
    }

    public boolean removeModule(int gameId, int serverId, String moduleName) {
        String gmUrl = getGmUrlByGameServerId(gameId, serverId);
        ReqMessage<RemoveModuleReq> req = new ReqMessage<>();
        req.setData(new RemoveModuleReq());
        req.getData().setModuleName(moduleName);

        RespMessage<RemoveModuleRes> res;
        try {
            res = HttpUtil.gmRequest(gmUrl, req);
        } catch (Exception e) {
            LogUtil.error(String.format("移除游戏服外置模块信息出错, gmUrl:%s, module:%s", gmUrl, moduleName), e);
            throw new BusinessException("http.request.error", "移除游戏服外置模块信息出错", e);
        }

        if (GameCodeEnum.OK.getCode() != res.getCode()) {
            LogUtil.error("移除游戏服外置模块信息失败：" + res.getParams());
        }
        return GameCodeEnum.OK.getCode() == res.getCode();
    }

    public GetHotfixHistoryRes getHotfixHistory(int gameId, int serverId, int pageNo, int pageSize) {
        String gmUrl = getGmUrlByGameServerId(gameId, serverId);
        ReqMessage<GetHotfixHistoryReq> req = new ReqMessage<>();
        req.setData(new GetHotfixHistoryReq());

        RespMessage<GetHotfixHistoryRes> res;
        try {
            res = HttpUtil.gmRequest(gmUrl, req);
        } catch (Exception e) {
            LogUtil.error("获取游戏服热修复记录出错, gmUrl:" + gmUrl, e);
            throw new BusinessException("http.request.error", "获取游戏服热修复记录出错", e);
        }
        return res.getData();
    }

    public String doHotfix(int gameId, int serverId, String hotfixKey) {
        String gmUrl = getGmUrlByGameServerId(gameId, serverId);
        ReqMessage<DoHotfixReq> req = new ReqMessage<>();
        req.setData(new DoHotfixReq());
        req.getData().setHotfixKey(hotfixKey);

        RespMessage<DoHotfixRes> res;
        try {
            res = HttpUtil.gmRequest(gmUrl, req);
        } catch (Exception e) {
            LogUtil.error("执行游戏服代码热修复出错, gmUrl:" + gmUrl, e);
            throw new BusinessException("http.request.error", "执行游戏服代码热修复出错", e);
        }
        return res.getData().getMessage();
    }

    private String getBatchGmUrl(int gameId, String env) {
        if (StringUtil.isEmptyString(env)) {
            return getConfigUrlPrefix() + gameId + "/param/" + gmUrlKey;
        }
        return getConfigUrlPrefix() + gameId + "/" + env + "/param/" + gmUrlKey;
    }

    private String getSingleGameServerConfigUrl(int gameId, int serverId, String env) {
        if (StringUtil.isEmptyString(env)) {
            return getConfigUrlPrefix() + gameId + "/" + serverId;
        }
        return getConfigUrlPrefix() + gameId + "/" + serverId + "/" + env;
    }

    private String getConfigUrlPrefix() {
        if (gameServerConfigUrlPrefix == null) {
            initUrlPrefix();
        }
        return gameServerConfigUrlPrefix;
    }

    private synchronized void initUrlPrefix() {
        if (gameServerConfigUrlPrefix == null) {
            if (configServerUrl != null) {
                gameServerConfigUrlPrefix = configServerUrl;
                if (!configServerUrl.endsWith("/")) {
                    gameServerConfigUrlPrefix = configServerUrl + "/";
                }
                gameServerConfigUrlPrefix += "game/server/";
            } else {
                throw new BusinessException("config.server.url", "未设置配置中心URL，请添加配置");
            }
        }
    }
}
