package cn.laoshini.dk.expression;

import cn.laoshini.dk.constant.ExpressionConstant;
import cn.laoshini.dk.domain.dto.HandlerExpDescriptorDTO;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.expression.js.JavaScriptExpressionLogic;
import cn.laoshini.dk.expression.js.JavaScriptExpressionProcessor;
import cn.laoshini.dk.expression.spel.SpelExpressionLogic;
import cn.laoshini.dk.expression.spel.SpelExpressionProcessor;

/**
 * @author fagarine
 */
public class ExpressionLogicFactory {
    private ExpressionLogicFactory() {
    }

    public static IExpressionLogic newExpressionLogic(HandlerExpDescriptorDTO descriptorVO) {
        ExpressionConstant.ExpressionTypeEnum type = ExpressionConstant.ExpressionTypeEnum
                .valueOf(descriptorVO.getExpressionType());
        BaseExpressionLogic logic;
        switch (type) {
            case SPEL:
                logic = new SpelExpressionLogic();
                break;

            case JS:
                logic = new JavaScriptExpressionLogic();
                break;

            default:
                throw new BusinessException("expression.not.supported", "不支持的表达式类型:" + type);
        }

        logic.setBlocks(descriptorVO.getExpressionBlocks());
        return logic;
    }

    public static BaseExpressionProcessor newExpressionProcessor(ExpressionConstant.ExpressionTypeEnum type) {
        BaseExpressionProcessor processor;
        switch (type) {
            case SPEL:
                processor = new SpelExpressionProcessor();
                break;

            case JS:
                processor = new JavaScriptExpressionProcessor();
                break;

            default:
                throw new BusinessException("expression.not.supported", "不支持的表达式类型:" + type);
        }
        return processor;
    }

}
