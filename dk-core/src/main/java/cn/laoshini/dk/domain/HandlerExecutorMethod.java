package cn.laoshini.dk.domain;

import cn.laoshini.dk.domain.common.ArrayTuple;

/**
 * @author fagarine
 */
public class HandlerExecutorMethod extends IntExecutorMethod<HandlerDesc> {

    /**
     * 是否只创建一次Handler对象，默认为true，在Handler中记录消息等数据的需要每次创建新的对象
     */
    private boolean buildOnce = true;

    /**
     * 如果不是每次都创建，则记录已创建对象
     */
    private Object handler;

    public HandlerExecutorMethod(Integer uid, Class<?> handlerClass, String methodName,
            ArrayTuple<String, String> params, HandlerDesc extension) {
        super(uid, handlerClass, methodName, params, extension);
    }

    public HandlerExecutorMethod(int uid, Class<?> handlerClass, String methodName, String[] paramTypes,
            String[] paramNames, HandlerDesc extension) {
        super(uid, handlerClass, methodName, paramTypes, paramNames, extension);
    }

    public HandlerExecutorMethod(Integer uid, Class<?> handlerClass, String methodName,
            ArrayTuple<String, String> params, HandlerDesc extension, boolean buildOnce) {
        super(uid, handlerClass, methodName, params, extension);
        this.buildOnce = buildOnce;
    }

    public HandlerExecutorMethod(Integer uid, Class<?> handlerClass, String methodName,
            ArrayTuple<String, String> params, HandlerDesc extension, boolean buildOnce, Object handler) {
        super(uid, handlerClass, methodName, params, extension);
        this.buildOnce = buildOnce;
        this.handler = handler;
    }

    public boolean isBuildOnce() {
        return buildOnce;
    }

    public void setBuildOnce(boolean buildOnce) {
        this.buildOnce = buildOnce;
    }

    public Object getHandler() {
        return handler;
    }

    public void setHandler(Object handler) {
        this.handler = handler;
    }

    @Override
    public String toString() {
        return "HandlerExecutorMethod{" + "buildOnce=" + buildOnce + ", handler=" + handler + "} " + super.toString();
    }
}
