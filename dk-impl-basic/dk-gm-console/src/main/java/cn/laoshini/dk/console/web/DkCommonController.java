package cn.laoshini.dk.console.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.laoshini.dk.console.service.GameServerService;
import cn.laoshini.dk.console.vo.HotfixRecordVO;
import cn.laoshini.dk.console.vo.ModuleInfoVO;
import cn.laoshini.dk.domain.dto.HotfixRecordDTO;
import cn.laoshini.dk.domain.dto.ModuleInfoDTO;
import cn.laoshini.dk.domain.query.Page;
import cn.laoshini.dk.gm.message.hotfix.GetHotfixHistoryRes;
import cn.laoshini.dk.util.LogUtil;

/**
 * @author fagarine
 */
@RestController
@RequestMapping("/dk")
public class DkCommonController {

    @Resource
    private GameServerService gameServerService;

    /**
     * 获取所有已加载的外置模块信息
     *
     * @param gameId 游戏id
     * @param serverId 游戏服id
     * @return 返回模块信息列表
     */
    @GetMapping("/{gameId}/{serverId}/modules")
    public List<ModuleInfoVO> getModuleList(@PathVariable("gameId") int gameId,
            @PathVariable("serverId") int serverId) {
        LogUtil.info("get game server modules, gameId: {}, serverId: {}", gameId, serverId);
        List<ModuleInfoDTO> modules = gameServerService.getModuleList(gameId, serverId);
        if (CollectionUtils.isEmpty(modules)) {
            return Collections.emptyList();
        }

        List<ModuleInfoVO> infos = new ArrayList<>(modules.size());
        for (ModuleInfoDTO module : modules) {
            ModuleInfoVO vo = new ModuleInfoVO();
            BeanUtils.copyProperties(module, vo);
            infos.add(vo);
        }
        return infos;
    }

    /**
     * 重加载外置模块
     *
     * @param gameId 游戏id
     * @param serverId 游戏服id
     */
    @PostMapping("/{gameId}/{serverId}/module/latest")
    public boolean reloadModules(@PathVariable("gameId") int gameId, @PathVariable("serverId") int serverId) {
        LogUtil.info("reload game server modules, gameId: {}, serverId: {}", gameId, serverId);
        return gameServerService.reloadModules(gameId, serverId);
    }

    /**
     * 移除指定外置模块
     *
     * @param gameId 游戏id
     * @param serverId 游戏服id
     * @param moduleName 模块名称
     */
    @DeleteMapping("/{gameId}/{serverId}/module/{moduleName}")
    public boolean removeModule(@PathVariable("gameId") int gameId, @PathVariable("serverId") int serverId,
            @PathVariable("moduleName") String moduleName) {
        LogUtil.info("remove game server modules, gameId: {}, serverId: {}, module: {}", gameId, serverId, moduleName);
        return gameServerService.removeModule(gameId, serverId, moduleName);
    }

    /**
     * 分页查询热修复的日志记录（仅在使用了数据库记录日志的情况下有效）
     *
     * @param gameId 游戏id
     * @param serverId 游戏服id
     * @param pageNo 页码，从1开始
     * @param pageSize 单页数据条数
     * @return 返回查询结果
     */
    @GetMapping("/{gameId}/{serverId}/hotfix/page")
    public Page<HotfixRecordVO> getHotfixHistory(@PathVariable("gameId") int gameId,
            @PathVariable("serverId") int serverId, int pageNo, int pageSize) {
        LogUtil.info("get game server hotfix history, gameId: {}, serverId: {}, pageNo: {}, pageSize: {}", gameId,
                serverId, pageNo, pageSize);
        GetHotfixHistoryRes res = gameServerService.getHotfixHistory(gameId, serverId, pageNo, pageSize);
        if (res.getTotal() == 0) {
            return new Page<>(res.getPageNo(), res.getPageSize(), 0, Collections.emptyList());
        }

        List<HotfixRecordVO> list = new ArrayList<>(res.getRecords().size());
        for (HotfixRecordDTO record : res.getRecords()) {
            HotfixRecordVO vo = new HotfixRecordVO();
            BeanUtils.copyProperties(record, vo);
            list.add(vo);
        }
        return new Page<>(res.getPageNo(), res.getPageSize(), res.getTotal(), list);
    }

    /**
     * 立即执行代码热修复
     *
     * @param gameId 游戏id
     * @param serverId 游戏服id
     * @param hotfixKey 唯一key，非必传，如果该值为null或空字符串，将会使用游戏服当前时间作为key
     * @return 返回执行结果
     */
    @PostMapping("/{gameId}/{serverId}/hotfix/latest")
    public String doHotfix(@PathVariable("gameId") int gameId, @PathVariable("serverId") int serverId,
            String hotfixKey) {
        LogUtil.info("do game server hotfix, gameId: {}, serverId: {}, hotfixKey: {}", gameId, serverId, hotfixKey);
        return gameServerService.doHotfix(gameId, serverId, hotfixKey);
    }

}
