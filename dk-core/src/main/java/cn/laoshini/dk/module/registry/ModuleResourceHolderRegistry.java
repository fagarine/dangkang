package cn.laoshini.dk.module.registry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

import cn.laoshini.dk.annotation.ResourceHolder;
import cn.laoshini.dk.function.ConfigurableFunctionInjector;
import cn.laoshini.dk.manager.ResourceHolderManager;
import cn.laoshini.dk.module.AbstractModuleRegistry;
import cn.laoshini.dk.module.loader.ModuleLoaderContext;
import cn.laoshini.dk.util.ClassUtil;

/**
 * 模块资源持有者相关信息注册表
 *
 * @author fagarine
 */
public class ModuleResourceHolderRegistry extends AbstractModuleRegistry {

    public ModuleResourceHolderRegistry(ModuleLoaderContext context) {
        super(context);
    }

    private Set<String> holderKeys = new HashSet<>();

    @Override
    public void register(JarFile jarFile) {
        List<Class<?>> classes = ClassUtil
                .getAllClassByAnnotationInJarFile(jarFile, getModuleClassLoader(), null, ResourceHolder.class);
        ResourceHolderManager.batchRegister(classes);

        ConfigurableFunctionInjector.findAndRejectFunctionByModule(getModuleClassLoader());
    }

    @Override
    public void prepareUnregister() {
        ResourceHolderManager.prepareUnregister(holderKeys);
    }

    @Override
    protected void unregister0() {
        ResourceHolderManager.unregisterOldHolders();
    }

    @Override
    protected void cleanUp() {
        super.cleanUp();

        holderKeys.clear();
        holderKeys = null;
    }
}
