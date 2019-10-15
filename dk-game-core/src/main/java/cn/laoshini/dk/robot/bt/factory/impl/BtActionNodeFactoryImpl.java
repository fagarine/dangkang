package cn.laoshini.dk.robot.bt.factory.impl;

import cn.laoshini.dk.robot.bt.factory.AbstractBtActionNodeFactory;
import cn.laoshini.dk.robot.bt.node.AbstractBtAction;
import cn.laoshini.dk.util.LogUtil;

/**
 * 行为树action节点工厂默认实现
 *
 * @author fagarine
 */
public class BtActionNodeFactoryImpl extends AbstractBtActionNodeFactory {
    @Override
    public AbstractBtAction createActionNode(int actionNodeType, boolean shared) {
        Class<? extends AbstractBtAction> clazz = actionNodeMap.get(actionNodeType);
        if (null == clazz) {
            LogUtil.error("action节点类型未注册，创建节点失败, actionNodeType:" + actionNodeType);
            return EMPTY_NODE;
        }
        AbstractBtAction node = null;
        try {
            node = clazz.newInstance();
            node.setShared(shared);
        } catch (InstantiationException | IllegalAccessException e) {
            LogUtil.error(e, "action节点初始化失败, actionNodeType:" + actionNodeType + ", class:" + clazz);
            node = EMPTY_NODE;
        }

        return node;
    }

    private static final AbstractBtAction EMPTY_NODE = new EmptyActionNode();

    private static class EmptyActionNode extends AbstractBtAction {

        @Override
        public boolean tick() {
            throw new IllegalArgumentException("错误的action节点实现");
        }

        @Override
        public boolean isValid() {
            return false;
        }

    }
}
