package cn.laoshini.dk.generator.name;

/**
 * @author fagarine
 */
class BasicLevelRandomNameGenerator extends AbstractLevelRandomNameGenerator {

    @Override
    Surname randomSurname() {
        return randomInAllSurname();
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
