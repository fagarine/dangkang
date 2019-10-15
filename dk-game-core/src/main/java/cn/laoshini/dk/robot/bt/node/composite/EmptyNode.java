package cn.laoshini.dk.robot.bt.node.composite;

import java.util.Collections;
import java.util.List;

import cn.laoshini.dk.robot.bt.node.AbstractBtCompositeNode;
import cn.laoshini.dk.robot.bt.node.IBtNode;

/**
 * 空节点
 *
 * @author fagarine
 */
public class EmptyNode extends AbstractBtCompositeNode {

    @Override
    public List<IBtNode> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public boolean tick() {
        return true;
    }

}
