package cn.laoshini.dk.module.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import cn.laoshini.dk.module.AbstractModuleRegistry;
import cn.laoshini.dk.module.loader.ModuleLoaderContext;

/**
 * 整合功能注册表类，用于当康系统自带功能注册表的聚合处理
 *
 * @author fagarine
 */
public final class AggregateModuleRegistry extends AbstractModuleRegistry {
    private AggregateModuleRegistry() {
    }

    private List<AbstractModuleRegistry> innerModuleRegistries;

    public static AggregateModuleRegistry newInstance(ModuleLoaderContext moduleLoaderContext,
            AggregateModuleRegistry oldRegistry) {
        AggregateModuleRegistry registry = new AggregateModuleRegistry();
        registry.context = moduleLoaderContext;
        registry.innerModuleRegistries = createInnerModuleRegistries(moduleLoaderContext);
        return registry;
    }

    private static List<AbstractModuleRegistry> createInnerModuleRegistries(ModuleLoaderContext moduleLoaderContext) {
        List<AbstractModuleRegistry> registries = new ArrayList<>(8);

        registries.add(new ModuleSpringBeanRegistry(moduleLoaderContext));
        registries.add(new ModuleConfigurableFunctionRegistry(moduleLoaderContext));
        registries.add(new ModuleResourceHolderRegistry(moduleLoaderContext));
        registries.add(new ModuleMessageHandlerRegistry(moduleLoaderContext));
        registries.add(new ModuleMessageRegistry(moduleLoaderContext));
        registries.add(new ModuleEntityClassRegistry(moduleLoaderContext));

        return registries;
    }

    @Override
    public void register(JarFile moduleJarFile) {
        for (AbstractModuleRegistry moduleRegistry : innerModuleRegistries) {
            moduleRegistry.register(moduleJarFile);
        }
    }

    @Override
    public void prepareUnregister() {
        for (AbstractModuleRegistry moduleRegistry : innerModuleRegistries) {
            moduleRegistry.prepareUnregister();
        }
    }

    @Override
    public void unregister0() {
        for (AbstractModuleRegistry moduleRegistry : innerModuleRegistries) {
            moduleRegistry.unregister();
        }
    }

    @Override
    protected void cleanUp() {
        super.cleanUp();

        innerModuleRegistries.clear();
        innerModuleRegistries = null;
    }
}
