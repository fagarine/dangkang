package cn.laoshini.dk.module.registry;

import java.util.jar.JarFile;

import cn.laoshini.dk.module.AbstractModuleRegistry;
import cn.laoshini.dk.module.loader.ModuleLoaderContext;
import cn.laoshini.dk.net.MessageHandlerHolder;
import cn.laoshini.dk.register.IClassScanner;
import cn.laoshini.dk.register.IMessageHandlerRegister;
import cn.laoshini.dk.register.Registers;

/**
 * 查找并注册消息处理handler
 *
 * @author fagarine
 */
class ModuleMessageHandlerRegistry extends AbstractModuleRegistry {

    ModuleMessageHandlerRegistry(ModuleLoaderContext context) {
        super(context);
    }

    @Override
    public void register(JarFile jarFile) {
        for (IMessageHandlerRegister handlerRegister : Registers.getHandlerRegisters()) {
            IClassScanner<Class<?>> scanner = handlerRegister.scanner();
            if (scanner != null) {
                scanner.setJarFile(jarFile);
                handlerRegister.action(getModuleClassLoader());
            }
        }
    }

    @Override
    public void prepareUnregister() {
        MessageHandlerHolder.prepareUnregisterHandlers(getModuleClassLoader());
    }

    @Override
    public void unregister0() {
        MessageHandlerHolder.unregisterHandlers(getModuleClassLoader());
    }

}
