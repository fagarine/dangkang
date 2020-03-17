package cn.laoshini.dk.text.sensitive;

import org.springframework.stereotype.Component;

import cn.laoshini.dk.annotation.FunctionVariousWays;

/**
 * 默认敏感词处理器
 *
 * @author fagarine
 */
@Component
@FunctionVariousWays(description = "默认敏感词处理器")
public final class DefaultSensitiveWordProcessor extends AbstractSensitiveWordProcessor {

}
