package cn.laoshini.dk.robot.bt.factory;

import cn.laoshini.dk.robot.bt.node.BtRootNode;

/**
 * root节点工厂基础实现类
 *
 * @author fagarine
 */
public interface IBtRootNodeFactory extends IBtNodeFactory {

    @Override
    default BtRootNode createBtNode(int nodeType, boolean shared) {
        return createRootNode(shared);
    }

    /**
     * 创建Root节点
     *
     * @param shared
     * @return
     */
    BtRootNode createRootNode(boolean shared);

}
