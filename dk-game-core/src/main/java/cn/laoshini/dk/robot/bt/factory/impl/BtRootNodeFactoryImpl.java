package cn.laoshini.dk.robot.bt.factory.impl;

import cn.laoshini.dk.robot.bt.factory.IBtRootNodeFactory;
import cn.laoshini.dk.robot.bt.node.BtRootNode;

/**
 * Root节点工厂的简单实现
 *
 * @author fagarine
 */
public class BtRootNodeFactoryImpl implements IBtRootNodeFactory {

    @Override
    public BtRootNode createRootNode(boolean shared) {
        return new BtRootNode();
    }
}
