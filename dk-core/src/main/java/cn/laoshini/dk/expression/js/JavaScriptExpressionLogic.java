package cn.laoshini.dk.expression.js;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;

import cn.laoshini.dk.constant.ExpressionConstant;
import cn.laoshini.dk.expression.BaseExpressionLogic;
import cn.laoshini.dk.expression.IExpressionProcessor;
import cn.laoshini.dk.util.CollectionUtil;

/**
 * 使用JavaScript脚本实现的表达式逻辑处理类
 *
 * @author fagarine
 */
public class JavaScriptExpressionLogic extends BaseExpressionLogic {

    public JavaScriptExpressionLogic() {
        super(ExpressionConstant.ExpressionTypeEnum.JS);
    }

    @Override
    protected void initProcessors() {
        super.initProcessors();

        if (CollectionUtil.isNotEmpty(processors())) {
            ScriptContext context = new SimpleScriptContext();
            for (IExpressionProcessor processor : processors()) {
                JavaScriptExpressionProcessor jsProcessor = (JavaScriptExpressionProcessor) processor;
                jsProcessor.setScriptContext(context);
                jsProcessor.prepare();
            }
        }
    }
}
