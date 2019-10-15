package cn.laoshini.dk.robot.bt;

import java.util.Collection;

import cn.laoshini.dk.robot.bt.node.BtRootNode;
import cn.laoshini.dk.robot.bt.node.IBtNodeConfig;

/**
 * 机器人行为树定义接口
 *
 * @author fagarine
 */
public interface IBehaviorTree {

    /**
     * 设置行为树配置信息
     *
     * @param config 行为树配置信息
     */
    void setTreeConfig(IBtConfig config);

    /**
     * 根据传入的行为树节点配置信息，初始化行为树
     *
     * @param nodeConfigs 节点配置信息
     */
    void initTree(Collection<? extends IBtNodeConfig> nodeConfigs);

    /**
     * 获取根节点
     *
     * @return 根节点
     */
    BtRootNode getRoot();

    /**
     * 设置行为树所属机器人id（仅当行为树不是共享树时有效）
     *
     * @param robotId 机器人唯一id
     */
    void setRobotId(long robotId);

    /**
     * 执行一次行为树逻辑
     */
    void tick();
}
