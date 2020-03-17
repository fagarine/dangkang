package cn.laoshini.dk.config.center.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author fagarine
 */
@Mapper
public interface VersionHistoryMapper {

    /**
     * 返回传入名称对应的迭代次数
     *
     * @param name name
     * @return 返回查询结果
     */
    Integer selectIterations(@Param("name") String name);

    /**
     * 增加一条版本迭代信息
     *
     * @param name name
     * @return 返回受影响行数
     */
    int insert(@Param("name") String name);

    /**
     * 版本迭代次数自增
     *
     * @param name name
     * @return 返回受影响行数
     */
    int versionIncrement(@Param("name") String name);
}
