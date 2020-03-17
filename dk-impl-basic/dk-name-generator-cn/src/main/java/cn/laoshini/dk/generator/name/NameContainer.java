package cn.laoshini.dk.generator.name;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;

import cn.laoshini.dk.util.JsonUtil;

/**
 * @author fagarine
 */
class NameContainer {

    private static NameContainer instance = new NameContainer();
    private List<Surname> surnames;
    /**
     * 姓氏全国排名中排在前百的姓
     */
    private List<Surname> hundredSurnames = new ArrayList<>(100);
    private Map<String, Surname> surnameMap = new HashMap<>(1500);
    private List<ChineseCharacter> characters = new ArrayList<>(3000);

    private NameContainer() {
    }

    static NameContainer getInstance() {
        return instance;
    }

    void initialize() {
        ClassPathResource resource = new ClassPathResource("surname.json");
        surnames = JsonUtil.readBeanList(resource, Surname.class);
        for (Surname surname : surnames) {
            surnameMap.put(surname.getSurname(), surname);
            if (surname.getLevel() > 1) {
                hundredSurnames.add(surname);
            }
        }

        resource = new ClassPathResource("chinese_character.json");
        characters = JsonUtil.readBeanList(resource, ChineseCharacter.class);
        for (ChineseCharacter character : characters) {
            character.setWeight(toWeight(character.getLevel()));
        }
    }

    private int toWeight(int level) {
        int base = level - 1;
        return 1 << (2 << base);
    }

    List<Surname> getSurnames() {
        return surnames;
    }

    List<Surname> getHundredSurnames() {
        return hundredSurnames;
    }

    Map<String, Surname> getSurnameMap() {
        return surnameMap;
    }

    List<ChineseCharacter> getCharacters() {
        return characters;
    }
}
