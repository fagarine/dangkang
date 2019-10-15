package cn.laoshini.dk.expression;

/**
 * 表达式处理器的接口，该接口实现具体表达式的执行
 *
 * @author fagarine
 */
public interface IExpressionProcessor {

    /**
     * 准备表达式执行条件
     */
    default void prepare() {
    }

    /**
     * 运行表达式，并返回执行结果
     *
     * @param logicContext 逻辑上下文内容
     * @return 返回执行结果
     */
    Object action(ExpressionLogicContext logicContext);
}
