package cn.laoshini.dk.generator.name;

/**
 * @author fagarine
 */
class MiddleLevelRandomNameGenerator extends AbstractLevelRandomNameGenerator {

    @Override
    Surname randomSurname() {
        return randomInHundredSurname();
    }

    @Override
    String randomName(int nameLen) {
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < nameLen; i++) {
            name.append(randomCharacterByWeight().getCharacter());
        }
        return name.toString();
    }
}
