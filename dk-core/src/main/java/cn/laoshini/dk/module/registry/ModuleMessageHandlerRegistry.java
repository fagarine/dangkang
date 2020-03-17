package cn.laoshini.dk.module.registry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

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
class ModuleMessageHandlerRegistry extends AbstractRecoverableModuleRegistry {

    private Map<IMessageHandlerRegister, List<Class<?>>> handlerClassesMap = new LinkedHashMap<>();

    ModuleMessageHandlerRegistry(ModuleLoaderContext context) {
        super(context);
    }

    @Override
    public void prepareRegister(JarFile moduleJarFile) {
        super.prepareRegister(moduleJarFile);

        for (IMessageHandlerRegister handlerRegister : Registers.getHandlerRegisters()) {
            IClassScanner<Class<?>> scanner = handlerRegister.scanner();
            if (scanner != null) {
                try {
                    scanner.setJarFile(moduleJarFile);
                    handlerClassesMap.put(handlerRegister, scanner.findClasses(getModuleClassLoader()));
                } finally {
                    scanner.setJarFile(null);
                }
            }
        }
    }

    @Override
    protected void cancelPrepareRegister() {
        super.cancelPrepareRegister();
        handlerClassesMap.clear();
    }

    @Override
    public void register(JarFile jarFile) {
        if (!handlerClassesMap.isEmpty()) {
            for (Map.Entry<IMessageHandlerRegister, List<Class<?>>> entry : handlerClassesMap.entrySet()) {
                IMessageHandlerRegister handlerRegister = entry.getKey();
                for (Class<?> handlerClass : entry.getValue()) {
                    handlerRegister.registerHandlerClass(handlerClass);
                }
                entry.getValue().clear();
            }
            handlerClassesMap.clear();
        }
    }

    @Override
    public void prepareUnregister() {
        MessageHandlerHolder.prepareUnregisterHandlers(getModuleClassLoader());
    }

    @Override
    protected void cancelPrepareUnregister() {
        MessageHandlerHolder.cancelPrepareUnregister();
    }

    @Override
    public void unregister0() {
        MessageHandlerHolder.unregisterHandlers(getModuleClassLoader());
    }

    @Override
    protected void cleanUp() {
        super.cleanUp();

        handlerClassesMap.clear();
        handlerClassesMap = null;
    }
}
