package cn.laoshini.dk.module.registry;

import java.util.jar.JarFile;

import cn.laoshini.dk.annotation.FunctionDependent;
import cn.laoshini.dk.dao.IEntityClassManager;
import cn.laoshini.dk.module.AbstractModuleRegistry;
import cn.laoshini.dk.module.loader.ModuleLoaderContext;
import cn.laoshini.dk.register.IClassScanner;
import cn.laoshini.dk.register.IEntityRegister;
import cn.laoshini.dk.register.Registers;

/**
 * 查找并注册数据库表单对应的实体类
 *
 * @author fagarine
 */
class ModuleEntityClassRegistry extends AbstractModuleRegistry {

    ModuleEntityClassRegistry(ModuleLoaderContext context) {
        super(context);
    }

    @FunctionDependent
    private IEntityClassManager entityClassManager;

    @Override
    public void register(JarFile jarFile) {
        for (IEntityRegister entityRegister : Registers.getEntityRegisters()) {
            IClassScanner<Class<?>> scanner = entityRegister.scanner();
            scanner.setJarFile(jarFile);
            entityRegister.action(getModuleClassLoader());
        }
    }

    @Override
    public void prepareUnregister() {
        entityClassManager.prepareUnregister(getModuleClassLoader());
    }

    @Override
    public void unregister0() {
        entityClassManager.unregister();
    }

}
