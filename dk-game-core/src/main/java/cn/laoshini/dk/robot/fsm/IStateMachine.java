package cn.laoshini.dk.robot.fsm;

/**
 * 状态机功能定义
 *
 * @author fagarine
 */
public interface IStateMachine {

    /**
     * 数据初始化操作
     */
    default void initialize() {
    }

    /**
     * 状态机执行一次任务逻辑
     */
    void tick();

}
