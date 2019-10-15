package cn.laoshini.dk.robot.bt.node.composite;

import cn.laoshini.dk.constant.CompositeType;
import cn.laoshini.dk.robot.bt.node.AbstractBtCompositeNode;
import cn.laoshini.dk.robot.bt.node.IBtNode;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.LogUtil;

/**
 * Parallel Fail On All Node
 *
 * @author fagarine
 */
public class ParallelAllFail extends AbstractBtCompositeNode {

    public ParallelAllFail() {
        super(CompositeType.PARALLEL_FAIL_ON_ALL);
    }

    /**
     * 平行执行它的所有Child Node，所有Child Node返回False才向自己的Parent Node返回False，否则返回True。
     */
    @Override
    public boolean tick() {
        boolean result = false;
        int failCount = 0;
        if (CollectionUtil.isNotEmpty(children)) {
            for (IBtNode child : children) {
                try {
                    if (!child.tick()) {
                        failCount++;
                    }
                } catch (Exception e) {
                    LogUtil.error(e, "Parallel Fail On All Node执行出错, node:" + child.nodeTypeToString());
                    failCount++;
                }
            }
            if (failCount < children.size()) {
                result = true;
            }
        }
        updateResult(result);
        return result;
    }

}
