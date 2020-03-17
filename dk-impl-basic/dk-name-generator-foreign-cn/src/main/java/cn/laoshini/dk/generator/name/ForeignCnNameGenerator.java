package cn.laoshini.dk.generator.name;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import cn.laoshini.dk.annotation.FunctionVariousWays;
import cn.laoshini.dk.util.JsonUtil;
import cn.laoshini.dk.util.StringUtil;

/**
 * 中文形式的外国人名称生成器，本实现中名字间的默认连接符为空字符串，即无连接符，可通过{@link #setNameSeparator(String)}方法设置。
 * 本实现类不支持名称生成规则的分级，即本实现中{@link #setLevel(Level)}方法无效
 *
 * @author fagarine
 */
@Component
@FunctionVariousWays(value = "foreign-cn", description = "中文形式的外国人名称生成器")
public class ForeignCnNameGenerator implements INameGenerator {

    /**
     * 外国人名字之间的连接符
     */
    public static final String FOREIGN_NAME_SEPARATOR = "●";

    /**
     * 名字（不包含姓氏）最大长度
     */
    private static final int DEFAULT_NAME_LENGTH_LIMIT = 2;

    private int nameLengthLimit = DEFAULT_NAME_LENGTH_LIMIT;

    private String nameSeparator = "";

    private List<String> familyList;
    private List<String> nameList;

    @PostConstruct
    public void initialize() {
        ClassPathResource resource = new ClassPathResource("ini_name.json");
        List<IniName> names = JsonUtil.readBeanList(resource, IniName.class);

        List<String> familyList = new ArrayList<>();
        List<String> nameList = new ArrayList<>();
        for (IniName staticName : names) {
            String familyName = staticName.getFamilyName();
            String womanName = staticName.getWomanName();
            String manName = staticName.getManName();
            if (StringUtil.isNotEmptyString(familyName)) {
                familyList.add(familyName);
            }

            if (StringUtil.isNotEmptyString(womanName)) {
                nameList.add(womanName);
            }

            if (StringUtil.isNotEmptyString(manName)) {
                nameList.add(manName);
            }
        }
        this.familyList = familyList;
        this.nameList = nameList;
    }

    @Override
    public String newName(String suffix, int length) {
        int nameLen = length - 1;
        if (nameLen >= 1) {
            if (nameLen > nameLengthLimit()) {
                nameLen = nameLengthLimit();
            }
        } else {
            nameLen = randomNameLen();
        }
        return generateName(suffix, nameLen);
    }

    private String generateName(String suffix, int nameLen) {
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < nameLen; i++) {
            if (i > 0) {
                name.append(nameSeparator);
            }
            name.append(randomName());
        }
        name.append(nameSeparator);
        if (StringUtil.isNotEmptyString(suffix)) {
            name.append(suffix);
        } else {
            name.append(randomFamilyName());
        }
        return name.toString();
    }

    @Override
    public List<String> batchName(String suffix, int length, int count) {
        int nameLen = length - 1;
        boolean randomLen = false;
        if (nameLen >= 1) {
            if (nameLen > nameLengthLimit()) {
                nameLen = nameLengthLimit();
            }
        } else {
            randomLen = true;
        }

        count = batchCount(count);
        List<String> names = new ArrayList<>(count);
        for (int c = 0; c < count; c++) {
            if (randomLen) {
                nameLen = randomNameLen();
            }
            names.add(generateName(suffix, nameLen));
        }
        return names;
    }

    private String randomFamilyName() {
        return familyList.get(RandomUtils.nextInt(0, familyList.size()));
    }

    private String randomName() {
        return nameList.get(RandomUtils.nextInt(0, nameList.size()));
    }

    private int randomNameLen() {
        return RandomUtils.nextInt(1, nameLengthLimit());
    }

    @Override
    public void setNameLengthLimit(int limit) {
        this.nameLengthLimit = limit;
    }

    @Override
    public int nameLengthLimit() {
        return nameLengthLimit;
    }

    @Override
    public void setNameSeparator(String nameSeparator) {
        this.nameSeparator = nameSeparator;
        if (this.nameSeparator == null) {
            this.nameSeparator = "";
        }
    }
}
