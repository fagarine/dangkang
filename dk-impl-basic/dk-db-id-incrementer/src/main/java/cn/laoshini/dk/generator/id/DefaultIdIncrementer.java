package cn.laoshini.dk.generator.id;

import cn.laoshini.dk.annotation.FunctionVariousWays;
import cn.laoshini.dk.common.SpringContextHolder;
import cn.laoshini.dk.exception.BusinessException;

/**
 * 项目内部关于id自增器的默认实现（使用数据库实现）
 *
 * @author fagarine
 */
@FunctionVariousWays(singleton = false)
public class DefaultIdIncrementer implements IIdIncrementer {

    private static volatile Boolean useRdb;

    private IDbIdIncrementer dbIdIncrementer;

    public DefaultIdIncrementer(String idName) {
        this.dbIdIncrementer = createDbIdIncrementer(idName);
    }

    private static boolean usedRdb() {
        try {
            SpringContextHolder.getBean("innerGameDataSourceConfig");
        } catch (Exception e) {
            // 进入这里，说明找不到关系数据库配置，表示没有使用关系数据库
            return false;
        }
        return true;
    }

    private IDbIdIncrementer createDbIdIncrementer(String idName) {
        if (useRdb == null) {
            synchronized (DefaultIdIncrementer.class) {
                if (useRdb == null) {
                    useRdb = usedRdb();
                }
            }
        }

        if (useRdb) {
            return new ColumnIdIncrementer(idName);
        } else {
            return new PairIdIncrementer(idName);
        }
    }

    @Override
    public String idName() {
        return dbIdIncrementer.idName();
    }

    @Override
    public long nextId() throws BusinessException {
        return dbIdIncrementer.nextId();
    }
}
