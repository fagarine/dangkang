package cn.laoshini.dk.robot.bt.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.laoshini.dk.robot.bt.node.AbstractBtCondition;
import cn.laoshini.dk.robot.bt.node.IBtNode;

/**
 * 行为树条件控制类节点工厂
 *
 * @author fagarine
 */
public abstract class AbstractBtConditionNodeFactory implements IBtNodeFactory {

    protected Map<Integer, Class<? extends AbstractBtCondition>> conditionNodeMap = new ConcurrentHashMap<>();

    @Override
    public IBtNode createBtNode(int conditionType, boolean shared) {
        return createConditionNode(conditionType, shared);
    }

    /**
     * 创建条件节点
     *
     * @param conditionType
     * @param shared
     * @return
     */
    public abstract AbstractBtCondition createConditionNode(int conditionType, boolean shared);

    /**
     * 注册具体的条件节点类
     *
     * @param conditionType
     * @param clazz
     */
    public void registerConditionNode(int conditionType, Class<? extends AbstractBtCondition> clazz) {
        if (null != clazz) {
            conditionNodeMap.put(conditionType, clazz);
        }
    }
}
