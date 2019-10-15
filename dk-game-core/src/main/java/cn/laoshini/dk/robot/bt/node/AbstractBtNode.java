package cn.laoshini.dk.robot.bt.node;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;

import cn.laoshini.dk.constant.BtNodeType;
import cn.laoshini.dk.constant.CompositeType;
import cn.laoshini.dk.constant.NodeState;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.StringUtil;

/**
 * 行为树节点抽象类，实现一些基础功能
 *
 * @author fagarine
 */
public abstract class AbstractBtNode implements IBtNode {
    /**
     * 当前状态
     */
    protected NodeState state;

    /**
     * 节点类型
     */
    protected BtNodeType nodeType;

    /**
     * 复合类节点的具体复合类型
     */
    protected CompositeType compositeNode = CompositeType.NONE;

    /**
     * 节点配置信息
     */
    protected IBtNodeConfig nodeConfig;

    protected IBtNode parent;

    protected List<IBtNode> children;

    protected long robotRoleId;

    /**
     * 是否是共享节点
     */
    protected boolean shared;

    @Override
    public BtNodeType getNodeType() {
        return nodeType;
    }

    @Override
    public CompositeType getCompositeType() {
        return compositeNode;
    }

    @Override
    public void setParent(IBtNode parent) {
        this.parent = parent;
    }

    @Override
    public IBtNode getParent() {
        return parent;
    }

    @Override
    public NodeState currentState() {
        return state;
    }

    @Override
    public List<IBtNode> getChildren() {
        return children;
    }

    @Override
    public void addNode(IBtNode node) {
        if (null != node) {
            if (null == children) {
                children = new ArrayList<>();
            }
            children.add(node);
        }
    }

    @Override
    public void removeNode(IBtNode node) {
        if (null != node && null != children) {
            children.remove(node);
        }
    }

    @Override
    public boolean hasNode(IBtNode node) {
        if (null != node && null != children) {
            return children.contains(node);
        }
        return false;
    }

    public void updateResult(boolean result) {
        if (result) {
            succeed();
        } else {
            fail();
        }
    }

    @Override
    public void succeed() {
        state = NodeState.SUCCEED;
    }

    @Override
    public void fail() {
        state = NodeState.FAILED;
    }

    @Override
    public boolean tick(Object... params) {
        return true;
    }

    @Override
    public void setConfig(IBtNodeConfig config) {
        this.nodeConfig = config;
    }

    @Override
    public IBtNodeConfig getConfig() {
        return nodeConfig;
    }

    @Override
    public String nodeTypeToString() {
        return "nodeType:" + nodeType.getName() + ", compositeType:" + compositeNode.getName();
    }

    /**
     * 是否有节点配置参数信息
     *
     * @return
     */
    public boolean haveConfigParam() {
        return null != nodeConfig && StringUtil.isNotEmptyString(nodeConfig.getParam());
    }

    /**
     * 将节点参数解析成一个双精度浮点型数值返回
     *
     * @return 如果参数信息未配置或解析失败，将会返回-1
     */
    protected double parseDoubleParam() {
        if (haveConfigParam()) {
            try {
                return Double.parseDouble(nodeConfig.getParam().trim());
            } catch (Exception e) {
                LogUtil.error(e, "节点配置参数信息解析出错, nodeConfig:" + nodeConfig);
            }
        }
        return -1;
    }

    /**
     * 将节点参数解析成一个长整型数值返回
     *
     * @return 如果参数信息未配置或解析失败，将会返回-1
     */
    protected long parseLongParam() {
        if (haveConfigParam()) {
            try {
                return Long.parseLong(nodeConfig.getParam().trim());
            } catch (Exception e) {
                LogUtil.error(e, "节点配置参数信息解析出错, nodeConfig:" + nodeConfig);
            }
        }
        return -1;
    }

    /**
     * 将节点参数解析成一个整型数值返回
     *
     * @return 如果参数信息未配置或解析失败，将会返回-1
     */
    protected int parseIntParam() {
        if (haveConfigParam()) {
            try {
                return Integer.parseInt(nodeConfig.getParam().trim());
            } catch (Exception e) {
                LogUtil.error(e, "节点配置参数信息解析出错, nodeConfig:" + nodeConfig);
            }
        }
        return -1;
    }

    /**
     * 将节点参数解析成一个String数组返回
     *
     * @return 如果参数信息未配置或解析失败，将会返回一个空数组
     */
    protected String[] parseListStringParam() {
        if (haveConfigParam()) {
            try {
                JSONArray arr = JSONArray.parseArray(getConfig().getParam());
                String[] params = new String[arr.size()];
                for (int i = 0; i < arr.size(); i++) {
                    params[i] = arr.getString(i);
                }
                return params;
            } catch (Exception e) {
                LogUtil.error(e, "节点配置参数信息解析出错, nodeConfig:" + nodeConfig);
            }
        }

        return new String[0];
    }

    /**
     * 将节点参数解析成一个int数组返回
     *
     * @return 如果参数信息未配置或解析失败，将会返回一个空数组
     */
    protected int[] parseListIntParam() {
        if (haveConfigParam()) {
            try {
                JSONArray arr = JSONArray.parseArray(getConfig().getParam());
                int[] params = new int[arr.size()];
                for (int i = 0; i < arr.size(); i++) {
                    params[i] = arr.getInteger(i);
                }
                return params;
            } catch (Exception e) {
                LogUtil.error(e, "节点配置参数信息解析出错, nodeConfig:" + nodeConfig);
            }

        }
        return new int[0];
    }

    @Override
    public void setRobotRoleId(long roleId) {
        this.robotRoleId = roleId;
    }

    public long getRobotRoleId() {
        return robotRoleId;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }
}
