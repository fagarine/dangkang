package cn.laoshini.dk.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.laoshini.dk.annotation.FunctionDependent;
import cn.laoshini.dk.constant.HotfixResultEnum;
import cn.laoshini.dk.dao.IDefaultDao;
import cn.laoshini.dk.domain.common.HotfixFile;
import cn.laoshini.dk.entity.HotfixRecord;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.util.FileUtil;
import cn.laoshini.dk.util.HotfixUtil;
import cn.laoshini.dk.util.LogUtil;

/**
 * @author fagarine
 */
@Component
public class HotfixManager {

    @FunctionDependent(nullable = true)
    private IDefaultDao defaultDao;

    @Value("${dk.hotfix:#{dangKangBasicProperties.hotfix}}")
    private String hotfixDir;

    /**
     * 记录已热更过的类文件，key: 热更类全名, value: 文件最后修改时间
     */
    private Map<String, Long> hotfixMap = new HashMap<>();

    public void init() {
        if (HotfixUtil.isValid()) {
            try {
                // 记录并清空热更class文件
                File hotfixRootDir = new File(FileUtil.getProjectPath(hotfixDir));
                FileUtil.clearHotfixDir(hotfixRootDir, hotfixMap);
            } catch (Exception e) {
                throw new BusinessException("hotfix.init.error", "热修复功能初始化出错", e);
            }
        } else {
            LogUtil.error("启动项未添加java agent，热修复功能不可用！！！");
        }
    }

    /**
     * 执行热修复操作
     *
     * @param hotfixKey 本次执行的key
     * @return 返回执行结果信息
     */
    public String doHotfix(String hotfixKey) {
        if (!HotfixUtil.isValid()) {
            throw new BusinessException("hotfix.not.supported", "当前热修复功能不可用，请检查启动项，确认javaagent配置正确");
        }

        StringBuilder detail = new StringBuilder();

        List<HotfixFile> hotfixFileList = new ArrayList<>();
        File hotfixRootDir = new File(FileUtil.getProjectPath(hotfixDir));
        FileUtil.readHotfixFiles(hotfixRootDir, hotfixFileList);

        int skipCount = 0;
        int succeedCount = 0;
        HotfixRecord record;
        HotfixResultEnum result;
        Date hotfixTime = new Date();
        List<HotfixRecord> records = null;
        if (defaultDao != null) {
            records = new ArrayList<>(hotfixFileList.size());
        }
        for (HotfixFile hotfixFile : hotfixFileList) {
            String className = hotfixFile.getFullClassName();

            detail.append("\r\n").append("文件 ").append(hotfixFile.getFilePath());
            if (hotfixMap.containsKey(className) && hotfixMap.get(className) == hotfixFile.getLastModifyTime()) {
                detail.append(" 没有改变，跳过");
                skipCount++;
                result = HotfixResultEnum.NO_CHANGE;
            } else {
                result = HotfixUtil.redefineClass(hotfixFile);
                if (HotfixResultEnum.SUCCEED.equals(result)) {
                    detail.append(" 执行成功");
                    succeedCount++;
                } else {
                    detail.append(" 执行失败!");
                }
            }

            if (records != null) {
                record = new HotfixRecord();
                record.setHotfixKey(hotfixKey);
                record.setClassName(className);
                record.setHotfixTime(hotfixTime);
                record.setResult(result.name());
                record.setDesc(className + " " + result.getDesc());
                records.add(record);
            }
        }

        saveHotfixRecord(records);

        int failCount = hotfixFileList.size() - skipCount - succeedCount;
        return "本次热修复，共找到文件[" + hotfixFileList.size() + "]个，其中跳过[" + skipCount + "]个，成功[" + succeedCount + "]个，失败["
               + failCount + "]个，详细情况：" + "[" + detail + "\r\n]";
    }

    private void saveHotfixRecord(List<HotfixRecord> records) {
        if (defaultDao != null) {
            defaultDao.saveRelationalEntityList(records);
        }
    }
}
