package cn.laoshini.dk.id;

import cn.laoshini.dk.annotation.ConfigurableFunction;
import cn.laoshini.dk.exception.BusinessException;

/**
 * id自增功能的接口定义
 * <p>
 * 特别需要注意的是，该类的实现类的构造方法中，必须有一个指向{@link #idName()}方法返回值相同参数的构造方法
 * </p>
 *
 * @author fagarine
 */
@ConfigurableFunction(key = "dk.id.incrementer", description = "id自增器")
public interface IIdIncrementer {

    /**
     * 返回id自增器实例对应id的名称
     *
     * @return 实现类应该保证该方法不会返回null
     */
    String idName();

    /**
     * 自增并返回下一个id
     *
     * @return 正整数
     * @throws BusinessException 所有异常都封装为BusinessException抛出
     */
    long nextId() throws BusinessException;
}
