package cn.laoshini.dk.robot.fsm;

/**
 * 有限状态机中的机器人状态接口
 *
 * @author fagarine
 */
public interface IFsmState<R extends IFsmRobot> {

    /**
     * 机器人进入当前状态
     *
     * @param robot 机器人对象
     */
    void enter(R robot);

    /**
     * 机器人在当前状态内刷新（更新）数据
     *
     * @param robot 机器人对象
     */
    void refresh(R robot);

    /**
     * 机器人退出当前状态
     *
     * @param robot 机器人对象
     */
    void exit(R robot);
}
