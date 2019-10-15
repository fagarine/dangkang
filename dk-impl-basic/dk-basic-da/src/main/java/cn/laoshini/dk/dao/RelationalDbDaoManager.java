package cn.laoshini.dk.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import cn.laoshini.dk.annotation.FunctionVariousWays;
import cn.laoshini.dk.condition.ConditionalOnPropertyExists;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.exception.DaoException;
import cn.laoshini.dk.function.VariousWaysManager;
import cn.laoshini.dk.util.StringUtil;

/**
 * 关系数据库数据访问对象管理，仅在开启配置且DataSource已创建的情况下创建实例
 *
 * @author fagarine
 */
@Component
@FunctionVariousWays
@ConditionalOnPropertyExists(prefix = "dk.rdb", name = { "driver", "url", "username", "password" })
public class RelationalDbDaoManager implements IRelationalDbDaoManager {

    @Resource(name = "innerGameJdbcTemplate")
    private JdbcTemplate innerGameJdbcTemplate;

    /**
     * 记录表对应的DAO，key: 表名, value: 对应的DAO实例
     */
    private Map<String, IRelationalDbDao> relationalDbDaoMap = new ConcurrentHashMap<>();

    @Override
    public <EntityType> IRelationalDbDao<EntityType> getValidDbDao(String tableName, Class<EntityType> clazz) {
        if (StringUtil.isEmptyString(tableName) || clazz == null) {
            throw new BusinessException("table.register.null", String.format("表名[%s]和类[%s]不能为空", tableName, clazz));
        }

        IRelationalDbDao dao = relationalDbDaoMap.get(tableName);
        if (dao == null) {
            dao = VariousWaysManager.getFunctionCurrentImpl(IRelationalDbDao.class, clazz, innerGameJdbcTemplate);
            relationalDbDaoMap.put(tableName, dao);
        } else if (!clazz.equals(dao.getType())) {
            throw new DaoException("table.dao.conflict",
                    String.format("DAO实体类对应的表冲突, table:%s, 已注册类:%s, 期望类:%s", tableName, dao.getType(), clazz));
        }
        //noinspection unchecked
        return dao;
    }

    @Override
    public boolean validateTable(String tableName) {
        String sql = SqlBuilder.buildValidateTableSql(tableName);
        try {
            innerGameJdbcTemplate.execute(sql);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
