package cn.laoshini.dk.manager;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import cn.laoshini.dk.annotation.ResourceHolder;
import cn.laoshini.dk.common.ResourcesHolder;
import cn.laoshini.dk.common.SpringContextHolder;
import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.function.ConfigurableFunctionInjector;
import cn.laoshini.dk.function.VariousWaysManager;
import cn.laoshini.dk.register.IEntityRegister;
import cn.laoshini.dk.register.IMessageDtoRegister;
import cn.laoshini.dk.register.IMessageHandlerRegister;
import cn.laoshini.dk.register.IMessageRegister;
import cn.laoshini.dk.register.Registers;
import cn.laoshini.dk.util.ClassUtil;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.ReflectHelper;
import cn.laoshini.dk.util.SpringUtils;

/**
 * 当康系统自定义注解类型处理，在Spring容器初始化时，分阶段相关初始化任务
 * Spring容器初始化顺序参见{@link org.springframework.beans.factory.BeanFactory}
 *
 * @author fagarine
 */
@Component
public class DangKangAnnotationProcessor implements ApplicationContextAware, InitializingBean {

    @Value("${dk.ext.package-prefix:[]}")
    private List<String> packagePrefix;

    private String[] packages;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextHolder.setContext(applicationContext);

        if (CollectionUtil.isEmpty(packagePrefix)) {
            packagePrefix = new ArrayList<>(1);
            packagePrefix.add(Constants.DK_PACKAGE_PREFIX);
        } else {
            boolean flag = false;
            for (String prefix : packagePrefix) {
                if (Constants.DK_PACKAGE_PREFIX.startsWith(prefix)) {
                    flag = true;
                }
            }
            if (!flag) {
                packagePrefix.add(Constants.DK_PACKAGE_PREFIX);
            }
        }
        packages = ResourcesHolder.addPackagePrefixes(packagePrefix.toArray(new String[0]));

        ClassLoader classLoader = DangKangAnnotationProcessor.class.getClassLoader();

        // 查找系统中的Spring托管对象
        findAndRegisterSpringBeans(classLoader, packages);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ClassLoader classLoader = DangKangAnnotationProcessor.class.getClassLoader();

        // 查找并注册具有多实现的功能
        VariousWaysManager.findAndRegisterVariousWaysClasses(classLoader, packages);

        // 查找并注册系统中的资源持有者
        findAndRegisterResourceHolders(classLoader, packages);

        // 加载外置模块
        ModuleManager.initModuleSystem();

        // 可配置功能对象注入，一定要保证可配置功能的注入，早于对可配置功能依赖的调用
        ConfigurableFunctionInjector.injectWaitBeans();

        // 查找并注册消息类
        findAndRegisterMessage(classLoader);

        // 查找并注册中自定义消息DTO
        findAndRegisterCustomDto(classLoader);

        // 查找并注册消息处理handler
        findAndRegisterHandler(classLoader);

        // 查找并注册表单实体类
        findAndRegisterEntityClass(classLoader);
    }

    private void findAndRegisterResourceHolders(ClassLoader cl, String[] packages) {
        List<Class<?>> classes = ClassUtil.getClassByAnnotationInPackages(cl, packages, ResourceHolder.class);
        ResourceHolderManager.batchRegister(classes);
        TypeUseManager.registerResourceHolders(ResourceHolderManager.getHoldersSnapshot());
    }

    private void findAndRegisterSpringBeans(ClassLoader classLoader, String[] basePackage) {
        List<Class<?>> classes = ReflectHelper.getAllSpringAnnotationInClasspath(classLoader, basePackage, null);
        for (Class<?> clazz : classes) {
            try {
                SpringContextHolder.getBean(clazz);
            } catch (BeansException e) {
                // 如果被Spring注解标记的类没有被托管（可能是用户没有将类路径配置到Spring扫描），加入托管
                SpringUtils.registerSpringBean(clazz);
            }
            String beanName = StringUtils.uncapitalize(clazz.getSimpleName());
            SpringBeanManager.registerBean(beanName, clazz);
            TypeUseManager.registerSpringBean(clazz.getName(), beanName);
        }
    }

    private void findAndRegisterEntityClass(ClassLoader classLoader) {
        if (Registers.getEntityRegisters().isEmpty()) {
            Registers.addEntityRegister(Registers.dangKangEntityRegister());
        }
        for (IEntityRegister entityRegister : Registers.getEntityRegisters()) {
            entityRegister.action(classLoader);
        }
    }

    private void findAndRegisterMessage(ClassLoader classLoader) {
        if (Registers.getMessageRegisters().isEmpty()) {
            Registers.addMessageRegister(Registers.dangKangCustomMessageRegister());
        }
        for (IMessageRegister messageRegister : Registers.getMessageRegisters()) {
            messageRegister.action(classLoader);
        }
    }

    private void findAndRegisterCustomDto(ClassLoader classLoader) {
        if (Registers.getDtoRegisters().isEmpty()) {
            Registers.addDtoRegister(Registers.dangKangCustomDtoRegister());
        }
        for (IMessageDtoRegister dtoRegister : Registers.getDtoRegisters()) {
            dtoRegister.action(classLoader);
        }
    }

    /**
     * 检查ClassLoader中的协议处理类，并注册
     *
     * @param classLoader 类加载器
     */
    private void findAndRegisterHandler(ClassLoader classLoader) {
        if (Registers.getHandlerRegisters().isEmpty()) {
            Registers.addHandlerRegister(Registers.dangKangMessageHandlerRegister());
        }
        for (IMessageHandlerRegister handlerRegister : Registers.getHandlerRegisters()) {
            handlerRegister.action(classLoader);
        }
    }
}
