package cn.laoshini.dk.register;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import cn.laoshini.dk.dao.IEntityClassManager;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.function.VariousWaysManager;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.StringUtil;

/**
 * 数据库表对应的实体类注册器
 *
 * @author fagarine
 */
public interface IEntityRegister extends IFunctionRegister {

    /**
     * 获取实体类扫描器
     *
     * @return 如果要使用类扫描，该方法不应该返回null
     */
    IClassScanner<Class<?>> scanner();

    /**
     * 设置实体类扫描器
     *
     * @param entityScanner 实体类扫描器
     * @return 返回当前对象，用于fluent风格编程
     */
    IEntityRegister setScanner(IClassScanner<Class<?>> entityScanner);

    /**
     * 获取并返回实体类关联的表名称读取器
     *
     * @return 如果使用系统自动扫描和注册功能，该方法不能返回null
     */
    Function<Class<?>, String> tableNameReader();

    /**
     * 设置实体类关联的表名称读取器
     *
     * @param tableNameReader 表名称读取逻辑
     * @return 返回当前对象，用于fluent风格编程
     */
    IEntityRegister setTableNameReader(Function<Class<?>, String> tableNameReader);

    @Override
    default void action(ClassLoader classLoader) {
        IClassScanner<Class<?>> entityScanner = scanner();
        if (entityScanner != null) {
            if (tableNameReader() == null) {
                throw new BusinessException("name.reader.missing", "要使用系统自动扫描注册实体类，tableNameReader不能为空");
            }

            IEntityClassManager manager = VariousWaysManager.getCurrentImpl(IEntityClassManager.class);
            Function<Class<?>, String> tableNameReader = tableNameReader();
            List<Class<?>> classes = entityScanner.findClasses(classLoader);
            for (Class<?> clazz : classes) {
                String tableName = tableNameReader.apply(clazz);
                if (StringUtil.isNotEmptyString(tableName)) {
                    manager.registerEntityClass(tableName, clazz);
                }
            }
        }
    }

    /**
     * 用户手动注册多个实体类，使用本方法，必须保证{@link #tableNameReader()}不为空，并能通过其读取到类对应的表名
     *
     * @param entityClasses 要注册的实体类
     * @return 返回当前对象，用于fluent风格编程
     */
    default IEntityRegister registerEntityClasses(List<Class<?>> entityClasses) {
        if (CollectionUtil.isEmpty(entityClasses)) {
            return this;
        }

        IEntityClassManager manager = VariousWaysManager.getCurrentImpl(IEntityClassManager.class);
        Function<Class<?>, String> tableNameReader = tableNameReader();
        for (Class<?> clazz : entityClasses) {
            String tableName = tableNameReader.apply(clazz);
            if (StringUtil.isNotEmptyString(tableName)) {
                manager.registerEntityClass(tableName, clazz);
            }
        }
        return this;
    }

    /**
     * 用户手动注册单个注册实体类
     *
     * @param tableName 表名
     * @param entityClass 实体类
     * @return 返回当前对象，用于fluent风格编程
     */
    default IEntityRegister registerEntityClass(String tableName, Class<?> entityClass) {
        VariousWaysManager.getCurrentImpl(IEntityClassManager.class).registerEntityClass(tableName, entityClass);
        return this;
    }

    /**
     * 用户手动注册多个实体类
     *
     * @param entityClassMap 要注册的实体类, key: 表名, value: 实体类
     * @return 返回当前对象，用于fluent风格编程
     */
    default IEntityRegister registerEntityClasses(Map<String, Class<?>> entityClassMap) {
        if (CollectionUtil.isNotEmpty(entityClassMap)) {
            VariousWaysManager.getCurrentImpl(IEntityClassManager.class).batchRegister(entityClassMap);
        }
        return this;
    }

    @Override
    default String functionName() {
        return "表实体类";
    }
}
