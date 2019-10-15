package cn.laoshini.dk.robot.bt.factory;

import cn.laoshini.dk.robot.bt.node.IBtNode;
import cn.laoshini.dk.robot.bt.node.IBtNodeConfig;
import cn.laoshini.dk.util.LogUtil;

/**
 * 行为树节点对象抽象工厂接口
 *
 * @author fagarine
 */
public interface IBtNodeFactory {

    IBtNode createBtNode(int subType, boolean shared);

    default IBtNode createBtNode(int subType) {
        return createBtNode(subType, true);
    }

    default IBtNode createBtNode(IBtNodeConfig config, boolean shared) {
        if (null == config) {
            return null;
        }

        IBtNode node = createBtNode(config.getSubType(), shared);
        if (null != node) {
            node.setConfig(config);
        }
        return node;
    }

    default IBtNode createNode(Class<? extends IBtNode> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            LogUtil.error(e, "创建节点对象失败, clazz:" + clazz.getName());
        }
        return null;
    }
}
