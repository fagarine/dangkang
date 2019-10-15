package cn.laoshini.dk.jit.type;

/**
 * @author fagarine
 */
public abstract class AbstractNumericTypeBean<T> extends AbstractTypeBean<T> {

    private T min;

    private T max;

    public T getMin() {
        return min;
    }

    public void setMin(T min) {
        this.min = min;
    }

    public T getMax() {
        return max;
    }

    public void setMax(T max) {
        this.max = max;
    }

    @Override
    public String toString() {
        return "AbstractNumericTypeBean{" + "min=" + min + ", max=" + max + "} " + super.toString();
    }
}
