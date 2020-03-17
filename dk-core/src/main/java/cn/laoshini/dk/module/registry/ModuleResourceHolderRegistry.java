package cn.laoshini.dk.module.registry;

import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarFile;

import cn.laoshini.dk.annotation.ResourceHolder;
import cn.laoshini.dk.manager.ResourceHolderManager;
import cn.laoshini.dk.module.loader.ModuleLoaderContext;
import cn.laoshini.dk.util.ClassUtil;

/**
 * 模块资源持有者相关信息注册表
 *
 * @author fagarine
 */
class ModuleResourceHolderRegistry extends AbstractRecoverableModuleRegistry {

    private List<Class<?>> holderClasses = new LinkedList<>();

    ModuleResourceHolderRegistry(ModuleLoaderContext context) {
        super(context);
    }

    @Override
    public void prepareRegister(JarFile moduleJarFile) {
        super.prepareRegister(moduleJarFile);

        List<Class<?>> classes = ClassUtil
                .getAllClassByAnnotationInJarFile(moduleJarFile, getModuleClassLoader(), null, ResourceHolder.class);
        holderClasses.addAll(classes);
    }

    @Override
    protected void cancelPrepareRegister() {
        super.cancelPrepareRegister();
        holderClasses.clear();
    }

    @Override
    public void register(JarFile jarFile) {
        if (!holderClasses.isEmpty()) {
            ResourceHolderManager.batchRegister(holderClasses);
            holderClasses.clear();
        }
    }

    @Override
    public void prepareUnregister() {
        ResourceHolderManager.prepareUnregister(getModuleClassLoader());
    }

    @Override
    protected void cancelPrepareUnregister() {
        ResourceHolderManager.cancelPrepareUnregister();
    }

    @Override
    protected void unregister0() {
        ResourceHolderManager.unregisterOldHolders();
    }

    @Override
    protected void cleanUp() {
        super.cleanUp();

        holderClasses.clear();
        holderClasses = null;
    }
}
