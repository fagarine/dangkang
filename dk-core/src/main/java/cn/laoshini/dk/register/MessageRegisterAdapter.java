package cn.laoshini.dk.register;

import java.util.function.Function;

/**
 * @author fagarine
 */
public class MessageRegisterAdapter implements IMessageRegister {

    private IClassScanner<Class<?>> scanner;

    private Function<Class<?>, Integer> idReader;

    @Override
    public IClassScanner<Class<?>> scanner() {
        return scanner;
    }

    @Override
    public MessageRegisterAdapter setScanner(IClassScanner<Class<?>> messageScanner) {
        this.scanner = messageScanner;
        return this;
    }

    @Override
    public Function<Class<?>, Integer> idReader() {
        return idReader;
    }

    @Override
    public MessageRegisterAdapter setIdReader(Function<Class<?>, Integer> idReader) {
        this.idReader = idReader;
        return this;
    }

}
