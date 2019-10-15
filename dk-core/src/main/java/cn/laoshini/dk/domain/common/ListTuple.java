package cn.laoshini.dk.domain.common;

import java.util.List;

/**
 * 两个元素元素都为列表的元组类型
 *
 * @param <V1> 第一个列表的元素类型
 * @param <V2> 第二个列表的元素类型
 * @author fagarine
 */
public class ListTuple<V1, V2> extends Tuple<List<V1>, List<V2>> {

    private static final long serialVersionUID = 1L;

    public ListTuple() {
    }

    public ListTuple(List<V1> v1s, List<V2> v2s) {
        super(v1s, v2s);
    }

    @Override
    public String toString() {
        return "ListTuple{} " + super.toString();
    }
}
