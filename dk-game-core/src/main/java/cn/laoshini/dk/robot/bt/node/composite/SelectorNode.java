package cn.laoshini.dk.robot.bt.node.composite;

import cn.laoshini.dk.constant.CompositeType;
import cn.laoshini.dk.robot.bt.node.AbstractBtCompositeNode;
import cn.laoshini.dk.robot.bt.node.IBtNode;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.LogUtil;

/**
 * Selector Node
 *
 * @author fagarine
 */
public class SelectorNode extends AbstractBtCompositeNode {

    public SelectorNode() {
        super(CompositeType.SELECTOR);
    }

    /**
     * 选择执行，当执行本类型Node时，它将从begin到end迭代执行自己的Child Node: 如遇到一个Child Node执行后返回True，将停止迭代，本Node向自己的Parent
     * Node也返回True；否则所有Child Node都返回False， 那本Node向自己的Parent Node返回False。
     */
    @Override
    public boolean tick() {
        if (CollectionUtil.isNotEmpty(children)) {
            for (IBtNode child : children) {
                try {
                    if (child.tick()) {
                        succeed();
                        return true;
                    }
                } catch (Exception e) {
                    LogUtil.debug("Selector Node执行出错, node:" + child.nodeTypeToString());
                }
            }
        }
        fail();
        return false;
    }
}
