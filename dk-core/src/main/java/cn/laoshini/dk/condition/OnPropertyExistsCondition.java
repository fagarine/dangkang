package cn.laoshini.dk.condition;

import java.util.Collection;

import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.StringUtil;

/**
 * 检查配置项是否存在的条件
 *
 * @author fagarine
 * @see ConditionalOnPropertyExists
 */
public class OnPropertyExistsCondition extends AbstractSpringCondition {

    @Override
    protected boolean match(ConditionContext context, AnnotatedTypeMetadata metadata, ConditionRecord failRecord) {
        Collection<String> propertyNames = getPropertyNames(metadata);
        boolean isMatch = true;
        if (CollectionUtil.isNotEmpty(propertyNames)) {
            for (String propertyName : propertyNames) {
                String value = context.getEnvironment().getProperty(propertyName);
                if (StringUtil.isEmptyString(value)) {
                    isMatch = false;
                    appendRecord(failRecord, propertyName + "未找到配置，条件不匹配");
                }
            }
        }
        return isMatch;
    }

    private Collection<String> getPropertyNames(AnnotatedTypeMetadata metadata) {
        String[] values = getAnnotationAttribute(metadata, ConditionalOnPropertyExists.class, "value");
        String prefix = getAnnotationAttribute(metadata, ConditionalOnPropertyExists.class, "prefix");
        String[] names = getAnnotationAttribute(metadata, ConditionalOnPropertyExists.class, "name");
        return toPropertyNames(values, prefix, names);
    }
}
