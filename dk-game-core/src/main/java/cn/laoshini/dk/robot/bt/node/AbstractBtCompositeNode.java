package cn.laoshini.dk.robot.bt.node;

import cn.laoshini.dk.constant.BtNodeType;
import cn.laoshini.dk.constant.CompositeType;

/**
 * 行为树复合类型节点
 *
 * @author fagarine
 */
public abstract class AbstractBtCompositeNode extends AbstractBtNode {
    protected CompositeType compositeType;

    public AbstractBtCompositeNode() {
        nodeType = BtNodeType.COMPOSITE;
    }

    public AbstractBtCompositeNode(CompositeType compositeType) {
        nodeType = BtNodeType.COMPOSITE;
        this.compositeType = compositeType;
        // 默认为共享节点
        setShared(true);
    }
}
