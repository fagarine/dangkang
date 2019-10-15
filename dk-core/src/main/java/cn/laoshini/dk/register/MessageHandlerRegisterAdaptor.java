package cn.laoshini.dk.register;

import java.util.function.Function;

import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.net.MessageHandlerHolder;
import cn.laoshini.dk.util.SpringUtils;

/**
 * @author fagarine
 */
public class MessageHandlerRegisterAdaptor implements IMessageHandlerRegister {

    private IClassScanner<Class<?>> scanner;

    private Function<Class<?>, Integer> idReader;

    private boolean singleton;

    @Override
    public IClassScanner<Class<?>> scanner() {
        return scanner;
    }

    @Override
    public MessageHandlerRegisterAdaptor setScanner(IClassScanner<Class<?>> handlerScanner) {
        this.scanner = handlerScanner;
        return this;
    }

    @Override
    public Function<Class<?>, Integer> idReader() {
        return idReader;
    }

    @Override
    public MessageHandlerRegisterAdaptor setIdReader(Function<Class<?>, Integer> idReader) {
        this.idReader = idReader;
        return this;
    }

    @Override
    public MessageHandlerRegisterAdaptor singleton() {
        this.singleton = true;
        return this;
    }

    @Override
    public MessageHandlerRegisterAdaptor registerHandlerClass(Class<?> handlerClass) {
        Integer id = idReader.apply(handlerClass);
        if (singleton) {
            if (SpringUtils.isSpringBeanClass(handlerClass)) {
                MessageHandlerHolder.registerSpringBeanHandler(id, SpringUtils.registerSpringBean(handlerClass));
            } else {
                try {
                    Object object = handlerClass.newInstance();
                    MessageHandlerHolder.registerSingletonHandler(id, object);
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new BusinessException("execute.constructor.fail",
                            String.format("调用类[%s]的无参构造方法出错", handlerClass.getName()), e);
                }
            }
        } else {
            MessageHandlerHolder.registerHandler(id, handlerClass);
        }

        return this;
    }

}
