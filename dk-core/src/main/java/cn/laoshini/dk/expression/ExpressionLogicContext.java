package cn.laoshini.dk.expression;

import java.util.HashMap;
import java.util.Map;

import cn.laoshini.dk.constant.ExpressionConstant;
import cn.laoshini.dk.util.CollectionUtil;

/**
 * 表达式逻辑上下文，记录和传递一个完整表达式逻辑的上下文数据
 *
 * @author fagarine
 */
public class ExpressionLogicContext {

    /**
     * 记录上下文数据
     */
    private Map<String, Object> nameToParams = new HashMap<>();

    /**
     * 记录逻辑的最终返回结果
     */
    private Object result;

    public ExpressionLogicContext() {
    }

    public ExpressionLogicContext(Map<String, Object> paramMap) {
        if (CollectionUtil.isNotEmpty(paramMap)) {
            this.nameToParams.putAll(paramMap);
        }
    }

    public <T> T getParam(String name) {
        return (T) nameToParams.get(name);
    }

    public void addParam(String name, Object value) {
        nameToParams.put(name, value);
    }

    public void clear() {
        if (nameToParams.containsKey(ExpressionConstant.RETURN_PARAM)) {
            setResult(nameToParams.get(ExpressionConstant.RETURN_PARAM));
        }
        nameToParams.clear();
    }

    public Map<String, Object> getAllParams() {
        return new HashMap<>(nameToParams);
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
