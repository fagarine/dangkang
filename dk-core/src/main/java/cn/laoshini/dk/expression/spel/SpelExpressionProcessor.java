package cn.laoshini.dk.expression.spel;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import cn.laoshini.dk.constant.ExpressionConstant;
import cn.laoshini.dk.constant.ExpressionDependEnum;
import cn.laoshini.dk.expression.BaseExpressionProcessor;
import cn.laoshini.dk.expression.ExpressionLogicContext;
import cn.laoshini.dk.util.StringUtil;

/**
 * 使用Spring表达式实现的逻辑处理器
 *
 * @author fagarine
 */
public class SpelExpressionProcessor extends BaseExpressionProcessor {

    /**
     * 已解析出的Spring表达式对象
     */
    private Expression parsedExpression;

    @Override
    public Object action(ExpressionLogicContext logicContext) {
        if (parsedExpression == null) {
            parsedExpression = new SpelExpressionParser().parseExpression(descriptor.getExpression());
        }

        EvaluationContext ec = null;
        String type = descriptor.getType();
        if (type != null && type.contains(ExpressionConstant.ExpressionCodeType.DEPENDENCY.name())) {
            // 格式："DEPENDENCY " + 表达式依赖类型 + " " + 参数名称
            String[] ss = type.replaceAll("[ ]+", " ").split(" ");
            if (ss.length >= 3) {
                ExpressionDependEnum dependEnum = ExpressionDependEnum.valueOf(ss[1]);
                if (ExpressionDependEnum.LOCAL.equals(dependEnum)) {
                    ec = new StandardEvaluationContext(logicContext.getParam(ss[2]));
                } else {
                    ec = new StandardEvaluationContext(dependEnum.getValue(ss[2]));
                }
            }
        }

        Object result = ec == null ? parsedExpression.getValue() : parsedExpression.getValue(ec);
        if (result != null && StringUtil.isNotEmptyString(descriptor.getRecordParam())) {
            logicContext.addParam(descriptor.getRecordParam(), result);
        }
        return result;
    }
}
