package cn.laoshini.dk.generator.name;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import cn.laoshini.dk.annotation.FunctionVariousWays;
import cn.laoshini.dk.exception.BusinessException;

/**
 * 中文名称生成器，本类实现了名字生成等级规则，详见{@link RandomLevel}
 *
 * @author fagarine
 */
@Component
@FunctionVariousWays(value = "cn", description = "中文名称生成器")
public class ChineseNameGenerator implements INameGenerator {

    /**
     * 名字（不包含姓氏）最大长度
     */
    private static final int DEFAULT_NAME_LENGTH_LIMIT = 2;
    private int nameLengthLimit = DEFAULT_NAME_LENGTH_LIMIT;
    private RandomLevel level = RandomLevel.MIDDLE;
    private ILevelRandomNameGenerator generator;

    @PostConstruct
    public void initialize() {
        LevelRandomNameGeneratorRegistry.initContainer();
        LevelRandomNameGeneratorRegistry.setNameLengthLimit(nameLengthLimit);
        generator = LevelRandomNameGeneratorRegistry.getGenerator(level);
    }

    @Override
    public String newName(String prefix, int length) {
        return generator.newName(prefix, length);
    }

    @Override
    public List<String> batchName(String prefix, int length, int count) {
        return generator.batchName(prefix, length, count);
    }

    @Override
    public void setNameLengthLimit(int limit) {
        this.nameLengthLimit = limit;
        LevelRandomNameGeneratorRegistry.setNameLengthLimit(limit);
    }

    @Override
    public int nameLengthLimit() {
        return nameLengthLimit;
    }

    @Override
    public void setLevel(Level level) {
        if (level instanceof RandomLevel) {
            this.level = (RandomLevel) level;
            generator = LevelRandomNameGeneratorRegistry.getGenerator(this.level);
        } else {
            throw new BusinessException("not.supported.level", "不可识别的等级类型:" + level);
        }
    }

    /**
     * 名字生成规则等级枚举
     */
    public enum RandomLevel implements INameGenerator.Level {

        /**
         * 基础版，姓氏和名字在随机时，采用完全随机
         */
        BASIC(1),

        /**
         * 简单版，姓氏在全国排名前百的被随机到概率更高
         */
        SIMPLE(2),

        /**
         * 中级版，在{@link #SIMPLE}的基础上，名字（不含姓氏）部分也加大常见名字被随机到的概率
         */
        MIDDLE(3),

        /**
         * 高级版，使生成的名字更接近人们常见的（暂未实现）
         */
        HIGH(4),

        /**
         * 升级版，在{@link #HIGH}的基础上，再加入姓名学等数据，使生成的名字好听且有寓意（暂未实现）
         */
        UPGRADE(5);

        private int code;

        RandomLevel(int code) {
            this.code = code;
        }

        public static RandomLevel getByCode(int code) {
            for (RandomLevel level : RandomLevel.values()) {
                if (level.getCode() == code) {
                    return level;
                }
            }
            return null;
        }

        public int getCode() {
            return code;
        }
    }
}
