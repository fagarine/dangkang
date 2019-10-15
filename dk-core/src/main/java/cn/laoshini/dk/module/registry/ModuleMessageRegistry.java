package cn.laoshini.dk.module.registry;

import java.util.jar.JarFile;

import cn.laoshini.dk.domain.common.Tuple;
import cn.laoshini.dk.module.AbstractModuleRegistry;
import cn.laoshini.dk.module.loader.ModuleLoaderContext;
import cn.laoshini.dk.net.MessageHolder;
import cn.laoshini.dk.register.IClassScanner;
import cn.laoshini.dk.register.IMessageDtoRegister;
import cn.laoshini.dk.register.IMessageRegister;
import cn.laoshini.dk.register.Registers;

/**
 * 查找并注册游戏消息类
 *
 * @author fagarine
 */
class ModuleMessageRegistry extends AbstractModuleRegistry {

    ModuleMessageRegistry(ModuleLoaderContext context) {
        super(context);
    }

    @Override
    public void register(JarFile jarFile) {
        for (IMessageRegister messageRegister : Registers.getMessageRegisters()) {
            IClassScanner<Class<?>> scanner = messageRegister.scanner();
            if (scanner != null) {
                scanner.setJarFile(jarFile);
                messageRegister.action(getModuleClassLoader());
            }
        }

        for (IMessageDtoRegister dtoRegister : Registers.getDtoRegisters()) {
            IClassScanner<Tuple<Integer, Class<?>>> scanner = dtoRegister.dtoScanner();
            if (scanner != null) {
                scanner.setJarFile(jarFile);
                dtoRegister.action(getModuleClassLoader());
            }
        }
    }

    @Override
    public void prepareUnregister() {
        MessageHolder.prepareUnregisterMessages(getModuleClassLoader());
    }

    @Override
    public void unregister0() {
        MessageHolder.unregisterMessages(getModuleClassLoader());
    }

    @Override
    protected void cleanUp() {
        super.cleanUp();
    }
}
