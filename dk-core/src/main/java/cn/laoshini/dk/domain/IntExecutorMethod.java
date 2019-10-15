package cn.laoshini.dk.domain;

import cn.laoshini.dk.domain.common.ArrayTuple;

/**
 * 使用整型作为标识符的可执行方法描述类
 *
 * @author fagarine
 */
public class IntExecutorMethod<E> extends ExecutorMethod<Integer, E> {

    public IntExecutorMethod() {
    }

    public IntExecutorMethod(Integer uid, Class<?> handlerClass, String methodName, ArrayTuple<String, String> params) {
        super(uid, handlerClass, methodName, params);
    }

    public IntExecutorMethod(Integer uid, Class<?> handlerClass, String methodName, String[] paramTypes,
            String[] paramNames) {
        super(uid, handlerClass, methodName, new ArrayTuple<>(paramTypes, paramNames));
    }

    public IntExecutorMethod(Integer uid, Class<?> handlerClass, String methodName, ArrayTuple<String, String> params,
            E extension) {
        super(uid, handlerClass, methodName, params, extension);
    }

    public IntExecutorMethod(Integer uid, Class<?> handlerClass, String methodName, String[] paramTypes,
            String[] paramNames, E extension) {
        super(uid, handlerClass, methodName, new ArrayTuple<>(paramTypes, paramNames), extension);
    }
}
