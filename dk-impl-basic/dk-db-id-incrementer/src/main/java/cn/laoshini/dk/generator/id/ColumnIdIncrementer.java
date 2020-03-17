package cn.laoshini.dk.generator.id;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.incrementer.AbstractColumnMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.AbstractIdentityColumnMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.MySQLMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.SqlServerMaxValueIncrementer;

import cn.laoshini.dk.common.SpringContextHolder;
import cn.laoshini.dk.constant.DBTypeEnum;
import cn.laoshini.dk.dao.SqlBuilder;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.exception.DaoException;

/**
 * 使用数据库表实现id自增器
 *
 * @author fagarine
 */
class ColumnIdIncrementer implements IDbIdIncrementer {

    private AbstractColumnMaxValueIncrementer columnIncrementer;

    ColumnIdIncrementer(String columnName) {
        DataSource dataSource;
        try {
            dataSource = SpringContextHolder.getBean(DataSource.class);
        } catch (BeansException e) {
            throw new DaoException("rdb.not.init", "找不到DataSource对象，无法通过数据库表实现id自增器");
        }

        DBTypeEnum dbType = SqlBuilder.getDBType();
        String tableName = IdIncrementerConstant.ID_INCREMENTER_TABLE;
        switch (dbType) {
            case MYSQL:
                columnIncrementer = new MySQLMaxValueIncrementer(dataSource, tableName, columnName);
                break;

            case SQL_SERVER:
                columnIncrementer = new SqlServerMaxValueIncrementer(dataSource, tableName, columnName);
                break;

            default:
                columnIncrementer = new DefaultColumnIncrementer(dataSource, tableName, columnName);
                break;
        }
        columnIncrementer.setCacheSize(cacheSize());
    }

    @Override
    public String idName() {
        return columnIncrementer.getColumnName();
    }

    @Override
    public long nextId() {
        try {
            return columnIncrementer.nextLongValue();
        } catch (DataAccessException e) {
            throw new BusinessException("id.next.fail", "通过关系数据库实现id自增失败:" + columnIncrementer.getColumnName(), e);
        }
    }

    @Override
    public int cacheSize() {
        // 默认缓存量为100
        return IdIncrementerConstant.RELATION_DB_CACHE_SIZE;
    }

    private final class DefaultColumnIncrementer extends AbstractIdentityColumnMaxValueIncrementer {

        DefaultColumnIncrementer(DataSource dataSource, String incrementerName, String columnName) {
            super(dataSource, incrementerName, columnName);
        }

        @Override
        protected String getIncrementStatement() {
            return "insert into " + getIncrementerName() + "(" + getColumnName() + " ) values(" + getIdentityStatement()
                   + ")";
        }

        @Override
        protected String getIdentityStatement() {
            return "SELECT MAX(" + getColumnName() + ") FROM " + getIncrementerName();
        }
    }
}
