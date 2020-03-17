package cn.laoshini.dk.generator.name;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author fagarine
 */
class LevelRandomNameGeneratorRegistry {

    private static final Map<ChineseNameGenerator.RandomLevel, ILevelRandomNameGenerator> RANDOMS = new EnumMap<>(
            ChineseNameGenerator.RandomLevel.class);

    private static final ILevelRandomNameGenerator DEFAULT_RANDOM = new BasicLevelRandomNameGenerator();

    static {
        RANDOMS.put(ChineseNameGenerator.RandomLevel.BASIC, DEFAULT_RANDOM);
        RANDOMS.put(ChineseNameGenerator.RandomLevel.SIMPLE, new SimpleLevelRandomNameGenerator());
        RANDOMS.put(ChineseNameGenerator.RandomLevel.MIDDLE, new MiddleLevelRandomNameGenerator());
    }

    static void initContainer() {
        NameContainer.getInstance().initialize();
    }

    static ILevelRandomNameGenerator getGenerator(ChineseNameGenerator.RandomLevel level) {
        if (level == null) {
            return DEFAULT_RANDOM;
        }

        ILevelRandomNameGenerator random = RANDOMS.get(level);
        while (random == null && level != null) {
            level = ChineseNameGenerator.RandomLevel.getByCode(level.getCode() - 1);
            random = RANDOMS.get(level);
        }
        return random;
    }

    static void setNameLengthLimit(int nameLengthLimit) {
        for (ILevelRandomNameGenerator random : RANDOMS.values()) {
            random.setNameLimit(nameLengthLimit);
        }
    }
}
