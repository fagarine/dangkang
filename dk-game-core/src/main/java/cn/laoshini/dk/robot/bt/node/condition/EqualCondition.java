package cn.laoshini.dk.robot.bt.node.condition;

import cn.laoshini.dk.robot.bt.node.BtConditionNode;

/**
 * Equals条件判断
 *
 * @author fagarine
 */
public class EqualCondition extends BtConditionNode {

    @Override
    public boolean checkCondition(Object... params) {
        if (null == params || params.length < 2) {
            return false;
        }

        if (null == params[0]) {
            return null == params[1];
        }

        return params[0].equals(params[1]);
    }
}
