package cn.laoshini.dk.starter;

import cn.laoshini.dk.common.SpringContextHolder;
import cn.laoshini.dk.jit.DynamicGenerator;
import cn.laoshini.dk.manager.HotfixManager;
import cn.laoshini.dk.manager.TypeUseManager;

/**
 * @author fagarine
 */
final class ExternalLastInitializer {

    static void afterInitializationProcess() {
        // 初始化热修复功能
        SpringContextHolder.getBean(HotfixManager.class).init();

        boolean jit = SpringContextHolder.getBoolProperty("dk.jit", false);
        // JIT生成类加载、注册
        if (jit) {
            DynamicGenerator.loadAndRegisterGeneratedClass();
        }

        // 常用类注册
        boolean expression = SpringContextHolder.getBoolProperty("dk.expression", false);
        if (expression) {
            TypeUseManager.registerIntrinsicClasses();
        }
    }
}
