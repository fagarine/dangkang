package cn.laoshini.dk.register;

/**
 * 游戏服启动成功后的处理逻辑接口
 *
 * @author fagarine
 */
@FunctionalInterface
public interface IGameServerStartedHandler {

    /**
     * 执行逻辑
     */
    void action();

}
