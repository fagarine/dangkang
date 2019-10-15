package cn.laoshini.dk.robot.bt.factory.impl;

import cn.laoshini.dk.constant.CompositeType;
import cn.laoshini.dk.robot.bt.factory.IBtNodeFactory;
import cn.laoshini.dk.robot.bt.node.AbstractBtCompositeNode;
import cn.laoshini.dk.robot.bt.node.composite.EmptyNode;
import cn.laoshini.dk.robot.bt.node.composite.ParallelAllFail;
import cn.laoshini.dk.robot.bt.node.composite.ParallelAllSucceed;
import cn.laoshini.dk.robot.bt.node.composite.ParallelHybirdFail;
import cn.laoshini.dk.robot.bt.node.composite.ParallelHybirdSucceed;
import cn.laoshini.dk.robot.bt.node.composite.ParallelSelector;
import cn.laoshini.dk.robot.bt.node.composite.ParallelSequence;
import cn.laoshini.dk.robot.bt.node.composite.SelectorNode;
import cn.laoshini.dk.robot.bt.node.composite.SequenceNode;

/**
 * 行为树复合节点类型工厂类
 *
 * @author fagarine
 */
public class BtCompositeNodeFactory implements IBtNodeFactory {

    @Override
    public AbstractBtCompositeNode createBtNode(int compositeType, boolean shared) {
        CompositeType type = CompositeType.valueOf(compositeType);
        AbstractBtCompositeNode node = null;
        switch (type) {
            case SELECTOR:
                node = new SelectorNode();
                break;

            case SEQUENCE:
                node = new SequenceNode();
                break;

            case PARALLEL_SELECTOR:
                node = new ParallelSelector();
                break;

            case PARALLEL_SEQUENCE:
                node = new ParallelSequence();
                break;

            case PARALLEL_FAIL_ON_ALL:
                node = new ParallelAllFail();
                break;

            case PARALLEL_SUCC_ON_ALL:
                node = new ParallelAllSucceed();
                break;

            case PARALLEL_HYBIRD_FAIL:
                node = new ParallelHybirdFail();
                break;

            case PARALLEL_HYBIRD_SUCC:
                node = new ParallelHybirdSucceed();
                break;

            case NONE:
            default:
                node = new EmptyNode();
                break;
        }
        node.setShared(shared);
        return node;
    }
}
