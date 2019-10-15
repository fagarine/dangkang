package cn.laoshini.dk.constant;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 外置模块功能相关常量
 *
 * @author fagarine
 */
public class ModuleConstant {
    private ModuleConstant() {
    }

    /**
     * 系统可识别的，外置模块的配置文件后缀名
     */
    public static final String[] MODULE_CONFIG_FILE_SUFFIX = { ".properties", ".yaml", ".yml" };

    /**
     * 外置模块系统，模块配置文件名称命名规则（加载模块时，系统会去查找符合规则的配置文件，并且以先找到的配置文件中的配置信息为准）
     */
    public static final String[] MODULE_CONFIG_FILE_REG_EXP = { "^application-[A-Za-z0-9_-]+$", "^[A-Za-z0-9_-]+$" };

    public static final Set<Pattern> MODULE_CONFIG_FILE_PATTERNS = new LinkedHashSet<>(
            MODULE_CONFIG_FILE_REG_EXP.length);

    static {
        for (String regExp : MODULE_CONFIG_FILE_REG_EXP) {
            MODULE_CONFIG_FILE_PATTERNS.add(Pattern.compile(regExp));
        }
    }
}
