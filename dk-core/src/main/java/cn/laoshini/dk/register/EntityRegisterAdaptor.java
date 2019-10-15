package cn.laoshini.dk.register;

import java.util.function.Function;

/**
 * @author fagarine
 */
public class EntityRegisterAdaptor implements IEntityRegister {

    private IClassScanner<Class<?>> scanner;

    private Function<Class<?>, String> tableNameReader;

    @Override
    public IClassScanner<Class<?>> scanner() {
        return scanner;
    }

    @Override
    public EntityRegisterAdaptor setScanner(IClassScanner<Class<?>> scanner) {
        this.scanner = scanner;
        return this;
    }

    @Override
    public Function<Class<?>, String> tableNameReader() {
        return tableNameReader;
    }

    @Override
    public EntityRegisterAdaptor setTableNameReader(Function<Class<?>, String> tableNameReader) {
        this.tableNameReader = tableNameReader;
        return this;
    }
}
