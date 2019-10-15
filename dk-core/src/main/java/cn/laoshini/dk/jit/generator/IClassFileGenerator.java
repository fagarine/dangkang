package cn.laoshini.dk.jit.generator;

import java.io.File;
import java.util.regex.Pattern;

import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.util.StringUtil;

/**
 * JAVA类源文件生成器接口
 *
 * @author fagarine
 */
public interface IClassFileGenerator {

    /**
     * 换行符
     */
    String LS = Constants.LINE_SEPARATOR;

    /**
     * 段落间隔，两个换行符
     */
    String PG = LS + LS;

    /**
     * 导入类的开始
     */
    String IMT = "import ";

    /**
     * 实现接口
     */
    String IMP = " implements ";

    /**
     * 单次缩进，4个空格
     */
    String INDENT = "    ";

    /**
     * 两次缩进
     */
    String I2 = INDENT + INDENT;

    /**
     * 方法中的return开始，两次缩进
     */
    String RT = I2 + "return ";

    /**
     * 方法中的this开始
     */
    String TH = I2 + "this.";

    /**
     * 重写方法的公共开始部分
     */
    String ORI = INDENT + "@Override" + LS + INDENT;

    /**
     * 方法关闭，结束花括号加分段
     */
    String CL = INDENT + "}" + PG;

    /**
     * 方法结束
     */
    String END = LS + CL;

    /**
     * 参数注释开始
     */
    String CB = INDENT + "/**" + LS + INDENT + " * ";

    /**
     * 参数注释结束
     */
    String CE = LS + INDENT + " */" + LS;

    /**
     * 生成类的声明信息
     */
    String DECLARATION = "声明信息";

    /**
     * 生成类的作者
     */
    String AUTHOR = "dk generator";

    /**
     * 类名、参数名等名称的匹配规则
     */
    Pattern NAME_PATTERN = Pattern.compile("^[A-Z_]+[a-zA-Z0-9_]+$");

    /**
     * 系统生成的JAVA源文件包路径
     */
    String GENERATED_PACKAGE = "cn.laoshini.dk.jit.generated";
    String GENERATED_PATH = "cn/laoshini/dk/jit/generated/";
    String GENERATED_NAME = "GenerateClass";

    /**
     * 当前的类加载器，也是源文件编译后使用的类加载器
     *
     * @return 返回类加载器对象
     */
    ClassLoader getClassLoader();

    /**
     * 生成JAVA源文件，并返回文件对象
     *
     * @return 返回生成的源文件对象
     */
    File toJavaFile();

    /**
     * 返回生成类的全限定名
     *
     * @return 类名
     */
    String getFullClassName();

    /**
     * 是否是有效的名称（类名、字段名等）
     *
     * @param name 名称
     * @return 返回是否为有效名称
     */
    default boolean isValidName(String name) {
        return NAME_PATTERN.matcher(name).find();
    }

    /**
     * 返回一个有效可使用的名称（类名、字段名等）
     *
     * @param name 候选名称
     * @return 返回一个有效可使用的名称
     */
    default String getValidClassName(String name) {
        String className;
        if (name != null && isValidName(name)) {
            className = name.substring(name.lastIndexOf(".") + 1);
        } else {
            className = candidateClassNamePrefix();
        }

        if (StringUtil.isEmptyString(className)) {
            className = GENERATED_NAME;
        }

        // 检查是否当前已有该名称的类，如果有，就增加后缀的编号，直到没有重复
        int index = 1;
        String clsName = className;
        ClassLoader classLoader = getClassLoader();
        try {
            while (classLoader.loadClass(GENERATED_PACKAGE + "." + clsName + ".class") != null) {
                clsName = className + index++;
            }
        } catch (ClassNotFoundException e) {
            // 未找到类，说明该类名可以使用
        }
        return clsName;
    }

    /**
     * 生成雷的候选类名前缀
     *
     * @return 候选类名前缀
     */
    default String candidateClassNamePrefix() {
        return GENERATED_NAME;
    }

}
