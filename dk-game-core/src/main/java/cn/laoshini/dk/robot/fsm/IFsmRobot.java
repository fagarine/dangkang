package cn.laoshini.dk.robot.fsm;

/**
 * 有限状态机中的机器人定义
 *
 * @author fagarine
 */
public interface IFsmRobot<S extends IFsmState> {

    /**
     * 执行一次机器人逻辑（一般由定时任务调用）
     */
    void tick();

    /**
     * 获取机器人当前状态
     *
     * @return 该方法可能返回null
     */
    S currentState();

    /**
     * 变更状态
     *
     * @param newState 新状态
     */
    void changeState(S newState);

    /**
     * 机器人当前是否正处于指定状态中
     *
     * @param state 对比状态
     * @return 返回对比结果
     */
    default boolean isInState(S state) {
        return state != null && state.equals(currentState());
    }

    /**
     * 接收到外部消息，并执行相应逻辑（该方法用于其他模块与机器人对象的交互）
     *
     * @param msg 有限状态机内部消息对象
     * @param <M> 实际消息类型
     * @return 只要接收消息成功，即返回true，否则返回false
     */
    default <M> boolean onMessage(FsmMessage<M> msg) {
        return true;
    }
}
