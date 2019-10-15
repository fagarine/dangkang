package cn.laoshini.dk.domain;

import cn.laoshini.dk.domain.common.ArrayTuple;

/**
 * 描述可执行方法的类
 *
 * @param <K> 唯一标识符类型
 * @param <E> 扩展数据类型
 * @author fagarine
 */
public class ExecutorMethod<K, E> {

    private K uid;

    private Class<?> handlerClass;

    private String methodName;

    /**
     * 方法参数表，第一个元素记录参数类型（类的全限定名），第二个元素记录参数名称
     */
    private ArrayTuple<String, String> params;

    /**
     * 记录扩展数据
     */
    private E extension;

    public ExecutorMethod() {
    }

    public ExecutorMethod(K uid, Class<?> handlerClass, String methodName, ArrayTuple<String, String> params) {
        this.uid = uid;
        this.handlerClass = handlerClass;
        this.methodName = methodName;
        this.params = params;
    }

    public ExecutorMethod(K uid, Class<?> handlerClass, String methodName, ArrayTuple<String, String> params,
            E extension) {
        this.uid = uid;
        this.handlerClass = handlerClass;
        this.methodName = methodName;
        this.params = params;
        this.extension = extension;
    }

    public K getUid() {
        return uid;
    }

    public void setUid(K uid) {
        this.uid = uid;
    }

    public Class<?> getHandlerClass() {
        return handlerClass;
    }

    public void setHandlerClass(Class<?> handlerClass) {
        this.handlerClass = handlerClass;
    }

    public String getClassName() {
        return handlerClass.getName();
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public ArrayTuple<String, String> getParams() {
        return params;
    }

    public void setParams(ArrayTuple<String, String> params) {
        this.params = params;
    }

    public E getExtension() {
        return extension;
    }

    public void setExtension(E extension) {
        this.extension = extension;
    }

    @Override
    public String toString() {
        return "ExecutorMethod{" + "uid=" + uid + ", handlerClass=" + handlerClass + ", methodName='" + methodName
                + '\'' + ", params=" + params + ", extension=" + extension + '}';
    }
}
