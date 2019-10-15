package cn.laoshini.dk.robot.bt.node.condition;

import cn.laoshini.dk.robot.bt.node.BtConditionNode;

/**
 * 返回false的条件节点
 *
 * @author fagarine
 */
public class FalseCondition extends BtConditionNode {
    @Override
    public boolean checkCondition(Object... params) {
        return false;
    }
}
