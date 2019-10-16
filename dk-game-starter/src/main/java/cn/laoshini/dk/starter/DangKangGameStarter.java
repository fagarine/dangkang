package cn.laoshini.dk.starter;

import java.lang.instrument.Instrumentation;
import java.util.LinkedList;
import java.util.List;

import org.springframework.context.ApplicationContext;

import cn.laoshini.dk.agent.DangKangAgent;
import cn.laoshini.dk.common.ResourcesHolder;
import cn.laoshini.dk.common.SpringContextHolder;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.register.IGameServerRegister;
import cn.laoshini.dk.register.IMessageHandlerRegister;
import cn.laoshini.dk.register.IMessageRegister;
import cn.laoshini.dk.register.Registers;
import cn.laoshini.dk.server.AbstractGameServer;
import cn.laoshini.dk.util.StringUtil;

/**
 * @author fagarine
 */
public class DangKangGameStarter extends DangKangStarter {
    private DangKangGameStarter() {
    }

    private static DangKangGameStarter instance = new DangKangGameStarter();

    public static DangKangGameStarter get() {
        return instance;
    }

    private List<IGameServerRegister> gameServerRegisters = new LinkedList<>();

    /**
     * 设置java agent启动后的Instrumentation对象（如果用户使用了自己的agent，需要注册到当康系统）
     * 如果用户使用了当康系统提供的dk-agent作为java agent，则不需要再注册Instrumentation对象
     *
     * @param instrumentation java agent启动后的Instrumentation对象
     */
    public static void setInstrumentation(Instrumentation instrumentation) {
        DangKangAgent.setInstrumentation(instrumentation);
    }

    /**
     * 传入java agent启动后的Instrumentation对象，仅在用户使用了java agent方式启动，且使用的agent不是当康系统提供的dk-agent时，需要调用该方法
     *
     * @param instrumentation java agent启动后的Instrumentation对象
     * @return 返回当前对象，用于fluent风格编程
     */
    public DangKangGameStarter instrumentation(Instrumentation instrumentation) {
        setInstrumentation(instrumentation);
        return this;
    }

    /**
     * 用户类扫描路径前缀
     * 如果用户希望使用当康系统的功能，必须传入用户项目的包路径前缀，否则系统无法用户类的完成扫描
     *
     * @param packagePrefixes 包路径前缀
     * @return 返回当前对象，用于fluent风格编程
     */
    public DangKangGameStarter packagePrefixes(String... packagePrefixes) {
        ResourcesHolder.addPackagePrefixes(packagePrefixes);
        return this;
    }

    /**
     * 如果用户项目中使用了Spring，传入Spring配置文件路径
     *
     * @param springConfigLocations Spring配置文件路径
     * @return 返回当前对象，用于fluent风格编程
     */
    public DangKangGameStarter springConfigs(String... springConfigLocations) {
        ResourcesHolder.setSpringLocations(springConfigLocations);
        return this;
    }

    /**
     * 如果用户希望使用当康系统提供的消息池（Message Pool），传入相关的配置信息
     *
     * @param messageRegister 用户消息注册器，通过该配置，系统才能完成用户消息的扫描和注册
     * @return 返回当前对象，用于fluent风格编程
     */
    public DangKangGameStarter message(IMessageRegister messageRegister) {
        Registers.addMessageRegister(messageRegister);
        return this;
    }

    /**
     * 如果用户希望使用当康系统提供的消息处理Handler管理工具，传入相关的配置信息
     * <p>
     * 所谓消息处理Handler，是指消息到达后，负责处理对应业务的类
     * </p>
     *
     * @param handlerRegister 用户消息处理Handler注册器，通过该配置，系统才能完成用户消息处理Handler的扫描和注册
     * @return 返回当前对象，用于fluent风格编程
     */
    public DangKangGameStarter messageHandler(IMessageHandlerRegister handlerRegister) {
        Registers.addHandlerRegister(handlerRegister);
        return this;
    }

    /**
     * 如果用户希望使用当康系统内置的游戏服务器相关功能，使用该方法传入相关的配置信息
     *
     * @param gameServerRegister 游戏服务器注册信息
     * @param <S> 客户端连接会话类型
     * @param <M> 客户端消息到达后的类型
     * @return 返回当前对象，用于fluent风格编程
     */
    public <S, M> DangKangGameStarter gameServer(IGameServerRegister<S, M> gameServerRegister) {
        if (StringUtil.isEmptyString(gameServerRegister.gameName())) {
            throw new IllegalArgumentException("无效的游戏名称:" + gameServerRegister.gameName());
        }

        for (IGameServerRegister register : gameServerRegisters) {
            if (gameServerRegister.gameName().equals(register.gameName())) {
                throw new BusinessException("game.name.duplicate", "游戏名称重复");
            }
        }

        gameServerRegisters.add(gameServerRegister);
        return this;
    }

    /**
     * 启动当康系统，启动完成后会完成相关功能的扫描、注册、初始化等操作
     * <p>
     * 注意：
     * 由于当康系统使用了Spring，无论用户原来的项目中是否使用了Spring，都会启动Spring容器
     * </p>
     *
     * @return 返回Spring上下文对象
     */
    public ApplicationContext start() {
        startBySpringXmlFile(ResourcesHolder.getPackagePrefixesAsArray(), ResourcesHolder.getSpringLocations());

        AbstractGameServer gameServer;
        for (IGameServerRegister gameServerRegister : gameServerRegisters) {
            gameServer = gameServerRegister.startServer();
            GameServers.putServer(gameServer);
        }
        return SpringContextHolder.getContext();
    }

}
