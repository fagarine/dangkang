package cn.laoshini.dk.robot.bt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.laoshini.dk.robot.bt.node.BtRootNode;
import cn.laoshini.dk.robot.bt.node.IBtNode;
import cn.laoshini.dk.robot.bt.node.IBtNodeConfig;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.LogUtil;

/**
 * 机器人行为树
 *
 * @author fagarine
 */
public class BehaviorTree implements IBehaviorTree {

    /**
     * 行为树配置信息
     */
    private IBtConfig config;

    /**
     * 行为树节点配置信息
     */
    private Map<Integer, IBtNodeConfig> nodeConfigMap;

    private BtRootNode root;
    private boolean initialized;
    private boolean shared;

    /**
     * 如果不是共享行为树，记录行为树所属机器人角色
     */
    private long robotRoleId;

    @Override
    public void initTree(Collection<? extends IBtNodeConfig> nodeConfigList) {
        if (CollectionUtil.isNotEmpty(nodeConfigList)) {
            IBtNodeConfig rootConfig = getRootNode(nodeConfigList);
            if (null == rootConfig) {
                // 没有找到有效根节点，行为树实例初始化失败
                LogUtil.error("行为树初始化失败, tree:" + config);
                return;
            }
            createTree(rootConfig);
            initialized = true;
        }
    }

    @Override
    public void tick() {
        if (null != root) {
            root.tick();
        }
    }

    /**
     * 根据根节点配置创建行为树
     *
     * @param rootNodeConfig 根节点配置信息
     */
    private void createTree(IBtNodeConfig rootNodeConfig) {
        if (null == rootNodeConfig || !rootNodeConfig.isRootNode()) {
            return;
        }

        root = (BtRootNode) BehaviorTreeProducer.createNode(rootNodeConfig, isShared());

        if (CollectionUtil.isNotEmpty(rootNodeConfig.getChildren())) {
            createChildNodes(rootNodeConfig, root);
        }
    }

    /**
     * 递归创建节点的所有子节点
     *
     * @param parentConfig 父节点配置信息
     * @param parent 父节点对象
     */
    private void createChildNodes(IBtNodeConfig parentConfig, IBtNode parent) {
        IBtNode childNode;
        IBtNodeConfig childConfig;
        for (Integer childNodeId : parentConfig.getChildren()) {
            childConfig = nodeConfigMap.get(childNodeId);
            if (null != childConfig) {
                childNode = BehaviorTreeProducer.createNode(childConfig, isShared());
                if (null != childNode) {
                    parent.addNode(childNode);
                    childNode.setParent(parent);

                    if (!childConfig.isLeafNode()) {
                        // 递归创建子节点
                        createChildNodes(childConfig, childNode);
                    }
                }
            }
        }
    }

    /**
     * 从传入的节点配置信息中，找出根节点配置信息
     *
     * @param nodes 节点配置信息
     * @return 如果传入信息中没有根节点配置信息，将会抛出异常
     */
    private IBtNodeConfig getRootNode(Collection<? extends IBtNodeConfig> nodes) {
        nodeConfigMap = new HashMap<>(nodes.size());
        List<IBtNodeConfig> rootNodeList = new ArrayList<>();
        for (IBtNodeConfig node : nodes) {
            if (node.isRootNode()) {
                rootNodeList.add(node);
            }
            nodeConfigMap.put(node.getNodeId(), node);
        }

        // 没有根节点，或不止一个，判定该行为树无效
        if (rootNodeList.isEmpty() || rootNodeList.size() > 1) {
            throw new IllegalArgumentException("行为树根节点配置错误，程序无法处理, tree:" + config);
        }
        return rootNodeList.get(0);
    }

    /**
     * 设置节点的所有子节点机器人信息
     *
     * @param roleId 机器人的角色id
     * @param parent 父节点
     */
    private void fillChildrenRobotId(long roleId, IBtNode parent) {
        if (null != parent) {
            parent.setRobotRoleId(roleId);
            if (CollectionUtil.isNotEmpty(parent.getChildren())) {
                for (IBtNode node : parent.getChildren()) {
                    // 递归设置
                    fillChildrenRobotId(roleId, node);
                }
            }
        }
    }

    /**
     * 将机器人信息设置到行为树的所有节点中
     */
    private void putRobotToNodes() {
        if (robotRoleId > 0) {
            fillChildrenRobotId(robotRoleId, getRoot());
        }
    }

    @Override
    public void setRobotId(long robotId) {
        this.robotRoleId = robotId;
        putRobotToNodes();
    }

    public long getRobotRoleId() {
        return robotRoleId;
    }

    @Override
    public void setTreeConfig(IBtConfig config) {
        setConfig(config);
    }

    public IBtConfig getConfig() {
        return config;
    }

    public void setConfig(IBtConfig config) {
        this.config = config;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    @Override
    public BtRootNode getRoot() {
        return root;
    }

    public void setRoot(BtRootNode root) {
        this.root = root;
    }

}
