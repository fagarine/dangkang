package cn.laoshini.dk.expression.js;

import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import cn.laoshini.dk.constant.ExpressionConstant;
import cn.laoshini.dk.constant.ExpressionDependEnum;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.expression.BaseExpressionProcessor;
import cn.laoshini.dk.expression.ExpressionLogicContext;
import cn.laoshini.dk.util.StringUtil;

/**
 * @author fagarine
 */
public class JavaScriptExpressionProcessor extends BaseExpressionProcessor {

    private static final ScriptEngineManager SCRIPT_ENGINE_MANAGER = new ScriptEngineManager();

    private ScriptEngine scriptEngine;

    /**
     * 同一JS表达式逻辑对象下，共享一个脚本上下本对象
     */
    private transient ScriptContext scriptContext;

    @Override
    public void prepare() {
        if (ExpressionConstant.ExpressionCodeType.DEPENDENCY.name().equals(descriptor.getType())) {
            try {
                scriptEngine.eval(descriptor.getExpression());

                Invocable invocable = (Invocable) scriptEngine;
                List<String> depends = (List<String>) invocable
                        .invokeFunction(ExpressionConstant.JS_DEPENDENCY_FUNCTION);
                for (String dependency : depends) {
                    String[] ss = dependency.trim().replaceAll("[ ]+", " ").split(" ");
                    if (ss.length >= 2) {
                        ExpressionDependEnum codeType = ExpressionDependEnum.valueOf(ss[0]);
                        scriptContext.setAttribute(ss[1], codeType.getValue(ss[1]), ScriptContext.GLOBAL_SCOPE);
                    }
                }
            } catch (ScriptException | NoSuchMethodException e) {
                throw new BusinessException("js.expression.error", "执行JS表达式前置引导出错", e);
            }
        }
    }

    @Override
    public Object action(ExpressionLogicContext logicContext) {
        if (ExpressionConstant.ExpressionCodeType.LOGIC.name().equals(descriptor.getType())) {
            try {
                Object result = scriptEngine.eval(descriptor.getExpression());
                if (result != null && StringUtil.isNotEmptyString(descriptor.getRecordParam())) {
                    logicContext.addParam(descriptor.getRecordParam(), result);
                }
                return result;
            } catch (ScriptException e) {
                throw new BusinessException("js.expression.error", "执行JS表达式出错", e);
            }
        }
        return null;
    }

    void setScriptContext(ScriptContext scriptContext) {
        this.scriptContext = scriptContext;
        if (scriptEngine == null) {
            scriptEngine = newScriptEngine(scriptContext);
        } else {
            scriptEngine.setContext(scriptContext);
        }
    }

    private static ScriptEngine newScriptEngine(ScriptContext context) {
        ScriptEngine engine = SCRIPT_ENGINE_MANAGER.getEngineByName("nashorn");
        if (context != null) {
            engine.setContext(context);
        }
        return engine;
    }
}
