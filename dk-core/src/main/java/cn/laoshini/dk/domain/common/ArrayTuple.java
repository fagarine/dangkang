package cn.laoshini.dk.domain.common;

/**
 * 两个元素都为数组的元组类型
 * <p>
 * 该类的缺陷：如果数组元素是原始数据类型，如int[]、long[]这样的数据，无法直接装箱拆箱，与Integer[]、Long[]之间进行转换；<br>
 * 这时最好是使用父类，参数类型为数组类型，如：Tuple&lt;int[], long[]&gt;
 * </p>
 *
 * @param <V1> 第一个数组的元素类型
 * @param <V2> 第二个数组的元素类型
 * @author fagarine
 */
public class ArrayTuple<V1, V2> extends Tuple<V1[], V2[]> {

    private static final long serialVersionUID = 1L;

    public ArrayTuple() {
    }

    public ArrayTuple(V1[] v1s, V2[] v2s) {
        super(v1s, v2s);
    }

    public int length() {
        return getV1() != null ? getV1().length : 0;
    }

    public Tuple<V1, V2> getTuple(int index) {
        if (index < 0 || index >= length()) {
            throw new IllegalArgumentException("invalid array tuple index:" + index);
        }
        return new Tuple<>(getV1()[index], getV2()[index]);
    }

    public TupleArray<V1, V2> toTupleArray() {
        return new TupleArray<>(this);
    }

    @Override
    public String toString() {
        return "ArrayTuple{} " + super.toString();
    }
}
