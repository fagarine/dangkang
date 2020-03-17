package cn.laoshini.dk.generator.name;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;

import cn.laoshini.dk.util.StringUtil;

/**
 * @author fagarine
 */
abstract class AbstractLevelRandomNameGenerator implements ILevelRandomNameGenerator {

    private int nameLimit;

    /**
     * 记录常用字的总权重，用于随机
     */
    private int totalWight;

    @Override
    public String newName(String prefix, int maxLen) {
        Surname surname = getValidSurname(prefix);
        return surname.getSurname() + randomName(getNameLen(maxLen, surname));
    }

    @Override
    public List<String> batchName(String prefix, int maxLen, int count) {
        List<String> names = new ArrayList<>(count);

        Surname surname = null;
        if (StringUtil.isNotEmptyString(prefix)) {
            surname = getSurname(prefix);
        }

        int nameLen;
        boolean randomSurname = false;
        if (surname == null) {
            randomSurname = true;
        }

        for (int c = 0; c < count; c++) {
            if (randomSurname) {
                surname = randomSurname();
            }
            nameLen = getNameLen(maxLen, surname);
            names.add(surname.getSurname() + randomName(nameLen));
        }
        return names;
    }

    /**
     * 随机一个姓
     *
     * @return 返回随机到的姓氏
     */
    abstract Surname randomSurname();

    Surname getValidSurname(String prefix) {
        Surname surname = null;
        if (StringUtil.isNotEmptyString(prefix)) {
            surname = getSurname(prefix);
        }
        if (surname == null) {
            surname = randomSurname();
        }
        return surname;
    }

    /**
     * 随机生成一个名字（不包含姓氏）
     *
     * @param nameLen 名字长度
     * @return 返回生成的名字
     */
    abstract String randomName(int nameLen);

    List<Surname> getAllSurname() {
        return NameContainer.getInstance().getSurnames();
    }

    List<Surname> getHundredSurname() {
        return NameContainer.getInstance().getHundredSurnames();
    }

    Surname randomInAllSurname() {
        List<Surname> surnames = getAllSurname();
        return surnames.get(RandomUtils.nextInt(0, surnames.size()));
    }

    Surname randomInHundredSurname() {
        List<Surname> surnames = getHundredSurname();
        return surnames.get(RandomUtils.nextInt(0, surnames.size()));
    }

    Surname getSurname(String surname) {
        return NameContainer.getInstance().getSurnameMap().get(surname);
    }

    List<ChineseCharacter> getAllCharacters() {
        return NameContainer.getInstance().getCharacters();
    }

    ChineseCharacter randomCharacter() {
        List<ChineseCharacter> characters = getAllCharacters();
        return characters.get(RandomUtils.nextInt(0, characters.size()));
    }

    private void initTotalWeight(List<ChineseCharacter> characters) {
        for (ChineseCharacter character : characters) {
            totalWight += character.getWeight();
        }
    }

    ChineseCharacter randomCharacterByWeight() {
        List<ChineseCharacter> characters = getAllCharacters();
        if (totalWight == 0) {
            initTotalWeight(characters);
        }

        int num = 0;
        int random = RandomUtils.nextInt(0, totalWight);
        for (ChineseCharacter character : characters) {
            num += character.getWeight();
            if (random <= num) {
                return character;
            }
        }

        // 正常的情况下，走不到这里
        return characters.get(RandomUtils.nextInt(0, characters.size()));
    }

    int getNameLen(int max) {
        int nameLen = max;
        if (nameLen >= 1) {
            if (nameLen > getNameLimit()) {
                nameLen = getNameLimit();
            }
        } else {
            nameLen = randomNameLen();
        }
        return nameLen;
    }

    int getNameLen(int max, Surname surname) {
        return getNameLen(max - surname.getLen());
    }

    private int randomNameLen() {
        return RandomUtils.nextInt(0, nameLimit) + 1;
    }

    private int getNameLimit() {
        return nameLimit;
    }

    @Override
    public void setNameLimit(int nameLimit) {
        this.nameLimit = nameLimit;
    }
}
