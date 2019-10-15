package cn.laoshini.dk.robot.bt.node;

import java.util.Collections;
import java.util.List;

import cn.laoshini.dk.constant.BtNodeType;

/**
 * 行为树节点配置信息基础功能定义
 *
 * @author fagarine
 */
public interface IBtNodeConfig {

    /**
     * 获取节点的id
     *
     * @return 节点id
     */
    int getNodeId();

    /**
     * 获取节点的子节点配置，非叶节点配置需要实现该方法
     *
     * @return 子节点
     */
    default List<Integer> getChildren() {
        return Collections.emptyList();
    }

    /**
     * 获取节点类型
     *
     * @return 节点类型
     */
    int getNodeType();

    /**
     * 如果节点有细分类型，获取具体的细分类型
     *
     * @return 节点子类型
     */
    int getSubType();

    /**
     * 获取节点的详细配置 参数
     *
     * @return 节点详细配置
     */
    String getParam();

    /**
     * 是否是Root节点
     *
     * @return 是否是Root节点
     */
    default boolean isRootNode() {
        return getNodeType() == BtNodeType.ROOT.getCode();
    }

    /**
     * 是否是复合类节点
     *
     * @return 是否是复合类节点
     */
    default boolean isCompositeNode() {
        return getNodeType() == BtNodeType.COMPOSITE.getCode();
    }

    /**
     * 是否是装饰类节点
     *
     * @return 是否是装饰类节点
     */
    default boolean isDecoratorNode() {
        return getNodeType() == BtNodeType.DECORATOR.getCode();
    }

    /**
     * 是否是行为树的叶节点（条件节点或行为节点）
     *
     * @return 是否是叶节点
     */
    default boolean isLeafNode() {
        int nodeType = getNodeType();
        return nodeType == BtNodeType.ACTION.getCode() || nodeType == BtNodeType.CONDITION.getCode();
    }

    /**
     * 是否是条件节点
     *
     * @return 是否是条件节点
     */
    default boolean isConditionNode() {
        return getNodeType() == BtNodeType.CONDITION.getCode();
    }

    /**
     * 是否是行为节点
     *
     * @return 是否是行为节点
     */
    default boolean isActionNode() {
        return getNodeType() == BtNodeType.ACTION.getCode();
    }
}
