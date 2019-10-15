package cn.laoshini.dk.module;

import cn.laoshini.dk.module.loader.ModuleClassLoader;
import cn.laoshini.dk.module.loader.ModuleLoaderContext;

/**
 * 模块功能注册表基本实现
 *
 * @author fagarine
 */
public abstract class AbstractModuleRegistry implements IModuleRegistry {

    protected ModuleLoaderContext context;

    public AbstractModuleRegistry() {
    }

    public AbstractModuleRegistry(ModuleLoaderContext context) {
        this.context = context;
    }

    /**
     * 注销并清理数据，不允许子类再覆盖
     */
    @Override
    public final void unregister() {
        unregister0();

        cleanUp();
    }

    /**
     * 子类通过实现该方法注销
     */
    protected abstract void unregister0();

    /**
     * 注销后清理数据
     */
    protected void cleanUp() {
        context = null;
    }

    @Override
    public ModuleClassLoader getModuleClassLoader() {
        return context.getClassLoader();
    }

}
