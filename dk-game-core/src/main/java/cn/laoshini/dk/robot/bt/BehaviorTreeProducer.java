package cn.laoshini.dk.robot.bt;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import cn.laoshini.dk.constant.BtNodeType;
import cn.laoshini.dk.robot.bt.factory.AbstractBtActionNodeFactory;
import cn.laoshini.dk.robot.bt.factory.AbstractBtConditionNodeFactory;
import cn.laoshini.dk.robot.bt.factory.IBtNodeFactory;
import cn.laoshini.dk.robot.bt.factory.impl.BtActionNodeFactoryImpl;
import cn.laoshini.dk.robot.bt.factory.impl.BtCompositeNodeFactory;
import cn.laoshini.dk.robot.bt.factory.impl.BtConditionNodeFactoryImpl;
import cn.laoshini.dk.robot.bt.factory.impl.BtDecoratorNodeFactory;
import cn.laoshini.dk.robot.bt.factory.impl.BtRootNodeFactoryImpl;
import cn.laoshini.dk.robot.bt.node.AbstractBtAction;
import cn.laoshini.dk.robot.bt.node.AbstractBtCondition;
import cn.laoshini.dk.robot.bt.node.IBtNode;
import cn.laoshini.dk.robot.bt.node.IBtNodeConfig;

/**
 * 行为树、节点、工厂的生产类
 *
 * @author fagarine
 */
public class BehaviorTreeProducer {
    private static BehaviorTreeProducer ins = new BehaviorTreeProducer();

    public static BehaviorTreeProducer getIns() {
        return ins;
    }

    private BehaviorTreeProducer() {
    }

    /**
     * 记录节点类型对应的节点工厂对象
     */
    private Map<BtNodeType, IBtNodeFactory> typeToFactory = new EnumMap<>(BtNodeType.class);

    /**
     * 注册节点工厂对象
     *
     * @param nodeType 节点类型
     * @param factory 对应的工厂对象
     */
    public static void registerNodeFactory(int nodeType, IBtNodeFactory factory) {
        if (null != factory) {
            BtNodeType type = BtNodeType.byCode(nodeType);
            registerNodeFactory(type, factory);
        }
    }

    /**
     * 注册action节点类
     *
     * @param actionType 具体的action分类类型
     * @param clazz 具体处理的action类
     */
    public static void registerActionNode(int actionType, Class<? extends AbstractBtAction> clazz) {
        if (null != clazz) {
            AbstractBtActionNodeFactory factory = (AbstractBtActionNodeFactory) getFactory(BtNodeType.ACTION);
            factory.registerActionNode(actionType, clazz);
        }
    }

    /**
     * 注册condition节点类
     *
     * @param conditionType 具体的condition节点分类类型
     * @param clazz 对应的condition类
     */
    public static void registerConditionNode(int conditionType, Class<? extends AbstractBtCondition> clazz) {
        if (null != clazz) {
            AbstractBtConditionNodeFactory factory = (AbstractBtConditionNodeFactory) getFactory(BtNodeType.CONDITION);
            factory.registerConditionNode(conditionType, clazz);
        }
    }

    /**
     * 根据行为树配置和行为树对应的所有节点配置信息，创建行为树对象
     *
     * @param treeConfig 行为树配置信息
     * @param nodeConfigList 所有属于该行为树的节点配置信息
     * @return 返回按配置信息生成的行为树对象，如果配置信息为null或有问题，将返回null
     */
    public static BehaviorTree createBehaviorTree(IBtConfig treeConfig, List<? extends IBtNodeConfig> nodeConfigList) {
        if (null == treeConfig) {
            return null;
        }

        BehaviorTree tree = new BehaviorTree();
        tree.setConfig(treeConfig);
        tree.initTree(nodeConfigList);
        return tree;
    }

    /**
     * 根据节点配置信息，生成节点对象
     *
     * @param config 行为树节点配置信息
     * @param shared 是否是共享节点
     * @return 返回生成的节点对象，该方法可能返回null
     */
    public static IBtNode createNode(IBtNodeConfig config, boolean shared) {
        if (null == config) {
            return null;
        }

        IBtNodeFactory factory = getFactory(config.getNodeType());
        IBtNode node = factory.createBtNode(config.getSubType(), shared);
        if (null != node && node.isValid()) {
            node.setConfig(config);
        }
        return node;
    }

    /**
     * 根据具体的节点类，生成节点对象
     *
     * @param nodeType 节点类型
     * @param clazz 节点类
     * @return
     */
    public static IBtNode createNode(int nodeType, Class<? extends IBtNode> clazz) {
        IBtNodeFactory factory = getFactory(nodeType);
        return factory.createNode(clazz);
    }

    public static void registerNodeFactory(BtNodeType type, IBtNodeFactory factory) {
        if (null != factory) {
            ins.typeToFactory.put(type, factory);
        }
    }

    /**
     * 根据节点类型获取对应的工厂对象
     *
     * @param nodeType 节点类型
     * @return 返回节点工厂对象
     */
    public static IBtNodeFactory getFactory(int nodeType) {
        return getFactory(BtNodeType.byCode(nodeType));
    }

    public static IBtNodeFactory getFactory(BtNodeType nodeType) {
        IBtNodeFactory factory = ins.typeToFactory.get(nodeType);
        if (null == factory) {
            factory = createDefaultFactory(nodeType);
            ins.typeToFactory.put(nodeType, factory);
        }
        return factory;
    }

    /**
     * 创建默认节点工厂对象
     *
     * @param type
     * @return
     */
    private static IBtNodeFactory createDefaultFactory(BtNodeType type) {
        IBtNodeFactory factory = null;
        switch (type) {
            case ROOT:
                factory = new BtRootNodeFactoryImpl();
                break;

            case DECORATOR:
                factory = new BtDecoratorNodeFactory();
                break;

            case CONDITION:
                factory = new BtConditionNodeFactoryImpl();
                break;

            case ACTION:
                factory = new BtActionNodeFactoryImpl();
                break;

            case COMPOSITE:
                factory = new BtCompositeNodeFactory();
                break;

            default:
                break;
        }
        return factory;
    }
}
