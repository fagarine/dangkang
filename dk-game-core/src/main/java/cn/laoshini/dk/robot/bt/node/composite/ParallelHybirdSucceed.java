package cn.laoshini.dk.robot.bt.node.composite;

import cn.laoshini.dk.constant.CompositeType;
import cn.laoshini.dk.robot.bt.node.AbstractBtCompositeNode;
import cn.laoshini.dk.robot.bt.node.IBtNode;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.LogUtil;

/**
 * Parallel Hybird Succeed Node
 *
 * @author fagarine
 */
public class ParallelHybirdSucceed extends AbstractBtCompositeNode {
    private int succeedCount;

    public ParallelHybirdSucceed() {
        super(CompositeType.PARALLEL_HYBIRD_SUCC);
    }

    /**
     * 平行执行它的所有Child Node，指定数量的Child Node返回True后才向自己的Parent Node返回True。
     */
    @Override
    public boolean tick() {
        boolean result = true;
        int count = 0;
        if (CollectionUtil.isNotEmpty(children)) {
            for (IBtNode child : children) {
                try {
                    if (child.tick()) {
                        count++;
                    }
                } catch (Exception e) {
                    LogUtil.debug("Parallel Hybird Succeed Node执行出错, node:" + child.nodeTypeToString());
                }
            }
            if (count < succeedCount) {
                result = false;
            }
        }
        updateResult(result);
        return result;
    }

}
