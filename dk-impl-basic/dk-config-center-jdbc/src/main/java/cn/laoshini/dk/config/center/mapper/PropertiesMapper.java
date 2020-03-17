package cn.laoshini.dk.config.center.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import cn.laoshini.dk.config.center.entity.ConfigProperty;

/**
 * @author fagarine
 */
@Mapper
public interface PropertiesMapper {

    /**
     * 获取指定应用的所有标签名（已去重）
     *
     * @param application 应用名
     * @return 返回所有标签名
     */
    List<String> selectAllLabelByApplication(@Param("application") String application);

    /**
     * 获取当前所有的配置信息（慎用）
     *
     * @return 返回所有配置信息
     */
    List<ConfigProperty> selectAllProperties();

    /**
     * 根据应用标签获取所有配置数据
     *
     * @param application 应用名
     * @param label 标签
     * @return 返回查询结果
     */
    List<ConfigProperty> selectByApplicationAndLabel(@Param("application") String application,
            @Param("label") String label);

    /**
     * 根据应用名称和profile获取所有配置数据
     *
     * @param application 应用名
     * @param profile profile
     * @return 返回查询结果
     */
    List<ConfigProperty> selectByApplicationAndProfile(@Param("application") String application,
            @Param("profile") String profile);

    /**
     * 获取指定配置源的配置数据
     *
     * @param application 应用名
     * @param profile profile
     * @param label 标签
     * @return 返回查询结果
     */
    List<ConfigProperty> selectByConfigSource(@Param("application") String application,
            @Param("profile") String profile, @Param("label") String label);

    /**
     * 获取指定配置源的配置数据，以Map形式返回
     *
     * @param application 应用名
     * @param profile profile
     * @param label 标签
     * @return 返回查询结果
     */
    @MapKey("key")
    Map<String, ConfigProperty> selectPropertyMapBySource(@Param("application") String application,
            @Param("profile") String profile, @Param("label") String label);

    List<ConfigProperty> selectBatchPropertySourceByProfiles(@Param("application") String application,
            @Param("label") String label, @Param("profiles") String profiles, @Param("keys") String keys);

    List<ConfigProperty> selectBatchPropertySourceByLabels(@Param("application") String application,
            @Param("profile") String profile, @Param("labels") String labels, @Param("keys") String keys);

    List<ConfigProperty> selectBatchSourceByProfile(@Param("application") String application,
            @Param("profile") String profile, @Param("keys") String keys);

    List<ConfigProperty> selectBatchSourceByLabel(@Param("application") String application,
            @Param("label") String label, @Param("keys") String keys);

    /**
     * 根据唯一id获取配置信息
     *
     * @param id 配置信息id
     * @return 返回查询结果
     */
    ConfigProperty selectPropertiesById(@Param("id") int id);

    /**
     * 添加一条配置信息
     *
     * @param configProperty 配置信息
     * @return 返回受影响行数
     */
    int insert(ConfigProperty configProperty);

    /**
     * 批量插入配置信息
     *
     * @param properties 配置信息
     * @return 返回插入条数
     */
    int batchInsert(List<ConfigProperty> properties);

    /**
     * 更新一条配置信息的所有字段
     *
     * @param configProperty 配置信息
     * @return 返回受影响行数
     */
    int updateFullProperty(ConfigProperty configProperty);

    /**
     * 更新配置信息的值
     *
     * @param configProperty 配置信息
     * @return 返回受影响行数
     */
    int updatePropertyValue(ConfigProperty configProperty);

    /**
     * 删除一条配置信息
     *
     * @param id 配置信息id
     * @return 返回受影响行数
     */
    int deleteById(@Param("id") long id);

    /**
     * 删除指定配置源的所有配置项信息
     *
     * @param application 应用名
     * @param profile profile
     * @param label 标签
     * @return 返回删除条数
     */
    int deleteBySource(@Param("application") String application, @Param("profile") String profile,
            @Param("label") String label);
}
