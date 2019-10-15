package cn.laoshini.dk.domain;

import java.lang.annotation.Annotation;

import cn.laoshini.dk.domain.common.ArrayTuple;

/**
 * 可执行方法与注解关系映射
 *
 * @author fagarine
 */
public class ExecutorBean<AnnotationType extends Annotation> {

    /**
     * 注解对象
     */
    private AnnotationType annotation;

    /**
     * 关联的类
     */
    private Class<?> executorClass;

    /**
     * 关联的方法名称
     */
    private String executorMethod;

    private ArrayTuple<String, String> params;

    public ExecutorBean(AnnotationType annotation, String executorMethod, Class<?> executorClass) {
        this.annotation = annotation;
        this.executorMethod = executorMethod;
        this.executorClass = executorClass;
    }

    public ExecutorBean(AnnotationType annotation, Class<?> executorClass, String executorMethod,
            ArrayTuple<String, String> params) {
        this.annotation = annotation;
        this.executorClass = executorClass;
        this.executorMethod = executorMethod;
        this.params = params;
    }

    public AnnotationType getAnnotation() {
        return annotation;
    }

    public void setAnnotation(AnnotationType annotation) {
        this.annotation = annotation;
    }

    public String getExecutorMethod() {
        return executorMethod;
    }

    public void setExecutorMethod(String executorMethod) {
        this.executorMethod = executorMethod;
    }

    public Class<?> getExecutorClass() {
        return executorClass;
    }

    public void setExecutorClass(Class<?> executorClass) {
        this.executorClass = executorClass;
    }

    public String getExecutorClassName() {
        return executorClass.getName();
    }

    public ArrayTuple<String, String> getParams() {
        return params;
    }

    public void setParams(ArrayTuple<String, String> params) {
        this.params = params;
    }
}
