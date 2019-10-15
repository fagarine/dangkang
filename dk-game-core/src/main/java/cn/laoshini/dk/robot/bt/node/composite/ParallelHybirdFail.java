package cn.laoshini.dk.robot.bt.node.composite;

import cn.laoshini.dk.constant.CompositeType;
import cn.laoshini.dk.robot.bt.node.AbstractBtCompositeNode;
import cn.laoshini.dk.robot.bt.node.IBtNode;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.LogUtil;

/**
 * Parallel Hybird Fail Node
 *
 * @author fagarine
 */
public class ParallelHybirdFail extends AbstractBtCompositeNode {
    private int failCount;// 判断向父节点返回false的子节点执行结果为false的次数

    public ParallelHybirdFail() {
        super(CompositeType.PARALLEL_HYBIRD_FAIL);
    }

    public ParallelHybirdFail(int failCount) {
        super(CompositeType.PARALLEL_HYBIRD_FAIL);
        this.failCount = failCount;
    }

    /**
     * 平行执行它的所有Child Node，指定数量的Child Node返回False后才向自己的Parent Node返回False。
     */
    @Override
    public boolean tick() {
        boolean result = false;
        int count = 0;
        if (CollectionUtil.isNotEmpty(children)) {
            for (IBtNode child : children) {
                try {
                    if (!child.tick()) {
                        count++;
                    }
                } catch (Exception e) {
                    LogUtil.debug("Parallel Hybird Fail Node执行出错, node:" + child.nodeTypeToString());
                    count++;
                }
            }
            if (count < failCount) {
                result = true;
            }
        }
        updateResult(result);
        return result;
    }

}
