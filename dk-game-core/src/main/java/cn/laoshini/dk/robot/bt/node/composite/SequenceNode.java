package cn.laoshini.dk.robot.bt.node.composite;

import cn.laoshini.dk.constant.CompositeType;
import cn.laoshini.dk.robot.bt.node.AbstractBtCompositeNode;
import cn.laoshini.dk.robot.bt.node.IBtNode;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.LogUtil;

/**
 * Sequence Node
 *
 * @author fagarine
 */
public class SequenceNode extends AbstractBtCompositeNode {

    public SequenceNode() {
        super(CompositeType.SEQUENCE);
    }

    /**
     * 顺序执行，当执行本类型Node时，它将从begin到end迭代执行自己的Child Node: 如遇到一个Child Node执行后返回False，则停止迭代本Node向自己的Parent
     * Node也返回False；否则所有Child Node都返回True， 那本Node向自己的Parent Node返回True。
     */
    @Override
    public boolean tick() {
        if (CollectionUtil.isNotEmpty(children)) {
            for (IBtNode child : children) {
                try {
                    if (!child.tick()) {
                        fail();
                        return false;
                    }
                } catch (Exception e) {
                    LogUtil.debug("Sequence Node执行出错, node:" + child.nodeTypeToString());
                    fail();
                    return false;
                }
            }
        }
        return true;
    }
}
