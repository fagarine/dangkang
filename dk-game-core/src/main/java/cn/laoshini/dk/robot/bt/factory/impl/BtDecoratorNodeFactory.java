package cn.laoshini.dk.robot.bt.factory.impl;

import cn.laoshini.dk.robot.bt.factory.IBtNodeFactory;
import cn.laoshini.dk.robot.bt.node.IBtNode;

/**
 * 装饰类节点工厂
 *
 * @author fagarine
 */
public class BtDecoratorNodeFactory implements IBtNodeFactory {

    @Override
    public IBtNode createBtNode(int decoratorType, boolean shared) {
        // 暂不实现
        return null;
    }
}
