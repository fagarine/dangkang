package cn.laoshini.dk.robot.bt.factory.impl;

import cn.laoshini.dk.robot.bt.factory.AbstractBtConditionNodeFactory;
import cn.laoshini.dk.robot.bt.node.AbstractBtCondition;
import cn.laoshini.dk.util.LogUtil;

/**
 * 条件类节点工厂默认实现
 *
 * @author fagarine
 */
public class BtConditionNodeFactoryImpl extends AbstractBtConditionNodeFactory {
    @Override
    public AbstractBtCondition createConditionNode(int conditionType, boolean shared) {
        Class<? extends AbstractBtCondition> clazz = conditionNodeMap.get(conditionType);
        if (null == clazz) {
            LogUtil.error("condition节点类型未注册，创建节点失败, conditionType:" + conditionType);
            return EMPTY_NODE;
        }

        AbstractBtCondition node;
        try {
            node = clazz.newInstance();
            node.setShared(shared);
        } catch (InstantiationException | IllegalAccessException e) {
            LogUtil.error(e, "condition节点初始化失败, conditionType:" + conditionType + ", class:" + clazz);
            node = EMPTY_NODE;
        }

        return node;
    }

    private static final AbstractBtCondition EMPTY_NODE = new EmptyConditionNode();

    private static class EmptyConditionNode extends AbstractBtCondition {

        @Override
        public boolean tick() {
            throw new IllegalArgumentException("错误的condition节点实现");
        }

        @Override
        public boolean checkCondition() {
            return false;
        }

        @Override
        public boolean checkCondition(Object... params) {
            return false;
        }

        @Override
        public boolean isValid() {
            return false;
        }

    }
}
