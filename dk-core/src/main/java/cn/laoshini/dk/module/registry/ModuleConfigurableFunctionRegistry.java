package cn.laoshini.dk.module.registry;

import java.util.Map;
import java.util.jar.JarFile;

import cn.laoshini.dk.function.VariousWaysManager;
import cn.laoshini.dk.module.loader.ModuleLoaderContext;

/**
 * 查找并注册可配置功能
 *
 * @author fagarine
 */
class ModuleConfigurableFunctionRegistry extends AbstractRecoverableModuleRegistry {

    /**
     * 记录模块中已注册的可配置功能实现类，key: 功能定义类, value: 实现类的key
     */
    private Map<Class<?>, String> configurableFunctionMap;

    ModuleConfigurableFunctionRegistry(ModuleLoaderContext context) {
        super(context);
    }

    @Override
    public void register(JarFile jarFile) {
        configurableFunctionMap = VariousWaysManager
                .findAndRegisterVariousWaysClassesInJar(jarFile, getModuleClassLoader(), null);
    }

    @Override
    public void prepareUnregister() {
        // do nothing
    }

    @Override
    protected void cancelPrepareUnregister() {
        // do nothing
    }

    @Override
    public void unregister0() {
        VariousWaysManager.batchUnregister(configurableFunctionMap);
    }

    @Override
    protected void cleanUp() {
        super.cleanUp();

        configurableFunctionMap.clear();
        configurableFunctionMap = null;
    }
}
