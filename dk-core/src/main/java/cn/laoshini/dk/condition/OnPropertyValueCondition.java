package cn.laoshini.dk.condition;

import java.util.Collection;

import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.StringUtil;

/**
 * 检查配置项是否包含指定值的条件
 *
 * @author fagarine
 * @see ConditionalOnPropertyValue
 */
public class OnPropertyValueCondition extends AbstractSpringCondition {
    @Override
    protected boolean match(ConditionContext context, AnnotatedTypeMetadata metadata, ConditionRecord failRecord) {
        String havingValue = getStringAttribute(metadata, ConditionalOnPropertyValue.class, "havingValue");
        if (StringUtil.isEmptyString(havingValue)) {
            appendRecord(failRecord, "havingValue条件不应该为空，跳过判断，算匹配成功");
            return true;
        }

        // 配置项key
        String[] values = getAnnotationAttribute(metadata, ConditionalOnPropertyValue.class, "propertyName");
        Collection<String> propertyNames = toCollection(values);

        boolean isMatch = true;
        if (CollectionUtil.isNotEmpty(propertyNames)) {
            String match = havingValue.toLowerCase();
            boolean matchIfMissing = getAnnotationAttribute(metadata, ConditionalOnPropertyValue.class,
                    "matchIfMissing");
            for (String propertyName : propertyNames) {
                // 配置项的值
                String value = context.getEnvironment().getProperty(propertyName);
                if (value == null && matchIfMissing) {
                    isMatch = false;
                    appendRecord(failRecord, propertyName + "配置项未找到，不能匹配 " + havingValue);
                } else if (value != null && !value.toLowerCase().contains(match)) {
                    isMatch = false;
                    appendRecord(failRecord, propertyName + " 的值[" + value + "]不能匹配 " + havingValue);
                }
            }
        } else {
            appendRecord(failRecord, "propertyName条件不应该为空，跳过判断，算匹配成功");
        }
        return isMatch;
    }
}
