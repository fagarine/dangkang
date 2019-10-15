package cn.laoshini.dk.starter;

import java.lang.instrument.Instrumentation;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import cn.laoshini.dk.agent.DangKangAgent;
import cn.laoshini.dk.common.ResourcesHolder;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.LogUtil;

/**
 * 当康系统基础版（不带游戏功能）启动类，如果用户想要使用当康系统的功能，需通过该入口启动当康系统
 *
 * @author fagarine
 */
public class DangKangStarter {

    static {
        DangKangAgent.checkAndLoadAgent();
    }

    private static final String APPLICATION_LOCATION = "applicationContext-dk-default.xml";

    /**
     * 开启当康系统基础支持，如果需要调用该盖房，请将调用语句放在项目启动最前，否则可能导致某些功能不可用（最好放在main方法的第一行）
     *
     * @return 返回当前类，用于fluent风格编程
     */
    public static Class<DangKangStarter> engineStart() {
        LogUtil.start("当康系统基础支持启动");
        if (DangKangAgent.inst == null) {
            LogUtil.error(
                    "当康系统agent加载失败，请保证本方法处于系统启动最前，且当康系统依赖项目dk-agent已加入项目启动classpath，或手动将dk-agent的jar包作为javaagent加入启动项");
        }
        return DangKangStarter.class;
    }

    /**
     * 设置java agent启动后的Instrumentation对象（如果用户使用了自己的agent，需要注册到当康系统）
     * 如果用户使用了当康系统提供的dk-agent作为java agent，则不需要再注册Instrumentation对象
     *
     * @param instrumentation java agent启动后的Instrumentation对象
     */
    static void setInstrumentation(Instrumentation instrumentation) {
        DangKangAgent.setInstrumentation(instrumentation);
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
        int size = CollectionUtil.isEmpty(springConfigLocations) ? 0 : springConfigLocations.length;
        String[] configs = new String[size + 1];
        configs[0] = APPLICATION_LOCATION;
        if (size > 0) {
            System.arraycopy(springConfigLocations, 0, configs, 1, size);
        }

        ResourcesHolder.addPackagePrefixes(packagePrefixes);
        ApplicationContext context = new GenericXmlApplicationContext(configs);
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
