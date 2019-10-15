package cn.laoshini.dk.condition;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.ClassUtils;

import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.StringUtil;

/**
 * Spring组件自动配置条件匹配规则计算的抽象类
 *
 * @author fagarine
 */
public abstract class AbstractSpringCondition implements Condition {

    @Override
    public final boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String classOrMethodName = getClassOrMethodName(metadata);
        ConditionRecord record = ConditionRecord.empty();

        boolean isMatch = match(context, metadata, record);

        logOutcome(classOrMethodName, isMatch, record);
        return isMatch;
    }

    /**
     * 执行匹配逻辑
     *
     * @param context the condition context
     * @param metadata the annotation metadata
     * @param failRecord 记录匹配失败的信息
     * @return 匹配结果
     */
    protected abstract boolean match(ConditionContext context, AnnotatedTypeMetadata metadata,
            ConditionRecord failRecord);

    protected <T> T getAnnotationAttribute(AnnotatedTypeMetadata metadata, Class<? extends Annotation> annotationClass,
            String attributeName) {
        return (T) metadata.getAnnotationAttributes(annotationClass.getName()).get(attributeName);
    }

    protected String getStringAttribute(AnnotatedTypeMetadata metadata, Class<? extends Annotation> annotationClass,
            String attributeName) {
        String value = getAnnotationAttribute(metadata, annotationClass, attributeName);
        return value.trim();
    }

    protected void appendRecord(ConditionRecord record, String message) {
        record.append(new ConditionRecord(message));
    }

    protected Collection<String> toPropertyNames(String[] values, String prefix, String[] names) {
        Set<String> propertyNames = toCollection(values);
        addPrefixNames(prefix, names, propertyNames);
        return propertyNames;
    }

    protected Set<String> toCollection(String[] values) {
        Set<String> propertyNames = new LinkedHashSet<>();
        if (CollectionUtil.isNotEmpty(values)) {
            for (String name : values) {
                if (StringUtil.isNotEmptyString(name)) {
                    propertyNames.add(name.trim());
                }
            }
        }
        return propertyNames;
    }

    private void addPrefixNames(String prefix, String[] names, Set<String> propertyNames) {
        if (CollectionUtil.isNotEmpty(names)) {
            prefix = prefix == null ? "" : prefix.trim();
            for (String name : names) {
                if (StringUtil.isNotEmptyString(name)) {
                    propertyNames.add(prefix + "." + name.trim());
                }
            }
        }
    }

    private void logOutcome(String classOrMethodName, boolean isMatch, ConditionRecord record) {
        LogUtil.debug(getLogMessage(classOrMethodName, isMatch, record));
    }

    private static String getClassOrMethodName(AnnotatedTypeMetadata metadata) {
        if (metadata instanceof ClassMetadata) {
            ClassMetadata classMetadata = (ClassMetadata) metadata;
            return classMetadata.getClassName();
        }
        MethodMetadata methodMetadata = (MethodMetadata) metadata;
        return methodMetadata.getDeclaringClassName() + "#" + methodMetadata.getMethodName();
    }

    private String getLogMessage(String classOrMethodName, boolean isMatch, ConditionRecord record) {
        StringBuilder message = new StringBuilder();
        message.append("标记在类 [");
        message.append(classOrMethodName);
        message.append("] 上的条件 [");
        message.append(ClassUtils.getShortName(getClass()));
        message.append("]");
        message.append(isMatch ? " 匹配通过" : " 匹配失败");
        if (record.notEmpty()) {
            message.append("；原因： ").append(record);
        }
        return message.toString();
    }
}
