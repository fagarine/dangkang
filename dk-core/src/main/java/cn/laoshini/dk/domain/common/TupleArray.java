package cn.laoshini.dk.domain.common;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 以{@link Tuple}为数组元素的数组结构
 *
 * @param <V1> Tuple第一个元素的类型
 * @param <V2> Tuple第二个元素的类型
 * @author fagarine
 */
public class TupleArray<V1, V2> {

    private static final long serialVersionUID = 1L;

    private Tuple<V1, V2>[] array;

    public TupleArray() {
    }

    public TupleArray(Tuple<V1, V2>[] array) {
        this.array = array;
    }

    public TupleArray(ArrayTuple<V1, V2> arrayTuple) {
        if (arrayTuple != null && arrayTuple.length() > 0) {
            this.array = new Tuple[arrayTuple.length()];
            for (int i = 0; i < array.length; i++) {
                array[i] = new Tuple<>(arrayTuple.getV1()[i], arrayTuple.getV2()[i]);
            }
        }
    }

    public TupleArray(Map<V1, V2> map) {
        if (map != null && map.size() > 0) {
            this.array = new Tuple[map.size()];
            int index = 0;
            for (Map.Entry<V1, V2> entry : map.entrySet()) {
                array[index++] = new Tuple<>(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 将当前对象转为以数组为元素的元组{@link ArrayTuple}类型
     * <p>
     * 当且仅当当前对象中，所有Tuple对象一致时，才适合使用该方法；否则可能发生不可预知的错误
     * </p>
     *
     * @return 该方法不会返回null
     */
    public ArrayTuple<V1, V2> toArrayTuple() {
        int length = length();
        if (length <= 0) {
            return new ArrayTuple<>();
        }

        Class<V1> type1 = (Class<V1>) array[0].getV1().getClass();
        Class<V2> type2 = (Class<V2>) array[0].getV2().getClass();
        V1[] arr1 = (V1[]) Array.newInstance(type1, length);
        V2[] arr2 = (V2[]) Array.newInstance(type2, length);
        for (int i = 0; i < array.length; i++) {
            Tuple<V1, V2> tuple = array[i];
            arr1[i] = tuple.getV1();
            arr2[i] = tuple.getV2();
        }

        return new ArrayTuple<>(arr1, arr2);
    }

    /**
     * 将当前对象转为以列表为元素的元组{@link ListTuple}类型
     *
     * @return 该方法不会返回null
     */
    public ListTuple<V1, V2> toListTuple() {
        int length = length();
        if (length <= 0) {
            return new ListTuple<>();
        }

        List<V1> list1 = new ArrayList<>(length);
        List<V2> list2 = new ArrayList<>(length);
        for (Tuple<V1, V2> tuple : array) {
            list1.add(tuple.getV1());
            list2.add(tuple.getV2());
        }
        return new ListTuple<>(list1, list2);
    }

    /**
     * 将当前对象转为Map，其中{@link Tuple}的第一个元素为key，第二个元素为value
     *
     * @return 该方法不会返回null
     */
    public Map<V1, V2> toMap() {
        Map<V1, V2> map = new LinkedHashMap<>(length());
        for (Tuple<V1, V2> tuple : array) {
            if (tuple != null && tuple.getV1() != null) {
                map.put(tuple.getV1(), tuple.getV2());
            }
        }
        return map;
    }

    public int length() {
        return array == null ? 0 : array.length;
    }

    public Tuple<V1, V2>[] getArray() {
        return array;
    }

    public void setArray(Tuple<V1, V2>[] array) {
        this.array = array;
    }

    @Override
    public String toString() {
        return "TupleArray{" + "array=" + Arrays.toString(array) + '}';
    }
}
