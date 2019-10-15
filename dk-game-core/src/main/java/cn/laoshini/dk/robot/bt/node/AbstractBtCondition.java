package cn.laoshini.dk.robot.bt.node;

import java.util.Collections;
import java.util.List;

import cn.laoshini.dk.constant.BtNodeType;

/**
 * 行为树条件节点抽象类
 *
 * @author fagarine
 */
public abstract class AbstractBtCondition extends AbstractBtNode {

    public AbstractBtCondition() {
        nodeType = BtNodeType.CONDITION;
    }

    @Override
    public List<IBtNode> getChildren() {
        return Collections.emptyList();
    }

    /**
     * 不额外传入参数的条件检查，将使用节点对象自己的参数进行检查
     *
     * @return
     */
    public abstract boolean checkCondition();

    /**
     * 带参数条件检查，用于公用行为树节点，传入参数为具体的智能体对象（机器人）的相关参数
     *
     * @param params
     * @return
     */
    public abstract boolean checkCondition(Object... params);
}
