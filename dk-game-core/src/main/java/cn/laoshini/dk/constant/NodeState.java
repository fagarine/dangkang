package cn.laoshini.dk.constant;

/**
 * 行为树节点状态枚举
 *
 * @author fagarine
 */
public enum NodeState {
    /**
     * 执行中
     */
    RUNNING,

    /**
     * 已执行完并返回成功
     */
    SUCCEED,

    /**
     * 已执行完并返回失败
     */
    FAILED,
    ;
}
