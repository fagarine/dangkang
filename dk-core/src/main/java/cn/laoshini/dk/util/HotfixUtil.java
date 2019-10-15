package cn.laoshini.dk.util;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.UnmodifiableClassException;

import org.springframework.util.FileCopyUtils;

import cn.laoshini.dk.agent.DangKangAgent;
import cn.laoshini.dk.constant.HotfixResultEnum;
import cn.laoshini.dk.domain.common.HotfixFile;

/**
 * 类文件热修复功能，使用java agent实现，只能热更新方法体的实现内容，达到热修复的目的
 *
 * @author fagarine
 */
public class HotfixUtil {
    private HotfixUtil() {
    }

    public static boolean isValid() {
        return DangKangAgent.inst != null;
    }

    /**
     * 重定义类
     *
     * @param hotfixFile 类文件描述信息
     * @return 返回执行结果
     */
    public static HotfixResultEnum redefineClass(HotfixFile hotfixFile) {
        if (DangKangAgent.inst == null) {
            return HotfixResultEnum.NO_AGENT;
        }

        // 查找类
        Class<?> clazz = ClassHelper.getClassAnywhere(hotfixFile.getFullClassName());
        if (clazz == null) {
            return HotfixResultEnum.NO_CLASS;
        }

        // 读取热更新文件
        File file = new File(hotfixFile.getFilePath());
        byte[] bytes;
        try {
            bytes = FileCopyUtils.copyToByteArray(file);
        } catch (IOException e) {
            e.printStackTrace();
            return HotfixResultEnum.EXCEPTION;
        }

        try {
            // 执行更新操作
            DangKangAgent.redefineClass(clazz, bytes);
        } catch (ClassNotFoundException | UnmodifiableClassException e) {
            // 前面已经做了判断，不应该出现这样的异常
            e.printStackTrace();
            return HotfixResultEnum.EXCEPTION;
        }
        return HotfixResultEnum.SUCCEED;
    }

}
