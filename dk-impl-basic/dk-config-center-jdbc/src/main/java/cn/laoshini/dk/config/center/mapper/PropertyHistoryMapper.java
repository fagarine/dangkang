package cn.laoshini.dk.config.center.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import cn.laoshini.dk.config.center.entity.PropertyHistory;

/**
 * @author fagarine
 */
@Mapper
public interface PropertyHistoryMapper {

    /**
     * 获取指定应用的所有历史发布记录
     *
     * @param application 应用名
     * @return 返回所有历史记录
     */
    List<PropertyHistory> selectReleasedRecordByApplication(@Param("application") String application);

    /**
     * 获取指定进程的最后发布版本号
     *
     * @param application 应用名
     * @param profile 应用profile
     * @param label 进程标签
     * @return 返回查询结果
     */
    int selectMaxReleasedVersion(@Param("application") String application, @Param("profile") String profile,
            @Param("label") String label);

    /**
     * 根据id获取配置信息历史记录
     *
     * @param id id
     * @return 返回查询结果
     */
    PropertyHistory selectById(@Param("id") Integer id);

    /**
     * 添加一条配置信息记录
     *
     * @param history 配置信息
     * @return 返回受影响行数
     */
    int insert(PropertyHistory history);

    /**
     * 批量添加配置信息记录
     *
     * @param histories 配置信息记录
     * @return 返回受影响行数
     */
    int batchInsert(List<PropertyHistory> histories);

    /**
     * 将配置信息历史记录置为已回滚状态
     *
     * @param id 历史记录id
     * @return 返回受影响行数
     */
    int updateRolledBack(@Param("id") Integer id);
}
