package cn.laoshini.dk.expression.spel;

import cn.laoshini.dk.constant.ExpressionConstant;
import cn.laoshini.dk.expression.BaseExpressionLogic;

/**
 * 使用Spring表达式实现的表达式逻辑处理类
 *
 * @author fagarine
 */
public class SpelExpressionLogic extends BaseExpressionLogic {

    public SpelExpressionLogic() {
        super(ExpressionConstant.ExpressionTypeEnum.SPEL);
    }
}
