package cn.laoshini.dk.robot.bt.node;

/**
 * 条件节点类
 *
 * @author fagarine
 */
public class BtConditionNode extends AbstractBtCondition {

    @Override
    public boolean tick() {
        return checkCondition();
    }

    @Override
    public boolean checkCondition() {
        return false;
    }

    @Override
    public boolean checkCondition(Object... params) {
        return false;
    }
}
