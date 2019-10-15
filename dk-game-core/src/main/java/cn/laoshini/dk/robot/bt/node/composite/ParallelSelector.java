package cn.laoshini.dk.robot.bt.node.composite;

import cn.laoshini.dk.constant.CompositeType;
import cn.laoshini.dk.robot.bt.node.AbstractBtCompositeNode;
import cn.laoshini.dk.robot.bt.node.IBtNode;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.LogUtil;

/**
 * Parallel Selector Node
 *
 * @author fagarine
 */
public class ParallelSelector extends AbstractBtCompositeNode {

    public ParallelSelector() {
        super(CompositeType.PARALLEL_SELECTOR);
    }

    /**
     * 平行执行它的所有Child Node，一个Child Node返回False则向自己的Parent Node返回False，全True才返回True。
     */
    @Override
    public boolean tick() {
        boolean result = true;
        if (CollectionUtil.isNotEmpty(children)) {
            for (IBtNode child : children) {
                try {
                    if (!child.tick()) {
                        result = false;
                    }
                } catch (Exception e) {
                    LogUtil.debug("Parallel Selector Node执行出错, node:" + child.nodeTypeToString());
                    result = false;
                }
            }
        }
        updateResult(result);
        return result;
    }

}
