package cn.laoshini.dk.module.registry;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

import org.springframework.util.StringUtils;

import cn.laoshini.dk.manager.SpringBeanManager;
import cn.laoshini.dk.module.loader.ModuleLoaderContext;
import cn.laoshini.dk.util.ReflectHelper;
import cn.laoshini.dk.util.SpringUtils;

/**
 * 热加载类到Spring容器中，一定要先于其他注册对象执行，先加载Spring依赖类，不然后面的对象创建会出错
 *
 * @author fagarine
 */
class ModuleSpringBeanRegistry extends AbstractRecoverableModuleRegistry {

    /**
     * 记录模块中已加载到Spring容器中的实例名称
     */
    private Set<String> springBeanNames = new LinkedHashSet<>();
    private List<Class<?>> springClasses = new LinkedList<>();

    ModuleSpringBeanRegistry(ModuleLoaderContext context) {
        super(context);
    }

    @Override
    public void prepareRegister(JarFile moduleJarFile) {
        super.prepareRegister(moduleJarFile);

        List<Class<?>> classes = ReflectHelper
                .getAllSpringAnnotationInJarFile(moduleJarFile, getModuleClassLoader(), null, null);
        springClasses.addAll(classes);
    }

    @Override
    protected void cancelPrepareRegister() {
        super.cancelPrepareRegister();
        springClasses.clear();
    }

    @Override
    public void register(JarFile jarFile) {
        if (!springClasses.isEmpty()) {
            for (Class<?> clazz : springClasses) {
                SpringUtils.registerSpringBean(clazz);

                String beanName = StringUtils.uncapitalize(clazz.getSimpleName());
                springBeanNames.add(beanName);
                SpringBeanManager.registerBean(beanName, clazz);
            }
            springClasses.clear();
        }
    }

    @Override
    public void prepareUnregister() {
        SpringBeanManager.prepareUnregister(getModuleClassLoader());
    }

    @Override
    protected void cancelPrepareUnregister() {
        SpringBeanManager.cancelPrepareUnregister();
    }

    @Override
    public void unregister0() {
        SpringBeanManager.unregister();
    }

    @Override
    protected void cleanUp() {
        super.cleanUp();

        springClasses.clear();
        springClasses = null;
        springBeanNames.clear();
        springBeanNames = null;
    }
}
