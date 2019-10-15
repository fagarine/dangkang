package cn.laoshini.dk.expression;

import java.util.List;
import java.util.Map;

import cn.laoshini.dk.util.CollectionUtil;

/**
 * 表达式逻辑处理，包含了若干个表达式逻辑处理器，代表着一个完整的任务逻辑
 *
 * @author fagarine
 */
public interface IExpressionLogic {

    /**
     * 获取所有表达式逻辑处理器
     *
     * @return 返回所有表达式逻辑处理器
     */
    List<IExpressionProcessor> processors();

    /**
     * 执行表达式逻辑
     *
     * @param params 传入上下文相关参数
     * @return 如果执行结果有返回值，返回执行结果，否则返回null
     */
    default Object execute(Map<String, Object> params) {
        ExpressionLogicContext logicContext = new ExpressionLogicContext(params);
        return execute(logicContext);
    }

    /**
     * 执行表达式逻辑
     *
     * @param logicContext 表达式逻辑上下文信息
     * @return 如果执行结果有返回值，返回执行结果，否则返回null
     */
    default Object execute(ExpressionLogicContext logicContext) {
        List<IExpressionProcessor> processors = processors();
        if (CollectionUtil.isEmpty(processors)) {
            return logicContext.getResult();
        }

        for (IExpressionProcessor processor : processors) {
            processor.action(logicContext);
        }

        logicContext.clear();
        return logicContext.getResult();
    }

}
