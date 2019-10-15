package cn.laoshini.dk.robot.bt.node.composite;

import cn.laoshini.dk.constant.CompositeType;
import cn.laoshini.dk.robot.bt.node.AbstractBtCompositeNode;
import cn.laoshini.dk.robot.bt.node.IBtNode;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.LogUtil;

/**
 * Parallel Sequence Node
 *
 * @author fagarine
 */
public class ParallelSequence extends AbstractBtCompositeNode {

    public ParallelSequence() {
        super(CompositeType.PARALLEL_SEQUENCE);
    }

    /**
     * 平行执行它的所有Child Node，一个Child Node返回True则向自己的Parent Node返回True，全False才返回False。
     */
    @Override
    public boolean tick() {
        boolean result = false;
        if (CollectionUtil.isNotEmpty(children)) {
            for (IBtNode child : children) {
                try {
                    if (child.tick()) {
                        result = true;
                    }
                } catch (Exception e) {
                    LogUtil.debug("Parallel Sequence Node执行出错, node:" + child.nodeTypeToString());
                    result = false;
                }
            }
        }
        updateResult(result);
        return result;
    }

}
