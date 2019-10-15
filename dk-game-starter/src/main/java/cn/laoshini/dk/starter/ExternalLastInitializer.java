package cn.laoshini.dk.starter;

import cn.laoshini.dk.autoconfigure.DangKangBasicProperties;
import cn.laoshini.dk.common.SpringContextHolder;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.jit.DynamicGenerator;
import cn.laoshini.dk.manager.HotfixManager;
import cn.laoshini.dk.manager.ModuleManager;
import cn.laoshini.dk.manager.TypeUseManager;

/**
 * @author fagarine
 */
class ExternalLastInitializer {

    static void afterInitializationProcess() {
        // 初始化热修复功能
        SpringContextHolder.getBean(HotfixManager.class).init();

        DangKangBasicProperties dangKangBasicProperties = SpringContextHolder.getBean(DangKangBasicProperties.class);
        // JIT生成类加载、注册
        if (dangKangBasicProperties.isJit()) {
            DynamicGenerator.loadAndRegisterGeneratedClass();
        }

        // 常用类注册
        if (dangKangBasicProperties.isExpression()) {
            TypeUseManager.registerIntrinsicClasses();
        }

        // 加载外置功能模块
        try {
            ModuleManager.initModuleSystem();
        } catch (Exception e) {
            throw new BusinessException("module.init.error", "初始化外置功能模块出错", e);
        }
    }
}
