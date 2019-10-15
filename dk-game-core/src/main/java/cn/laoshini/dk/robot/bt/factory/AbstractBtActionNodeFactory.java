package cn.laoshini.dk.robot.bt.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.laoshini.dk.robot.bt.node.AbstractBtAction;

/**
 * action叶节点工厂
 *
 * @author fagarine
 */
public abstract class AbstractBtActionNodeFactory implements IBtNodeFactory {

    protected Map<Integer, Class<? extends AbstractBtAction>> actionNodeMap = new ConcurrentHashMap<>();

    @Override
    public AbstractBtAction createBtNode(int actionType, boolean shared) {
        return createActionNode(actionType, shared);
    }

    /**
     * 创建action节点对象
     *
     * @param actionNodeType action节点类型
     * @param shared 是否共享节点
     * @return
     */
    public abstract AbstractBtAction createActionNode(int actionNodeType, boolean shared);

    /**
     * 注册action节点类
     *
     * @param actionType
     * @param clazz
     */
    public void registerActionNode(int actionType, Class<? extends AbstractBtAction> clazz) {
        if (null != clazz) {
            actionNodeMap.put(actionType, clazz);
        }
    }
}
