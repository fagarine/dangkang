package cn.laoshini.dk.robot.bt.node;

import java.util.Collections;
import java.util.List;

import cn.laoshini.dk.constant.BtNodeType;

/**
 * 行为树Action节点
 *
 * @author fagarine
 */
public abstract class AbstractBtAction extends AbstractBtNode {
    public AbstractBtAction() {
        nodeType = BtNodeType.ACTION;
    }

    @Override
    public List<IBtNode> getChildren() {
        return Collections.emptyList();
    }

}
