package cn.laoshini.dk.module.registry;

import java.util.jar.JarFile;

import cn.laoshini.dk.module.AbstractModuleRegistry;
import cn.laoshini.dk.module.loader.ModuleLoaderContext;

/**
 * @author fagarine
 */
public abstract class AbstractRecoverableModuleRegistry extends AbstractModuleRegistry {

    protected Phase phase = Phase.UNREGISTERED;

    public AbstractRecoverableModuleRegistry() {
    }

    public AbstractRecoverableModuleRegistry(ModuleLoaderContext context) {
        super(context);
    }

    @Override
    public void prepareRegister(JarFile moduleJarFile) {
        toPrepareRegister();
    }

    protected void cancelPrepareRegister() {
    }

    protected void cancelPrepareUnregister() {

    }

    @Override
    public void rollback() {
        switch (phase) {
            case UNREGISTERED:
                // 尚未注册功能的，不做处理
                break;
            case PREPARE_REGISTER:
                cancelPrepareRegister();
                toUnregistered();
                break;
            case REGISTERED:
                // 由于在注册前有准备操作，如果准备阶段未发现问题，已注册完成的，暂不做回滚处理
                break;
            case PREPARE_UNREGISTER:
                cancelPrepareUnregister();
                toRegistered();
                break;
            default:
                break;
        }
    }

    public boolean isRegistered() {
        return phase == Phase.REGISTERED;
    }

    protected void toPrepareRegister() {
        phase = Phase.PREPARE_REGISTER;
    }

    protected void toRegistered() {
        phase = Phase.REGISTERED;
    }

    protected void toUnregistered() {
        phase = Phase.UNREGISTERED;
    }

    protected void toPrepareUnregister() {
        phase = Phase.PREPARE_UNREGISTER;
    }
}
