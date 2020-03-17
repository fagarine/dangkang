package cn.laoshini.dk.module.registry;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.jar.JarFile;

import cn.laoshini.dk.domain.common.Tuple;
import cn.laoshini.dk.module.loader.ModuleLoaderContext;
import cn.laoshini.dk.net.MessageDtoClassHolder;
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
class ModuleMessageRegistry extends AbstractRecoverableModuleRegistry {

    private List<Tuple<Integer, Class<?>>> dtoClasses = new LinkedList<>();
    private Map<Integer, Class<?>> messageClassMap = new LinkedHashMap<>();

    ModuleMessageRegistry(ModuleLoaderContext context) {
        super(context);
    }

    @Override
    public void prepareRegister(JarFile moduleJarFile) {
        super.prepareRegister(moduleJarFile);

        for (IMessageDtoRegister dtoRegister : Registers.getDtoRegisters()) {
            IClassScanner<Tuple<Integer, Class<?>>> scanner = dtoRegister.dtoScanner();
            if (scanner != null) {
                try {
                    scanner.setJarFile(moduleJarFile);
                    dtoClasses.addAll(scanner.findClasses(getModuleClassLoader()));
                } finally {
                    scanner.setJarFile(null);
                }
            }
        }

        for (IMessageRegister messageRegister : Registers.getMessageRegisters()) {
            IClassScanner<Class<?>> scanner = messageRegister.scanner();
            if (scanner != null) {
                try {
                    scanner.setJarFile(moduleJarFile);
                    Function<Class<?>, Integer> idReader = messageRegister.idReader();
                    for (Class<?> clazz : scanner.findClasses(getModuleClassLoader())) {
                        messageClassMap.put(idReader.apply(clazz), clazz);
                    }
                } finally {
                    scanner.setJarFile(null);
                }
            }
        }
    }

    @Override
    protected void cancelPrepareRegister() {
        super.cancelPrepareRegister();
        dtoClasses.clear();
        messageClassMap.clear();
    }

    @Override
    public void register(JarFile jarFile) {
        if (!dtoClasses.isEmpty()) {
            for (Tuple<Integer, Class<?>> tuple : dtoClasses) {
                MessageDtoClassHolder.registerDtoClass(tuple.getV1(), tuple.getV2());
            }
            dtoClasses.clear();
        }

        if (!messageClassMap.isEmpty()) {
            for (Map.Entry<Integer, Class<?>> entry : messageClassMap.entrySet()) {
                if (entry.getKey() != null) {
                    MessageHolder.registerMessage(entry.getKey(), entry.getValue());
                }
            }
            messageClassMap.clear();
        }
    }

    @Override
    public void prepareUnregister() {
        MessageDtoClassHolder.prepareUnregister(getModuleClassLoader());
        MessageHolder.prepareUnregisterMessages(getModuleClassLoader());
    }

    @Override
    protected void cancelPrepareUnregister() {
        MessageDtoClassHolder.cancelPrepareUnregister();
        MessageHolder.cancelPrepareUnregister();
    }

    @Override
    public void unregister0() {
        MessageDtoClassHolder.unregister();
        MessageHolder.unregisterMessages();
    }

    @Override
    protected void cleanUp() {
        super.cleanUp();

        dtoClasses.clear();
        dtoClasses = null;
        messageClassMap.clear();
        messageClassMap = null;
    }
}
