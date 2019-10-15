package cn.laoshini.dk.robot.bt.node;

import java.util.List;

import cn.laoshini.dk.constant.BtNodeType;
import cn.laoshini.dk.constant.CompositeType;
import cn.laoshini.dk.constant.NodeState;

/**
 * 行为树节点接口定义
 *
 * @author fagarine
 */
public interface IBtNode {

    /**
     * 获取节点类型
     *
     * @return 节点类型
     */
    BtNodeType getNodeType();

    /**
     * 获取节点的复合类型，仅当节点为复合类型（调用方法{@link #getNodeType()}返回 {@link BtNodeType#COMPOSITE}）时，返回具体的CompositeNode类型
     *
     * @return 如果节点不是复合类型，将返回{@link CompositeType#NONE}
     */
    CompositeType getCompositeType();

    /**
     * 设置节点的父节点对象
     *
     * @param parent 父节点
     */
    void setParent(IBtNode parent);

    /**
     * 返回节点的父节点
     *
     * @return 如果该节点为根节点，将返回null
     */
    IBtNode getParent();

    /**
     * 设置机器人角色id（如果是共享树的节点，可以不设置，在调用 {@link #tick(Object...)} 方法时传入即可）
     *
     * @param roleId 机器人角色id
     */
    void setRobotRoleId(long roleId);

    /**
     * 获取节点的所有子节点
     *
     * @return 所有子节点
     */
    List<IBtNode> getChildren();

    /**
     * 给节点添加子节点
     *
     * @param node 子节点
     */
    void addNode(IBtNode node);

    /**
     * 删除子节点
     *
     * @param node 子节点
     */
    void removeNode(IBtNode node);

    /**
     * 是否包指定的子节点
     *
     * @param node 节点
     * @return 返回是否包含
     */
    boolean hasNode(IBtNode node);

    /**
     * 开始执行节点逻辑
     *
     * @return 返回执行结果
     */
    boolean tick();

    /**
     * 开始执行节点逻辑，用于公用行为树节点，传入参数为具体的机器人对象的相关参数
     *
     * @param params 逻辑执行所需参数
     * @return 返回执行结果
     */
    boolean tick(Object... params);

    /**
     * 获取当前状态
     *
     * @return 当前状态
     */
    NodeState currentState();

    /**
     * 将状态记为成功
     */
    void succeed();

    /**
     * 将状态记为失败
     */
    void fail();

    /**
     * 设置节点配置信息
     *
     * @param config 节点配置信息
     */
    void setConfig(IBtNodeConfig config);

    /**
     * 获取节点配置信息
     *
     * @return 节点配置信息
     */
    IBtNodeConfig getConfig();

    /**
     * 获取节点类型信息，返回一个描述节点类型信息的字符串
     *
     * @return 节点类型信息
     */
    String nodeTypeToString();

    /**
     * 是否有效节点，默认节点对象生成后为有效节点，一些特殊的节点对象（如默认空节点对象）可以重写该方法
     *
     * @return 是否有效节点
     */
    default boolean isValid() {
        return true;
    }
}
