package cn.laoshini.dk.module.registry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import cn.laoshini.dk.annotation.FunctionDependent;
import cn.laoshini.dk.dao.IEntityClassManager;
import cn.laoshini.dk.module.loader.ModuleLoaderContext;
import cn.laoshini.dk.register.IClassScanner;
import cn.laoshini.dk.register.IEntityRegister;
import cn.laoshini.dk.register.Registers;

/**
 * 查找并注册数据库表单对应的实体类
 *
 * @author fagarine
 */
class ModuleEntityClassRegistry extends AbstractRecoverableModuleRegistry {

    @FunctionDependent
    private IEntityClassManager entityClassManager;
    private Map<IEntityRegister, List<Class<?>>> entityClassesMap = new LinkedHashMap<>();

    ModuleEntityClassRegistry(ModuleLoaderContext context) {
        super(context);
    }

    @Override
    public void prepareRegister(JarFile jarFile) {
        super.prepareRegister(jarFile);

        for (IEntityRegister entityRegister : Registers.getEntityRegisters()) {
            IClassScanner<Class<?>> scanner = entityRegister.scanner();
            try {
                scanner.setJarFile(jarFile);
                entityClassesMap.put(entityRegister, scanner.findClasses(getModuleClassLoader()));
            } finally {
                scanner.setJarFile(null);
            }
        }
    }

    @Override
    protected void cancelPrepareRegister() {
        super.cancelPrepareRegister();
        entityClassesMap.clear();
    }

    @Override
    public void register(JarFile jarFile) {
        if (!entityClassesMap.isEmpty()) {
            for (Map.Entry<IEntityRegister, List<Class<?>>> entry : entityClassesMap.entrySet()) {
                entry.getKey().registerEntityClasses(entry.getValue());
                entry.getValue().clear();
            }
            entityClassesMap.clear();
        }
    }

    @Override
    public void prepareUnregister() {
        entityClassManager.prepareUnregister(getModuleClassLoader());
    }

    @Override
    protected void cancelPrepareUnregister() {
        entityClassManager.cancelPrepareUnregister();
    }

    @Override
    public void unregister0() {
        entityClassManager.unregister();
    }

    @Override
    protected void cleanUp() {
        super.cleanUp();

        entityClassesMap.clear();
        entityClassesMap = null;
    }
}
