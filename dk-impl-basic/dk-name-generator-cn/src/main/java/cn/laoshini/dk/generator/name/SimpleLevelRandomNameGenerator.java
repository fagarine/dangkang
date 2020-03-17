package cn.laoshini.dk.generator.name;

/**
 * @author fagarine
 */
class SimpleLevelRandomNameGenerator extends AbstractLevelRandomNameGenerator {

    @Override
    Surname randomSurname() {
        return randomInHundredSurname();
    }

    @Override
    String randomName(int nameLen) {
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < nameLen; i++) {
            name.append(randomCharacter().getCharacter());
        }
        return name.toString();
    }
}
