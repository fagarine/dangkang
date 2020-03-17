package cn.laoshini.dk.config.center.service;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.laoshini.dk.config.center.annotation.Label;
import cn.laoshini.dk.config.center.annotation.Profile;
import cn.laoshini.dk.config.center.annotation.Property;
import cn.laoshini.dk.config.center.constant.PropertiesConstant;
import cn.laoshini.dk.config.center.constant.PropertyOperation;
import cn.laoshini.dk.config.center.constant.PropertyStatus;
import cn.laoshini.dk.config.center.domain.PropertyField;
import cn.laoshini.dk.config.center.domain.PropertySource;
import cn.laoshini.dk.config.center.domain.UpdatedProperties;
import cn.laoshini.dk.config.center.entity.ConfigProperty;
import cn.laoshini.dk.config.center.entity.PropertyChange;
import cn.laoshini.dk.config.center.entity.PropertyHistory;
import cn.laoshini.dk.config.center.mapper.PropertiesMapper;
import cn.laoshini.dk.config.center.mapper.PropertyHistoryMapper;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.ReflectUtil;
import cn.laoshini.dk.util.StringUtil;

/**
 * @author fagarine
 */
@Service
public class PropertiesService {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    @Resource
    private PropertiesMapper propertiesMapper;
    @Resource
    private PropertyHistoryMapper propertyHistoryMapper;
    @Resource
    private VersionService versionService;
    /**
     * 记录（缓存）类中第一个被{@link Label}标记了的字段
     */
    private Map<Class<?>, Field> labelFieldMap = new ConcurrentHashMap<>();
    /**
     * 记录（缓存）类中第一个被{@link Profile}标记了的字段
     */
    private Map<Class<?>, Field> profileFieldMap = new ConcurrentHashMap<>();
    /**
     * 记录（缓存）类中所有被{@link Property}标记了的字段
     */
    private Map<Class<?>, List<PropertyField>> propertyFieldsMap = new ConcurrentHashMap<>();

    /**
     * 以String格式返回Field的值
     *
     * @param field
     * @param object
     * @return 可能返回null
     */
    public static String getFieldWithString(Field field, Object object) {
        if (field != null && object != null) {
            boolean accessible = field.isAccessible();
            try {
                field.setAccessible(true);
                Object value = field.get(object);
                // 特殊类型处理
                return getStringValue(value);
            } catch (IllegalAccessException e) {
                LogUtil.error(e, String.format("反射获取参数信息失败, field:%s, object:%s", field, object));
            } finally {
                field.setAccessible(accessible);
            }
        }
        return null;
    }

    private static String getStringValue(Object value) {
        if (value == null) {
            return "";
        } else if (value instanceof Date) {
            return new SimpleDateFormat(DATE_FORMAT).format(value);
        } else if (value instanceof Collection || value instanceof Map) {
            return JSON.toJSONString(value);
        }
        return String.valueOf(value);
    }

    /**
     * 获取指定配置源的配置数据
     *
     * @param application 应用名
     * @param profile profile
     * @param label 标签
     * @return 返回查询结果
     */
    public List<ConfigProperty> getConfigSource(String application, String profile, String label) {
        return propertiesMapper.selectByConfigSource(application, profile, label);
    }

    public PropertySource getPropertySource(String application, String profile, String label) {
        PropertySource propertySource = new PropertySource(application, profile, label);
        List<ConfigProperty> configProperties = getConfigSource(application, profile, label);
        if (CollectionUtil.isNotEmpty(configProperties)) {
            propertySource.setPropertiesMap(new HashMap<>(configProperties.size()));
            for (ConfigProperty property : configProperties) {
                propertySource.getPropertiesMap().put(property.getKey(), property.getValue());
            }
        }
        return propertySource;
    }

    /**
     * 获取指定应用名和标签的配置数据
     *
     * @param application 应用名
     * @param label 标签
     * @return 返回查询结果
     */
    public List<ConfigProperty> getByApplicationAndLabel(String application, String label) {
        return propertiesMapper.selectByApplicationAndProfile(application, label);
    }

    /**
     * 获取指定应用名和标签的配置数据
     *
     * @param application 应用名
     * @param label 标签
     * @return 返回查询结果
     */
    public Map<String, Map<String, String>> getMapByApplicationAndLabel(String application, String label) {
        List<ConfigProperty> properties = getByApplicationAndLabel(application, label);
        if (CollectionUtil.isNotEmpty(properties)) {
            Map<String, Map<String, String>> map = new LinkedHashMap<>();
            for (ConfigProperty config : properties) {
                map.computeIfAbsent(config.getProfile(), p -> new LinkedHashMap<>())
                        .put(config.getKey(), config.getValue());
            }
            return map;
        }
        return Collections.emptyMap();
    }

    /**
     * 更新指定应用单个配置源的配置信息
     * 注意：调用该方法，必须保证传入的对象中包含标注了{@link Property}注解的字段，否则，不会执行保存操作
     * 另外：由于数据保存前使用了Map记录要保存的配置项，如果存在同样的配置项key，后出现的将覆盖先出现的
     *
     * @param application 应用名
     * @param profile profile
     * @param object 包含有需要更新的配置项的对象
     */
    public <P> void putSinglePropertySource(String application, String profile, P object) {
        putSinglePropertySource(application, profile, PropertiesConstant.APPLICATION_DEFAULT_LABEL, object);
    }

    public <P> void putSinglePropertySource(String application, String profile, String label, P object) {
        if (application == null || object == null) {
            LogUtil.info("传入参数不完整，跳过更新");
            return;
        }

        UpdatedProperties updatedProperties = getUpdatedPropertiesField(object);
        if (!updatedProperties.hasProperties()) {
            LogUtil.error("没有找到需要更新的配置项，跳过更新");
            return;
        }

        if (StringUtil.isNotEmptyString(updatedProperties.getLabel())) {
            label = updatedProperties.getLabel();
        }

        if (StringUtil.isNotEmptyString(updatedProperties.getProfile())) {
            profile = updatedProperties.getProfile();
        }
        replaceSinglePropertySource(application, profile, label, updatedProperties.getPropertiesMap());
    }

    public void putSinglePropertySource(PropertySource propertySource) {
        replaceSinglePropertySource(propertySource.getApplication(), propertySource.getProfile(),
                propertySource.getLabel(), propertySource.getPropertiesMap());
    }

    public void replaceSinglePropertySource(String application, String profile, String label,
            Map<String, ?> propertyMap) {
        replaceSinglePropertySource(application, profile, label, propertyMap, false);
    }

    /**
     * 使用传入的配置项，替换数据库中的数据（如果数据库中没有，则创建新的记录），这些配置项同属于同一配置源（前三个参数指向一个唯一的配置源）
     *
     * @param application 应用名
     * @param profile 应用profile
     * @param label 应用标签
     * @param propertyMap 需要更新的配置项
     * @param part 是否是部分更新传入的配置项，如果为否（即全量更新），则数据库中的配置项，不包含在传入配置项中的，将被删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void replaceSinglePropertySource(String application, String profile, String label,
            Map<String, ?> propertyMap, boolean part) {
        if (StringUtil.isEmptyString(application) || StringUtil.isEmptyString(label) || CollectionUtil
                .isEmpty(propertyMap)) {
            LogUtil.info("传入参数不完整，跳过配置信息更新");
            return;
        }

        ConfigProperty configProperty;
        PropertyHistory history = new PropertyHistory(application, profile, label);
        history.setVersion(nextVersion(application, profile, label));
        history.setContent(new ArrayList<>(propertyMap.size()));
        // 暂不做用户系统
        history.setOperator("system");
        Map<String, ConfigProperty> configs = propertiesMapper.selectPropertyMapBySource(application, profile, label);
        for (Map.Entry<String, ?> entry : propertyMap.entrySet()) {
            if (entry.getValue() == null) {
                // 值为null的配置项跳过更新
                continue;
            }

            configProperty = configs.remove(entry.getKey());
            String value = getStringValue(entry.getValue());
            PropertyChange change = new PropertyChange(entry.getKey(), value);
            if (configProperty == null) {
                configProperty = createConfigProperty(entry.getKey(), value, application, profile, label);
                change.setOldValue("");
                change.setOperation(PropertyOperation.ADDED.name());
            } else {
                change.setOldValue(configProperty.getValue());
                change.setOperation(PropertyOperation.MODIFIED.name());

                configProperty.setValue(value);
                propertiesMapper.updatePropertyValue(configProperty);
            }
            change.setPid(configProperty.getId());
            history.getContent().add(change);
        }
        if (!part && !configs.isEmpty()) {
            for (ConfigProperty config : configs.values()) {
                history.getContent().add(new PropertyChange(config.getKey(), config.getId(), config.getValue(), "",
                        PropertyOperation.DELETED.name()));
                propertiesMapper.deleteById(config.getId());
            }
        }
        propertyHistoryMapper.insert(history);
    }

    public void batchUpdateAssignedPropertiesByProfiles(String application, String label,
            Map<String, Object> properties) {
        batchUpdateAssignedPropertiesByProfiles(application, label, null, properties);
    }

    /**
     * 批量更新指定应用，指定label下所有进程的指定配置项（用于对同一服务器在多个不同环境下的，多个配置项的批量更新）
     *
     * @param application 应用名
     * @param label 应用标签
     * @param profiles 本次要更新的profile，如果为空或空集合，表示更新所有profile
     * @param properties 本次要更新的配置项和其值
     */
    public void batchUpdateAssignedPropertiesByProfiles(String application, String label, List<String> profiles,
            Map<String, Object> properties) {
        if (StringUtil.isEmptyString(application) || StringUtil.isEmptyString(label) || CollectionUtil
                .isEmpty(properties)) {
            LogUtil.info("传入参数不完整，跳过指定参数的批量更新");
            return;
        }

        String keysSql = StringUtil.appendSqlCondition(properties.keySet());
        List<ConfigProperty> configs;
        if (CollectionUtil.isEmpty(profiles)) {
            configs = propertiesMapper.selectBatchSourceByLabel(application, label, keysSql);
        } else {
            String profileSql = StringUtil.appendSqlCondition(profiles);
            configs = propertiesMapper.selectBatchPropertySourceByProfiles(application, label, profileSql, keysSql);
        }

        updateProperties(application, false, properties, configs);
    }

    /**
     * 批量更新指定应用，指定profile下所有进程的指定配置项（用于对同一环境下，多个不同label的进程，多个配置项的批量更新）
     *
     * @param application 应用名
     * @param profile profile
     * @param labels 本次要更新的应用标签，如果为空或空集合，表示更新所有label
     * @param properties 本次要更新的配置项和其值
     */
    public void batchUpdateAssignedPropertiesByLabels(String application, String profile, List<String> labels,
            Map<String, Object> properties) {
        if (StringUtil.isEmptyString(application) || StringUtil.isEmptyString(profile) || CollectionUtil
                .isEmpty(properties)) {
            LogUtil.info("传入参数不完整，跳过指定参数的批量更新");
            return;
        }

        String keysSql = StringUtil.appendSqlCondition(properties.keySet());
        List<ConfigProperty> configs;
        if (CollectionUtil.isEmpty(labels)) {
            configs = propertiesMapper.selectBatchSourceByProfile(application, profile, keysSql);
        } else {
            String labelSql = StringUtil.appendSqlCondition(labels);
            configs = propertiesMapper.selectBatchPropertySourceByLabels(application, profile, labelSql, keysSql);
        }

        updateProperties(application, true, properties, configs);
    }

    /**
     * 更新指定配置项信息到数据库
     *
     * @param application 应用名
     * @param keyIsLabel 历史记录Map是否使用label作为key，否则使用profile作为key
     * @param properties 要更新的配置项信息
     * @param configs 数据库中已存在的配置项对应信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateProperties(String application, boolean keyIsLabel, Map<String, Object> properties,
            List<ConfigProperty> configs) {
        if (CollectionUtil.isEmpty(configs)) {
            LogUtil.error("未找到需要更新的配置项信息，跳过批量更新");
            return;
        }

        Map<String, PropertyHistory> historyMap = new HashMap<>();
        for (ConfigProperty config : configs) {
            String newValue = getStringValue(properties.remove(config.getKey()));
            String historyMapKey = keyIsLabel ? config.getLabel() : config.getProfile();
            historyMap.computeIfAbsent(historyMapKey,
                    k -> new PropertyHistory(application, config.getProfile(), config.getLabel(),
                            nextVersion(application, config.getProfile(), config.getLabel()), properties.size(),
                            "system")).getContent()
                    .add(new PropertyChange(config.getKey(), config.getId(), config.getValue(), newValue,
                            PropertyOperation.MODIFIED.name()));
            config.setValue(newValue);
            propertiesMapper.updatePropertyValue(config);
        }
        if (!properties.isEmpty()) {
            for (Map.Entry<String, Object> kv : properties.entrySet()) {
                for (PropertyHistory history : historyMap.values()) {
                    ConfigProperty config = createConfigProperty(kv.getKey(), getStringValue(kv.getValue()),
                            application, history.getProfile(), history.getLabel());
                    history.getContent().add(new PropertyChange(config.getKey(), config.getId(), "", config.getValue(),
                            PropertyOperation.ADDED.name()));
                }
            }
        }
        propertyHistoryMapper.batchInsert(new ArrayList<>(historyMap.values()));
    }

    /**
     * 批量写入指定应用指定profile下的所有进程配置信息
     *
     * @param application 应用名
     * @param label 应用标签
     * @param properties 配置信息，key为区分进程标识的profile
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchPutAssignedLabelProperties(String application, String label,
            Map<String, Map<String, Object>> properties) {
        if (StringUtil.isEmptyString(application) || StringUtil.isEmptyString(label) || CollectionUtil
                .isEmpty(properties)) {
            LogUtil.info("传入参数不完整，跳过批量更新");
            return;
        }

        PropertyHistory history;
        Map<String, PropertyHistory> historyMap = new HashMap<>(properties.size());
        List<ConfigProperty> configs = getByApplicationAndLabel(application, label);
        for (ConfigProperty config : configs) {
            Map<String, Object> map = properties.get(config.getProfile());
            if (map == null) {
                // 本次没有写入该进程的配置信息，跳过
                continue;
            }
            history = historyMap.computeIfAbsent(config.getProfile(),
                    p -> new PropertyHistory(application, p, label, nextVersion(application, p, label), map.size(),
                            "system"));
            if (!map.containsKey(config.getKey())) {
                history.getContent().add(new PropertyChange(config.getKey(), config.getId(), config.getValue(), "",
                        PropertyOperation.DELETED.name()));
                propertiesMapper.deleteById(config.getId());
            } else {
                String newValue = getStringValue(map.remove(config.getKey()));
                history.getContent()
                        .add(new PropertyChange(config.getKey(), config.getId(), config.getValue(), newValue,
                                PropertyOperation.MODIFIED.name()));
                config.setValue(newValue);
                propertiesMapper.updatePropertyValue(config);
            }
        }
        for (Map.Entry<String, Map<String, Object>> entry : properties.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                history = historyMap.computeIfAbsent(entry.getKey(), p -> new PropertyHistory(application, p, label,
                        versionService.nextVersionIterations(versionName(application, p, label)),
                        entry.getValue().size(), "system"));
                for (Map.Entry<String, Object> kv : entry.getValue().entrySet()) {
                    ConfigProperty config = createConfigProperty(kv.getKey(), getStringValue(kv.getValue()),
                            application, entry.getKey(), label);
                    history.getContent().add(new PropertyChange(config.getKey(), config.getId(), "", config.getValue(),
                            PropertyOperation.ADDED.name()));
                }
            }
        }
        propertyHistoryMapper.batchInsert(new ArrayList<>(historyMap.values()));
    }

    /**
     * 根据配置信息历史记录，回滚到上一个版本（该方法只能对当前最后一次操作进行回滚）
     *
     * @param historyId 历史记录id
     */
    @Transactional(rollbackFor = Exception.class)
    public void rollbackProperties(int historyId) {
        PropertyHistory history = propertyHistoryMapper.selectById(historyId);
        if (history == null) {
            throw new BusinessException("property.history.id", "找不到配置项历史记录, id:" + historyId);
        }

        if (PropertyStatus.ROLLED_BACK.name().equals(history.getStatus())) {
            throw new BusinessException("property.history.status", "配置项历史记录已执行过回滚操作, id:" + historyId);
        }

        int maxVersion = propertyHistoryMapper
                .selectMaxReleasedVersion(history.getApplication(), history.getProfile(), history.getLabel());
        if (history.getVersion() < maxVersion) {
            throw new BusinessException("property.history.version", "需要先对最后发布的配置项历史记录执行回滚操作, id:" + historyId);
        }

        ConfigProperty configProperty = new ConfigProperty();
        for (PropertyChange change : history.getContent()) {
            if (PropertyOperation.ADDED.name().equals(change.getOperation())) {
                propertiesMapper.deleteById(change.getPid());
            } else if (PropertyOperation.MODIFIED.name().equals(change.getOperation())) {
                configProperty.setId(change.getPid());
                configProperty.setValue(change.getOldValue());
                propertiesMapper.updatePropertyValue(configProperty);
            } else {
                createConfigProperty(change.getKey(), change.getOldValue(), history.getApplication(),
                        history.getProfile(), history.getLabel());
            }
        }

        propertyHistoryMapper.updateRolledBack(historyId);
    }

    /**
     * 创建一条配置项数据并插入数据库
     *
     * @return 返回配置项信息
     */
    private ConfigProperty createConfigProperty(String key, String value, String application, String profile,
            String label) {
        ConfigProperty configProperty = new ConfigProperty();
        configProperty.setKey(key);
        configProperty.setValue(value);
        configProperty.setApplication(application);
        configProperty.setProfile(profile);
        configProperty.setLabel(label);
        propertiesMapper.insert(configProperty);
        return configProperty;
    }

    /**
     * 从传入对象中获取需要更新的配置项相关信息
     *
     * @param object 记录配置项信息的对象
     * @return 返回对象中所有需要更新的配置项信息
     */
    private UpdatedProperties getUpdatedPropertiesField(Object object) {
        Field tempField;
        String label = null;
        String profile = null;
        Class clazz = object.getClass();

        // 获取配置项的label，如果有多个字段，使用最先找到的字段
        if ((tempField = getLabelKeyField(clazz)) != null) {
            label = getFieldWithString(tempField, object);
        }

        // 获取配置项的profile，如果有多个字段，使用最先找到的字段
        if ((tempField = getProfileKeyField(clazz)) != null) {
            profile = getFieldWithString(tempField, object);
        }

        // 获取需要更新的配置项
        Map<String, String> propertiesMap = getPropertiesFromObject(object);

        return new UpdatedProperties(profile, label, propertiesMap);
    }

    /**
     * 获取对象中的配置项数据
     *
     * @param object 记录配置项信息的对象
     * @return 获取对象中所有的配置项
     */
    private Map<String, String> getPropertiesFromObject(Object object) {
        Map<String, String> properties = new HashMap<>();
        List<PropertyField> propertyFields = getPropertyFields(object.getClass());
        if (CollectionUtil.isNotEmpty(propertyFields)) {
            for (PropertyField propertyField : propertyFields) {
                String value = propertyField.getValueWithString(object);
                if (value != null) {
                    // 跳过值为null的字段
                    properties.put(propertyField.getPropertyKey(), value);
                }
            }
        }
        return properties;
    }

    /**
     * 获取类中带有{@link Label}注解的字段，并返回第一个带有该注解的字段
     *
     * @param clazz
     * @return
     */
    private Field getLabelKeyField(Class<?> clazz) {
        return labelFieldMap.computeIfAbsent(clazz, type -> {
            List<Field> fields = ReflectUtil.getAssignedAnnotationFields(clazz, Label.class);
            if (CollectionUtil.isNotEmpty(fields)) {
                return fields.get(0);
            }
            return null;
        });
    }

    /**
     * 获取类中带有{@link Profile}注解的字段，并返回第一个带有该注解的字段
     *
     * @param clazz
     * @return
     */
    private Field getProfileKeyField(Class<?> clazz) {
        return profileFieldMap.computeIfAbsent(clazz, type -> {
            List<Field> fields = ReflectUtil.getAssignedAnnotationFields(clazz, Profile.class);
            if (!CollectionUtil.isNotEmpty(fields)) {
                return fields.get(0);
            }
            return null;
        });
    }

    /**
     * 获取类中所有带有{@link Property}注解的字段相关信息
     *
     * @param clazz
     * @return
     */
    private List<PropertyField> getPropertyFields(Class<?> clazz) {
        if (clazz != null) {
            List<PropertyField> propertyFields = propertyFieldsMap.get(clazz);
            if (propertyFields == null) {
                List<Field> fields = ReflectUtil.getAssignedAnnotationFields(clazz, Property.class);
                propertyFields = new ArrayList<>(fields.size());
                for (Field field : fields) {
                    propertyFields.add(new PropertyField(field, clazz));
                }
                propertyFieldsMap.put(clazz, propertyFields);
            }
            return propertyFields;
        }
        return Collections.emptyList();
    }

    private int nextVersion(String application, String profile, String label) {
        return versionService.nextVersionIterations(versionName(application, profile, label));
    }

    private String versionName(String application, String profile, String label) {
        return application + "_" + profile + "_" + label;
    }
}
