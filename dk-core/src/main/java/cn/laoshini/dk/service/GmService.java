package cn.laoshini.dk.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Resource;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import cn.laoshini.dk.constant.GameServerStatus;
import cn.laoshini.dk.domain.dto.GameServerInfoDTO;
import cn.laoshini.dk.domain.dto.HotfixRecordDTO;
import cn.laoshini.dk.domain.dto.ModuleInfoDTO;
import cn.laoshini.dk.domain.query.Page;
import cn.laoshini.dk.domain.query.PageQueryCondition;
import cn.laoshini.dk.entity.HotfixRecord;
import cn.laoshini.dk.manager.HotfixManager;
import cn.laoshini.dk.manager.ModuleManager;
import cn.laoshini.dk.server.AbstractGameServer;
import cn.laoshini.dk.server.GameServers;
import cn.laoshini.dk.util.DateUtil;
import cn.laoshini.dk.util.StringUtil;

/**
 * 为GM服务器提供的服务类，本类提供一系列快速管理游戏的方法
 *
 * @author fagarine
 */
@Service
public class GmService {

    @Resource
    private HotfixManager hotfixManager;

    /**
     * 获取所有已加载的外置模块信息
     *
     * @return 返回模块信息列表
     */
    public List<ModuleInfoDTO> getModuleList() {
        return ModuleManager.getModuleList();
    }

    /**
     * 重加载外置模块
     */
    public void reloadModules() {
        ModuleManager.loadModules();
    }

    /**
     * 移除指定外置模块
     *
     * @param moduleName 模块名称
     */
    public void removeModule(String moduleName) {
        ModuleManager.removeModule(moduleName);
    }

    /**
     * 分页查询热修复的日志记录（仅在使用了数据库记录日志的情况下有效）
     *
     * @param pageQueryCondition 查询条件
     * @return 返回查询结果
     */
    public Page<HotfixRecordDTO> getHotfixHistory(PageQueryCondition pageQueryCondition) {
        Page<HotfixRecord> page = hotfixManager.searchHistoryPage(pageQueryCondition);
        if (page.getTotal() == 0) {
            return new Page<>(page.getPageNo(), page.getPageSize(), 0, Collections.emptyList());
        }

        List<HotfixRecordDTO> list = new ArrayList<>(page.getResult().size());
        for (HotfixRecord record : page.getResult()) {
            HotfixRecordDTO dto = new HotfixRecordDTO();
            BeanUtils.copyProperties(record, dto);
            list.add(dto);
        }
        return new Page<>(page.getPageNo(), page.getPageSize(), page.getTotal(), list);
    }

    /**
     * 立即执行代码热修复
     *
     * @param hotfixKey 唯一key，如果传入null或空字符串，将会使用当前时间作为key
     * @return 返回执行结果
     */
    public String doHotfix(String hotfixKey) {
        String key = StringUtil.isEmptyString(hotfixKey) ? DateUtil.nowMilliNumberFormat() : hotfixKey.trim();
        return hotfixManager.doHotfix(key);
    }

    /**
     * 获取所有游戏服信息
     *
     * @return 服务器信息列表
     */
    public List<GameServerInfoDTO> getAllServerInfo() {
        return GameServers.getServerInfos(null);
    }

    /**
     * 获取指定游戏所有服务器信息
     *
     * @param gameId 游戏id
     * @return 服务器信息列表
     */
    public List<GameServerInfoDTO> getServerInfoByGame(int gameId) {
        return GameServers.getServerInfos(gameFilter(gameId));
    }

    /**
     * 获取指定游戏所有运行中的服务器信息
     *
     * @param gameId 游戏id
     * @return 服务器信息列表
     */
    public List<GameServerInfoDTO> getRunningServerInfoByGame(int gameId) {
        return GameServers.getServerInfos(gameAndStatusFilter(gameId, GameServerStatus.RUN));
    }

    /**
     * 获取指定游戏所有停止对外服务的服务器信息
     *
     * @param gameId 游戏id
     * @return 服务器信息列表
     */
    public List<GameServerInfoDTO> getPauseServerInfoByGame(int gameId) {
        return GameServers.getServerInfos(gameAndStatusFilter(gameId, GameServerStatus.PAUSE));
    }

    /**
     * 获取指定游戏所有已关闭（未启动）的服务器信息
     *
     * @param gameId 游戏id
     * @return 服务器信息列表
     */
    public List<GameServerInfoDTO> getClosedServerInfoByGame(int gameId) {
        return GameServers.getServerInfos(gameAndStatusFilter(gameId, GameServerStatus.CLOSE));
    }

    /**
     * 获取所有运行中的游戏信息
     *
     * @return 服务器信息列表
     */
    public List<GameServerInfoDTO> getRunningServerInfos() {
        return GameServers.getServerInfos(statusFilter(GameServerStatus.RUN));
    }

    /**
     * 获取所有停止对外服务的游戏信息
     *
     * @return 服务器信息列表
     */
    public List<GameServerInfoDTO> getPauseServerInfos() {
        return GameServers.getServerInfos(statusFilter(GameServerStatus.PAUSE));
    }

    /**
     * 获取所有已关闭（未启动）的游戏信息
     *
     * @return 服务器信息列表
     */
    public List<GameServerInfoDTO> getClosedServerInfos() {
        return GameServers.getServerInfos(statusFilter(GameServerStatus.CLOSE));
    }

    /**
     * 停止所有服务器对外服务
     *
     * @param tips 停止服务后，对玩家的提示信息
     * @param openTime 预计开放时间，如果该值不为null，系统将在开放时间到达后自动开放对外服务
     */
    public void pauseAll(String tips, Date openTime) {
        GameServers.pauseServers(tips, openTime, null);
    }

    /**
     * 停止指定游戏下的所有服务器对外服务
     *
     * @param gameId 游戏id
     * @param tips 停止服务后，对玩家的提示信息
     * @param openTime 预计开放时间，如果该值不为null，系统将在开放时间到达后自动开放对外服务
     */
    public void pauseByGame(int gameId, String tips, Date openTime) {
        GameServers.pauseServers(tips, openTime, gameFilter(gameId));
    }

    /**
     * 停止指定服务器对外服务
     *
     * @param serverId 服务器id
     * @param tips 停止服务后，对玩家的提示信息
     * @param openTime 预计开放时间，如果该值不为null，系统将在开放时间到达后自动开放对外服务
     */
    public void pauseById(int serverId, String tips, Date openTime) {
        GameServers.pauseById(serverId, tips, openTime);
    }

    /**
     * 对所有服务器开放对外服务
     */
    public void releaseAll() {
        GameServers.releaseServers(null);
    }

    /**
     * 对指定游戏下的所有服务器开放对外服务
     *
     * @param gameId 游戏id
     */
    public void releaseByGame(int gameId) {
        GameServers.releaseServers(gameFilter(gameId));
    }

    /**
     * 对指定服务器开放对外服务
     *
     * @param serverId 服务器id
     */
    public void releaseById(int serverId) {
        GameServers.releaseById(serverId);
    }

    /**
     * 关闭指定游戏的所有服务器
     *
     * @param gameId 游戏id
     */
    public void shutdownServerByGame(int gameId) {
        GameServers.shutdownServers(gameFilter(gameId));
    }

    /**
     * 关闭指定服务器
     *
     * @param serverId 服务器id
     */
    public void shutdownServerById(int serverId) {
        GameServers.shutdownServerById(serverId);
    }

    /**
     * 启动指定游戏的所有已配置服务器
     *
     * @param gameId 游戏id
     */
    public void startupServerByGame(int gameId) {
        GameServers.startupServers(gameFilter(gameId));
    }

    /**
     * 启动指定服务器
     *
     * @param serverId 服务器id
     */
    public void startupServerById(int serverId) {
        GameServers.startupServerById(serverId);
    }

    /**
     * 根据游戏id过滤游戏服
     *
     * @param gameId 游戏id
     * @return 返回过滤器对象
     */
    private Predicate<AbstractGameServer> gameFilter(int gameId) {
        return server -> server.getGameId() == gameId;
    }

    /**
     * 根据服务器状态过滤游戏服
     *
     * @param status 服务器状态枚举
     * @return 返回过滤器对象
     */
    private Predicate<AbstractGameServer> statusFilter(GameServerStatus status) {
        return server -> validateServerStatus(server, status);
    }

    /**
     * 根据游戏id和服务器状态过滤游戏服
     *
     * @param gameId 游戏id
     * @param status 服务器状态枚举
     * @return 返回过滤器对象
     */
    private Predicate<AbstractGameServer> gameAndStatusFilter(int gameId, GameServerStatus status) {
        return server -> server.getGameId() == gameId && validateServerStatus(server, status);
    }

    /**
     * 验证服务器是否正处于指定状态
     *
     * @param server 服务器对象
     * @param status 服务器状态枚举
     * @return 返回验证结果
     */
    private boolean validateServerStatus(AbstractGameServer server, GameServerStatus status) {
        switch (status) {
            case RUN:
                return !server.isPaused() && !server.isShutdown();
            case CLOSE:
                return server.isShutdown();
            case PAUSE:
                return server.isPaused() && !server.isShutdown();
            default:
                return false;
        }
    }
}
