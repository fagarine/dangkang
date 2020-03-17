package cn.laoshini.dk.module.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import cn.laoshini.dk.module.loader.ModuleLoaderContext;

/**
 * 整合功能注册表类，用于当康系统自带功能注册表的聚合处理
 *
 * @author fagarine
 */
public final class AggregateModuleRegistry extends AbstractRecoverableModuleRegistry {
    private List<AbstractRecoverableModuleRegistry> innerModuleRegistries;

    private AggregateModuleRegistry() {
    }

    public static AggregateModuleRegistry newInstance(ModuleLoaderContext moduleLoaderContext,
            AggregateModuleRegistry oldRegistry) {
        AggregateModuleRegistry registry = new AggregateModuleRegistry();
        registry.context = moduleLoaderContext;
        registry.innerModuleRegistries = createInnerModuleRegistries(moduleLoaderContext);
        return registry;
    }

    private static List<AbstractRecoverableModuleRegistry> createInnerModuleRegistries(
            ModuleLoaderContext moduleLoaderContext) {
        List<AbstractRecoverableModuleRegistry> registries = new ArrayList<>(8);

        registries.add(new ModuleSpringBeanRegistry(moduleLoaderContext));
        registries.add(new ModuleConfigurableFunctionRegistry(moduleLoaderContext));
        registries.add(new ModuleResourceHolderRegistry(moduleLoaderContext));
        registries.add(new ModuleMessageRegistry(moduleLoaderContext));
        registries.add(new ModuleMessageHandlerRegistry(moduleLoaderContext));
        registries.add(new ModuleEntityClassRegistry(moduleLoaderContext));

        return registries;
    }

    @Override
    public void prepareRegister(JarFile moduleJarFile) {
        for (AbstractRecoverableModuleRegistry moduleRegistry : innerModuleRegistries) {
            moduleRegistry.prepareRegister(moduleJarFile);
            moduleRegistry.toPrepareRegister();
        }
        toPrepareRegister();
    }

    @Override
    public void register(JarFile moduleJarFile) {
        for (AbstractRecoverableModuleRegistry moduleRegistry : innerModuleRegistries) {
            moduleRegistry.register(moduleJarFile);
            moduleRegistry.toRegistered();
        }
        toRegistered();
    }

    @Override
    public void prepareUnregister() {
        for (AbstractRecoverableModuleRegistry moduleRegistry : innerModuleRegistries) {
            moduleRegistry.prepareUnregister();
            moduleRegistry.toPrepareUnregister();
        }
        toPrepareUnregister();
    }

    @Override
    public void unregister0() {
        for (AbstractRecoverableModuleRegistry moduleRegistry : innerModuleRegistries) {
            moduleRegistry.unregister();
            moduleRegistry.toUnregistered();
        }
        toUnregistered();
    }

    @Override
    protected void cleanUp() {
        super.cleanUp();

        innerModuleRegistries.clear();
        innerModuleRegistries = null;
    }

    @Override
    public void rollback() {
        for (AbstractRecoverableModuleRegistry moduleRegistry : innerModuleRegistries) {
            moduleRegistry.rollback();
        }
    }
}
