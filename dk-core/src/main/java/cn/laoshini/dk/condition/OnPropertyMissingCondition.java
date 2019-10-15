package cn.laoshini.dk.condition;

import java.util.Collection;

import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import cn.laoshini.dk.util.CollectionUtil;

/**
 * 检查指定配置项是否 不存在 的条件
 *
 * @author fagarine
 * @see ConditionalOnPropertyMissing
 */
public class OnPropertyMissingCondition extends AbstractSpringCondition {

    @Override
    protected boolean match(ConditionContext context, AnnotatedTypeMetadata metadata, ConditionRecord failRecord) {
        Collection<String> propertyNames = getPropertyNames(metadata);
        boolean isMatch = true;
        if (CollectionUtil.isNotEmpty(propertyNames)) {
            for (String propertyName : propertyNames) {
                if (context.getEnvironment().containsProperty(propertyName)) {
                    isMatch = false;
                    appendRecord(failRecord, "[" + propertyName + "] 配置项存在");
                }
            }
        }
        return isMatch;
    }

    private Collection<String> getPropertyNames(AnnotatedTypeMetadata metadata) {
        String[] values = getAnnotationAttribute(metadata, ConditionalOnPropertyMissing.class, "value");
        String prefix = getAnnotationAttribute(metadata, ConditionalOnPropertyMissing.class, "prefix");
        String[] names = getAnnotationAttribute(metadata, ConditionalOnPropertyMissing.class, "name");
        return toPropertyNames(values, prefix, names);
    }
}
