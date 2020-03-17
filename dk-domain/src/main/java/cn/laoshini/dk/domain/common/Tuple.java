package cn.laoshini.dk.domain.common;

import java.io.Serializable;

import lombok.Data;

/**
 * 元组结构类型（包含两个元素的结构），使用泛型，避免每次需要一个包含两个元素的结构时都去新建类
 *
 * @param <V1> 第一个元素的类型
 * @param <V2> 第二个元素的类型
 * @author fagarine
 */
@Data
public class Tuple<V1, V2> implements Serializable {

    private static final long serialVersionUID = 1L;

    private V1 v1;

    private V2 v2;

    public Tuple() {
    }

    public Tuple(V1 v1, V2 v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

}
