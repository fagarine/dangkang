package cn.laoshini.dk.dao;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

import cn.laoshini.dk.annotation.FunctionDependent;
import cn.laoshini.dk.annotation.FunctionVariousWays;
import cn.laoshini.dk.dao.query.QueryUtil;
import cn.laoshini.dk.domain.common.Tuple;
import cn.laoshini.dk.domain.query.AbstractQueryCondition;
import cn.laoshini.dk.domain.query.BeanQueryCondition;
import cn.laoshini.dk.domain.query.ListQueryCondition;
import cn.laoshini.dk.domain.query.Page;
import cn.laoshini.dk.domain.query.PageQueryCondition;
import cn.laoshini.dk.exception.DaoException;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.ReflectUtil;
import cn.laoshini.dk.util.TypeUtil;

/**
 * 关系数据库数据库访问对象，默认实现类，针对单表
 *
 * @author fagarine
 */
@FunctionVariousWays(singleton = false)
public class DefaultRelationalDbDao<EntityType> implements IRelationalDbDao<EntityType> {

    /**
     * 批量操作单次最大个数
     */
    private static final int BATCH_MAX_COUNT = 1000;

    /**
     * 单次查询最大行数
     */
    private static final int SELECT_MAX_COUNT = 10000;
    private static final String LAST_INSERT_ID_FORMAT = "SELECT MAX(%s) FROM %s";
    @FunctionDependent
    private IEntityClassManager entityClassManager;
    /**
     * 表对应的实体类
     */
    private Class<EntityType> type;
    /**
     * 表中自增字段对应的变量
     */
    private Field autoIncrementField;
    private String autoIncrementColumn;
    /**
     * 表名
     */
    private String tableName;
    private JdbcTemplate jdbcTemplate;
    /**
     * 行数据转换为实体对象
     */
    private RowMapper<EntityType> rowMapper = (rs, index) -> {
        EntityType entity;
        try {
            entity = type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            LogUtil.error("实体类 [{}] 没有可访问的无参构造函数，无法创建对象", type.getName());
            return null;
        }

        Object value;
        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            String columnName = SqlBuilder.toColumnName(type, field.getName());
            try {
                if (field.getType().equals(Date.class)) {
                    // java.sql.Date对象转为java.util.Date对象
                    java.sql.Date date = rs.getDate(columnName);
                    value = date != null ? new Date(date.getTime()) : null;
                } else {
                    value = rs.getObject(columnName, field.getType());
                }
            } catch (SQLException e) {
                continue;
            }

            boolean access = field.isAccessible();
            try {
                field.setAccessible(true);
                field.set(entity, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } finally {
                field.setAccessible(access);
            }
        }
        return entity;
    };
    private ResultSetExtractor<Long> countReader = rs -> {
        if (rs.next()) {
            return rs.getLong(1);
        }
        return 0L;
    };
    private ResultSetExtractor<Number> autoIncrementIdReader = rs -> {
        if (rs.next()) {
            return rs.getObject(1, (Class<Number>) autoIncrementField.getType());
        }
        return null;
    };

    public DefaultRelationalDbDao(Class<EntityType> clazz, JdbcTemplate jdbcTemplate) {
        this.type = clazz;
        this.jdbcTemplate = jdbcTemplate;
        for (Field field : type.getDeclaredFields()) {
            if (field.isAnnotationPresent(TableKey.class) && field.getAnnotation(TableKey.class).autoIncrement()) {
                if (!Number.class.isAssignableFrom(field.getType()) && !TypeUtil.isUnpackNumberType(field.getType())) {
                    throw new DaoException("field.type.error",
                            "自增字段的类型必须为数值类型，请检查, class:" + clazz.getName() + ", field:" + field.getName() + ", type:"
                            + field.getType());
                }
                autoIncrementField = field;
                autoIncrementColumn = SqlBuilder.toColumnName(clazz, field.getName());
                break;
            }
        }
    }

    private int executeDDL(PreparedStatementCreator psc) {
        return Optional.ofNullable(jdbcTemplate.execute(psc, PreparedStatement::executeUpdate)).orElse(0);
    }

    @Override
    public int insert(EntityType data) {
        if (data == null) {
            return 0;
        }

        if (entityClassManager.containsClass(type)) {
            LogUtil.debug("收到待插入数据: {}", data);

            Tuple<String, Tuple<int[], List<Object>>> psTuple = PreparedStatementSqlBuilder.newInsertSql(data);
            PreparedStatementCreator psc = PreparedStatementBuilder.buildPsc(psTuple);
            if (psc != null) {
                LogUtil.debug("prepare execute insert sql: {}, params: {}", psTuple.getV1(), psTuple.getV2().getV2());
                if (autoIncrementField != null) {
                    // 表中id使用自增字段的，插入完成后注入id的值
                    return transactionInsert(psc, data);
                } else {
                    return executeDDL(psc);
                }
            } else {
                throw new DaoException("build.sql.fail", "创建插入SQL失败");
            }
        }
        throw new DaoException("not.table.entity", "实体类没有关联表，请检查配置:" + type.getName());
    }

    private int transactionInsert(PreparedStatementCreator psc, EntityType data) {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            connection.setAutoCommit(false);

            int row = executeDDL(psc);
            // 将新插入的自增id填入对象中
            Number autoIncrementId = null;
            if (row == 1) {
                String sql = String.format(LAST_INSERT_ID_FORMAT, autoIncrementColumn, getTableName());
                autoIncrementId = jdbcTemplate.query(sql, autoIncrementIdReader);
            }
            connection.commit();
            connection.setAutoCommit(true);

            if (autoIncrementId != null && autoIncrementId.longValue() > 0) {
                ReflectUtil.setFieldValue(data, autoIncrementField, autoIncrementId);
            }
            return row;
        } catch (SQLException e) {
            throw new DaoException("execute.insert.error", "执行插入SQL出错", e);
        }
    }

    private int doBatchInsert(List<EntityType> list) {
        Tuple<String, Tuple<int[], List<Object>>> psTuple = PreparedStatementSqlBuilder.newBatchInsertSql(list);
        PreparedStatementCreator psc = PreparedStatementBuilder.buildPsc(psTuple);
        if (psc != null) {
            LogUtil.debug("prepare execute batch insert sql: {}, params: {}", psTuple.getV1(), psTuple.getV2().getV2());
            return executeDDL(psc);
        } else {
            throw new DaoException("build.sql.fail", "创建批量插入SQL失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertList(List<EntityType> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }

        if (entityClassManager.containsClass(type)) {
            int total = list.size();
            LogUtil.debug("收到批量待插入数据，数量: {}", total);
            int succeed = 0;
            for (int start = 0; start < total; start += BATCH_MAX_COUNT) {
                int end = start + BATCH_MAX_COUNT - 1;
                if (end >= total) {
                    end = total - 1;
                }

                succeed += doBatchInsert(list.subList(start, end));
            }
            return succeed;
        }
        throw new DaoException("not.table.entity", "实体类没有关联表，请检查配置:" + type.getName());
    }

    @Override
    public int update(EntityType entity) {
        if (entity == null) {
            return 0;
        }

        if (entityClassManager.containsClass(type)) {
            LogUtil.debug("收到待更新数据: {}", entity);

            return updateEntity(entity);
        }
        throw new DaoException("not.table.entity", "实体类没有关联表，请检查配置:" + type.getName());
    }

    private int updateEntity(EntityType entity) {
        Tuple<String, Tuple<int[], List<Object>>> psTuple = PreparedStatementSqlBuilder.newUpdateSql(entity);
        PreparedStatementCreator psc = PreparedStatementBuilder.buildPsc(psTuple);
        if (psc != null) {
            LogUtil.debug("prepare execute update sql: {}, params: {}", psTuple.getV1(), psTuple.getV2().getV2());
            return executeDDL(psc);
        } else {
            throw new DaoException("build.sql.fail", "创建更新SQL失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateList(List<EntityType> entities) throws DaoException {
        if (CollectionUtil.isEmpty(entities)) {
            return 0;
        }

        if (entityClassManager.containsClass(type)) {
            LogUtil.debug("收到待批量更新数据，数量: {}", entities.size());

            int succeed = 0;
            for (EntityType entity : entities) {
                succeed += updateEntity(entity);
            }
            return succeed;
        }
        throw new DaoException("not.table.entity", "实体类没有关联表，请检查配置:" + type.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdate(Map<String, Object> updatedColumns, Map<String, Object> condition) {
        if (CollectionUtil.isEmpty(updatedColumns)) {
            return 0;
        }

        if (entityClassManager.containsClass(type)) {
            LogUtil.debug("收到批量更新请求，table: {}, 查找条件: {}, 更新内容: {}", getTableName(), condition, updatedColumns);

            Tuple<String, Tuple<int[], List<Object>>> psTuple = PreparedStatementSqlBuilder
                    .newBatchUpdateSql(getTableName(), type, condition, updatedColumns);
            PreparedStatementCreator psc = PreparedStatementBuilder.buildPsc(psTuple);
            if (psc != null) {
                LogUtil.debug("prepare execute batch update sql: {}", psTuple.getV1());
                return executeDDL(psc);
            } else {
                throw new DaoException("build.sql.fail", "创建批量更新SQL失败");
            }
        }
        throw new DaoException("not.table.entity", "实体类没有关联表，请检查配置:" + type.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int fullUpdate(Map<String, Object> updatedColumns) {
        if (CollectionUtil.isEmpty(updatedColumns)) {
            return 0;
        }

        if (entityClassManager.containsClass(type)) {
            LogUtil.debug("收到全量更新请求，table: {}, 更新内容: {}", getTableName(), updatedColumns);

            Tuple<String, Tuple<int[], List<Object>>> psTuple = PreparedStatementSqlBuilder
                    .newBatchUpdateSql(getTableName(), type, null, updatedColumns);
            PreparedStatementCreator psc = PreparedStatementBuilder.buildPsc(psTuple);
            if (psc != null) {
                LogUtil.debug("prepare execute full update sql: {}, params: {}", psTuple.getV1(),
                        psTuple.getV2().getV2());
                return executeDDL(psc);
            } else {
                throw new DaoException("build.sql.fail", "创建全量更新SQL失败");
            }
        }
        throw new DaoException("not.table.entity", "实体类没有关联表，请检查配置:" + type.getName());
    }

    @Override
    public int delete(EntityType entity) {
        if (entity == null) {
            return 0;
        }

        Tuple<String, Tuple<int[], List<Object>>> psTuple = PreparedStatementSqlBuilder.newDeleteSql(entity);
        PreparedStatementCreator psc = PreparedStatementBuilder.buildPsc(psTuple);
        if (psc != null) {
            LogUtil.debug("prepare execute delete sql: {}, params: {}", psTuple.getV1(), psTuple.getV2().getV2());
            return executeDDL(psc);
        } else {
            throw new DaoException("build.sql.fail", "创建删除SQL失败");
        }
    }

    @Override
    public EntityType selectByCondition(BeanQueryCondition condition) {
        Tuple<String, Tuple<int[], List<Object>>> psTuple = PreparedStatementSqlBuilder.newSelectSql(type, condition);
        PreparedStatementCreator psc = PreparedStatementBuilder.buildPsc(psTuple);
        if (psc != null) {
            LogUtil.debug("prepare execute single query sql: {}", psTuple.getV1());
            List<EntityType> list = jdbcTemplate.query(psc, rowMapper);
            if (list.size() > 1) {
                throw new DaoException("query.multi.result",
                        String.format("本次查询返回多条数据，请检查查询条件, condition:%s, sql:%s", condition, psTuple.getV1()));
            }
            return list.isEmpty() ? null : list.get(0);
        } else {
            throw new DaoException("build.sql.fail", "创建单条数据查询SQL失败");
        }
    }

    @Override
    public List<EntityType> selectListByCondition(ListQueryCondition condition) {
        long count = getCount(condition);
        if (count == 0) {
            return Collections.emptyList();
        }

        if (count < SELECT_MAX_COUNT) {
            return selectListBeanSimple(condition);
        }
        return selectListBeanOptimized(count, condition.getFilters());
    }

    private List<EntityType> selectListBeanSimple(ListQueryCondition condition) {
        Tuple<String, Tuple<int[], List<Object>>> psTuple = PreparedStatementSqlBuilder.newSelectSql(type, condition);
        PreparedStatementCreator psc = PreparedStatementBuilder.buildPsc(psTuple);
        if (psc != null) {
            LogUtil.debug("prepare execute query sql: {}, params: {}", psTuple.getV1(), psTuple.getV2().getV2());
            return jdbcTemplate.query(psc, rowMapper);
        } else {
            throw new DaoException("build.sql.fail", "创建批量查询SQL失败");
        }
    }

    private List<EntityType> selectListBeanOptimized(long count, Map<String, Object> filters) {
        List<EntityType> result = new LinkedList<>();
        PageQueryCondition condition = new PageQueryCondition();
        condition.setFilters(filters);
        for (int i = 0; count > 0; i++) {
            condition.setPageSize(SELECT_MAX_COUNT);
            condition.setPageNo(i);
            count -= SELECT_MAX_COUNT;

            result.addAll(selectByPageCondition(condition));
        }
        return result;
    }

    private long getCount(AbstractQueryCondition condition) {
        Tuple<String, Tuple<int[], List<Object>>> psTuple = PreparedStatementSqlBuilder.newCountSql(type, condition);
        PreparedStatementCreator psc = PreparedStatementBuilder.buildPsc(psTuple);
        if (psc != null) {
            LogUtil.debug("prepare execute count sql: {}", psTuple.getV1());
            Long totalCount = jdbcTemplate.query(psc, new ResultSetExtractor<Long>() {
                @Override
                public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                    return 0L;
                }
            });

            return totalCount == null ? 0 : totalCount;
        } else {
            throw new DaoException("build.sql.fail", "创建批量查询SQL失败");
        }

    }

    @Override
    public Page<EntityType> selectByPage(PageQueryCondition condition) {
        long count = getCount(condition);
        if (count == 0) {
            return new Page<>(condition.getPageNo(), condition.getPageSize(), 0, Collections.emptyList());
        }
        return new Page<>(condition.getPageNo(), condition.getPageSize(), count, selectByPageCondition(condition));
    }

    private List<EntityType> selectByPageCondition(PageQueryCondition condition) {
        Tuple<String, Tuple<int[], List<Object>>> psTuple = PreparedStatementSqlBuilder
                .newPageQuerySql(type, condition);
        PreparedStatementCreator psc = PreparedStatementBuilder.buildPsc(psTuple);
        if (psc != null) {
            LogUtil.debug("prepare execute page query sql: {}, params: {}", psTuple.getV1(), psTuple.getV2().getV2());
            return jdbcTemplate.query(psc, rowMapper);
        } else {
            throw new DaoException("build.sql.fail", "创建分页查询SQL失败");
        }
    }

    @Override
    public List<EntityType> selectAll() throws DaoException {
        return selectListByCondition(QueryUtil.newListQueryCondition());
    }

    private long getCount() {
        String countSql = SqlBuilder.buildCountSql(getTableName());
        LogUtil.debug("执行统计SQL: {}", countSql);
        Long totalCount = jdbcTemplate.query(countSql, countReader);

        return totalCount == null ? 0 : totalCount;
    }

    @Override
    public Class<EntityType> getType() {
        return type;
    }

    public void setType(Class<EntityType> type) {
        this.type = type;
    }

    @Override
    public String getTableName() {
        if (tableName == null) {
            tableName = entityClassManager.getClassTableName(type.getName());
        }
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    private static class PreparedStatementBuilder {

        static PreparedStatementCreator buildPsc(String sql, Tuple<int[], List<Object>> params) {
            if (sql == null) {
                return null;
            }

            return new PreparedStatementCreatorFactory(sql, params.getV1()).newPreparedStatementCreator(params.getV2());
        }

        static PreparedStatementCreator buildPsc(Tuple<String, Tuple<int[], List<Object>>> psTuple) {
            if (psTuple == null || psTuple.getV1() == null) {
                return null;
            }

            return new PreparedStatementCreatorFactory(psTuple.getV1(), psTuple.getV2().getV1())
                    .newPreparedStatementCreator(psTuple.getV2().getV2());
        }
    }
}
