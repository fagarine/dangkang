package cn.laoshini.dk.jit.type;

/**
 * @author fagarine
 */
public abstract class AbstractTypeBean<T> implements ITypeBean<T> {

    protected String name;

    protected Class<T> valueType;

    protected T val;

    protected T defaultVal;

    protected String description;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Class<T> getValueType() {
        return valueType;
    }

    public void setValueType(Class<T> valueType) {
        this.valueType = valueType;
    }

    @Override
    public T getVal() {
        if (val == null) {
            return getDefaultVal();
        }
        return val;
    }

    public void setVal(T val) {
        this.val = val;
    }

    @Override
    public T getDefaultVal() {
        return defaultVal;
    }

    public void setDefaultVal(T defaultVal) {
        this.defaultVal = defaultVal;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
