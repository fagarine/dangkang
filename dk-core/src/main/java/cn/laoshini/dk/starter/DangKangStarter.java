package cn.laoshini.dk.starter;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.support.SpringFactoriesLoader;

import cn.laoshini.dk.agent.DangKangAgent;
import cn.laoshini.dk.common.DkApplicationContext;
import cn.laoshini.dk.common.IDkApplicationContext;
import cn.laoshini.dk.common.ResourcesHolder;
import cn.laoshini.dk.util.CollectionUtil;

/**
 * 当康系统基础版（不带游戏功能）启动类，如果用户想要使用当康系统的功能，需通过该入口启动当康系统
 *
 * @author fagarine
 */
public class DangKangStarter {

    static {
        DangKangAgent.checkAndLoadAgent();
    }

    /**
     * 非Spring项目启动入口（用户项目为非Spring项目）
     *
     * @param packagePrefixes 项目包路径前缀，当康系统通过扫描这些包路径下的类，完成相关功能的初始化和注入
     */
    public static void startByNonSpring(String[] packagePrefixes) {
        startBySpringXmlFile(packagePrefixes);
    }

    /**
     * Spring项目启动
     *
     * @param packagePrefixes 项目包路径前缀
     * @param springConfigLocations Spring文件路径（无论是classpath相对路径还是文件路径都可以）
     * @return 返回Spring上下文对象
     */
    public static ApplicationContext startBySpringXmlFile(String[] packagePrefixes, String... springConfigLocations) {
        IDkApplicationContext context;
        ClassLoader classLoader = DangKangStarter.class.getClassLoader();
        List<String> contextClasses = SpringFactoriesLoader.loadFactoryNames(IDkApplicationContext.class, classLoader);
        ResourcesHolder.addPackagePrefixes(packagePrefixes);
        if (CollectionUtil.isNotEmpty(contextClasses) && contextClasses.size() == 1) {
            context = SpringFactoriesLoader.loadFactories(IDkApplicationContext.class, classLoader).get(0);
        } else {
            context = new DkApplicationContext();
        }

        int size = CollectionUtil.isEmpty(springConfigLocations) ? 0 : springConfigLocations.length;
        String[] dependent = context.dependentSpringConfigs();
        int dependentSize = CollectionUtil.isEmpty(dependent) ? 0 : dependent.length;
        String[] configs = new String[size + dependentSize];
        if (dependentSize > 0) {
            System.arraycopy(dependent, 0, configs, 0, dependent.length);
        }
        if (size > 0) {
            System.arraycopy(springConfigLocations, 0, configs, dependentSize, size);
        }

        context.configLocations(configs);
        context.refresh();
        springCompletedProcess();
        return context;
    }

    /**
     * Spring容器启动完成后的工作，例如外部功能初始化等操作
     */
    private static void springCompletedProcess() {
        ExternalLastInitializer.afterInitializationProcess();
    }
}
