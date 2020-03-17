package cn.laoshini.dk.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import cn.laoshini.dk.annotation.FunctionDependent;
import cn.laoshini.dk.annotation.FunctionVariousWays;
import cn.laoshini.dk.dao.IEntityClassManager;
import cn.laoshini.dk.dao.IRelationalDbDaoManager;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.exception.DaoException;

/**
 * 表映射对象类（实体类）管理类
 *
 * @author fagarine
 */
@Component
@FunctionVariousWays
public class EntityClassManager implements IEntityClassManager {

    @FunctionDependent(nullable = true)
    private IRelationalDbDaoManager relationalDbDaoManager;

    /**
     * 记录表单类，key:表单名称
     */
    private Map<String, Class<?>> entityClassMap = new ConcurrentHashMap<>();

    private Map<String, Class<?>> entityClassCache = new ConcurrentHashMap<>();

    private Map<ClassLoader, Set<String>> moduleMap = new ConcurrentHashMap<>();

    /**
     * key:className, value:tableName
     */
    private Map<String, String> classTableNameMap = new ConcurrentHashMap<>();

    /**
     * 注册表与实体类的映射关系
     *
     * @param tableName 表名
     * @param tableClass 实体类
     */
    @Override
    public void registerEntityClass(String tableName, Class<?> tableClass) {
        if (tableName == null || tableClass == null) {
            throw new BusinessException("table.register.null",
                    String.format("table类和名称不能为空, tableName:%s, class:%s", tableName, tableClass));
        }

        Class existClass = entityClassMap.get(tableName);
        if (existClass != null) {
            throw new BusinessException("table.bean.duplicate",
                    String.format("table类注册重复, name:%s, exist:%s, new:%s", tableName, existClass, tableClass));
        }

        // 检查表是否存在
        checkTableIsExists(tableName);

        entityClassMap.put(tableName, tableClass);
        classTableNameMap.put(tableClass.getName(), tableName);
        moduleMap.computeIfAbsent(tableClass.getClassLoader(), cl -> new LinkedHashSet<>()).add(tableName);
    }

    private void checkTableIsExists(String tableName) {
        if (relationalDbDaoManager != null && !relationalDbDaoManager.validateTable(tableName)) {
            throw new DaoException("table.not.found", String.format("表[%s]不存在，请检查配置或创建表", tableName));
        }
    }

    /**
     * 批量注册
     *
     * @param map key:表名称，value:对应的类
     */
    @Override
    public void batchRegister(Map<String, Class<?>> map) {
        if (map == null || map.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Class<?>> entry : map.entrySet()) {
            registerEntityClass(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void prepareUnregister(ClassLoader classLoader) {
        entityClassCache.clear();

        Collection<String> names = moduleMap.remove(classLoader);
        if (names != null && !names.isEmpty()) {
            for (String name : names) {
                Class<?> clazz = entityClassMap.remove(name);
                entityClassCache.put(name, clazz);
                classTableNameMap.remove(clazz.getName());
            }
        }
    }

    @Override
    public void cancelPrepareUnregister() {
        batchRegister(entityClassCache);
        entityClassCache.clear();
    }

    @Override
    public void unregister() {
        entityClassCache.clear();
    }

    @Override
    public boolean containsClass(Class<?> clazz) {
        return entityClassMap.containsValue(clazz);
    }

    @Override
    public String getClassTableName(String className) {
        return classTableNameMap.get(className);
    }

    @Override
    public Class<?> getTableBeanClass(String tableName) {
        return entityClassMap.get(tableName);
    }

    @Override
    public List<String> getTableNames() {
        return new ArrayList<>(classTableNameMap.values());
    }
}
