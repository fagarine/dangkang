package cn.laoshini.dk.id;

import cn.laoshini.dk.exception.BusinessException;

/**
 * 使用数据库（键值对数据库或关系数据库）实现id自增器的接口
 *
 * @author fagarine
 */
interface IDbIdIncrementer {

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

    /**
     * 返回ID缓存数量，也表示返回多少个ID写一次数据库。
     * 设置缓存可有效减轻数据库压力，但停服后重启可能导致索引不连续，如果是要求索引强制连续的，缓存位1，即不用缓存
     *
     * @return 缓存数量
     */
    default int cacheSize() {
        return 1;
    }
}
