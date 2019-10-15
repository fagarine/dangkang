package cn.laoshini.dk.transform.injection;

/**
 * @author fagarine
 */
public class ConfigurableFunctionInjectorProxy implements IConfigurableFunctionInjector {
    private ConfigurableFunctionInjectorProxy() {
    }

    private static ConfigurableFunctionInjectorProxy instance = new ConfigurableFunctionInjectorProxy();

    public static ConfigurableFunctionInjectorProxy getInstance() {
        return instance;
    }

    private IConfigurableFunctionInjector delegate;

    public static void setDelegate(IConfigurableFunctionInjector delegate) {
        instance.delegate = delegate;
    }

    @Override
    public void injectField(Object bean, String fieldName) {
        if (delegate == null) {
            // 功能注入实现类对象还未填入，缓存依赖注入的对象
            add(bean);
        } else {
            delegate.injectField(bean, fieldName);
        }
    }
}
