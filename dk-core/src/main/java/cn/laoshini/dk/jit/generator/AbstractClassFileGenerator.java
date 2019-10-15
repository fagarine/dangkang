package cn.laoshini.dk.jit.generator;

import java.io.File;

import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.exception.JitException;
import cn.laoshini.dk.jit.type.CompositeBean;
import cn.laoshini.dk.util.FileUtil;

/**
 * @author fagarine
 */
abstract class AbstractClassFileGenerator implements IClassFileGenerator {

    /**
     * DTO类生成模版
     */
    protected CompositeBean compositeBean;

    /**
     * 类生成后的类加载器
     */
    protected ClassLoader classLoader;

    /**
     * 生成类的类名
     */
    protected String className;

    /**
     * 生成后的源文件存放目录
     */
    protected String generatedFolder;

    /**
     * 记录源文件内容
     */
    protected StringBuilder javaFileText = new StringBuilder();

    /**
     * 记录hashCode()方法
     */
    protected StringBuilder hashCodeStr = new StringBuilder();

    /**
     * 记录toString()方法
     */
    protected StringBuilder toStringStr = new StringBuilder();

    public AbstractClassFileGenerator(CompositeBean compositeBean, ClassLoader classLoader) {
        this.compositeBean = compositeBean;
        this.classLoader = classLoader;
    }

    private void createGeneratedFolder() {
        generatedFolder = System.getProperty("user.dir") + "/" + GENERATED_PATH;
        File dirFile = new File(generatedFolder);
        if (!dirFile.exists()) {
            if (!dirFile.mkdirs()) {
                throw new JitException("make.dir.fail", "创建JAVA源文件目录失败:" + generatedFolder);
            }
        }
    }

    @Override
    public File toJavaFile() {
        this.className = getValidClassName(compositeBean.getName());

        // 生成源文件存放目录
        createGeneratedFolder();

        // 生成源文件内容
        buildJavaFileText();

        // 创建JAVA源文件并写入数据
        String filePath = generatedFolder + className + Constants.JAVA_FILE_SUFFIX;
        FileUtil.writeFile(filePath, javaFileText.toString());
        return new File(filePath);
    }

    /**
     * 生成源文件内容
     */
    protected void buildJavaFileText() {
        javaFileText.append("/* ").append(DECLARATION).append(" */").append(LS);
        javaFileText.append("package ").append(GENERATED_PACKAGE).append(";").append(PG);

        // 记录源文件的类注释
        StringBuilder classDocStr = new StringBuilder();
        classDocStr.append("/**").append(LS).append(" * ");
        if (compositeBean.getDescription() != null) {
            classDocStr.append(compositeBean.getDescription()).append(LS).append(" *");
        }
        classDocStr.append(LS).append(" * @author ").append(AUTHOR).append(LS).append(" */").append(LS);

        javaFileText.append(buildImportText()).append(classDocStr).append(buildJavaFileContent()).append(LS);
    }

    /**
     * 生成并返回导入类信息
     *
     * @return 返回导入信息
     */
    protected abstract StringBuilder buildImportText();

    /**
     * 生成并返回类内容
     *
     * @return 返回生成类的内容
     */
    protected abstract StringBuilder buildJavaFileContent();

    @Override
    public String getFullClassName() {
        return GENERATED_PACKAGE + "." + className;
    }

    public CompositeBean getCompositeBean() {
        return compositeBean;
    }

    public void setCompositeBean(CompositeBean compositeBean) {
        this.compositeBean = compositeBean;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
