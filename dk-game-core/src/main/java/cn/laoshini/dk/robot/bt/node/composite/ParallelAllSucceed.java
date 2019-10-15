package cn.laoshini.dk.robot.bt.node.composite;

import cn.laoshini.dk.constant.CompositeType;
import cn.laoshini.dk.robot.bt.node.AbstractBtCompositeNode;
import cn.laoshini.dk.robot.bt.node.IBtNode;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.LogUtil;

/**
 * Parallel Succeed On All Node
 *
 * @author fagarine
 */
public class ParallelAllSucceed extends AbstractBtCompositeNode {

    public ParallelAllSucceed() {
        super(CompositeType.PARALLEL_SUCC_ON_ALL);
    }

    /**
     * 平行执行它的所有Child Node，所有Child Node返回True才向自己的Parent Node返回True，否则返回False。
     */
    @Override
    public boolean tick() {
        boolean result = true;
        int succeedCount = 0;
        if (CollectionUtil.isNotEmpty(children)) {
            for (IBtNode child : children) {
                try {
                    if (child.tick()) {
                        succeedCount++;
                    }
                } catch (Exception e) {
                    LogUtil.debug("Parallel Succeed On All Node执行出错, node:" + child.nodeTypeToString());
                }
            }
            if (succeedCount < children.size()) {
                result = false;
            }
        }
        updateResult(result);
        return result;
    }

}
