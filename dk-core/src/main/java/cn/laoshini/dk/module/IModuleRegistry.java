package cn.laoshini.dk.module;

import java.util.jar.JarFile;

/**
 * 模块功能注册表
 *
 * @author fagarine
 */
public interface IModuleRegistry {

    /**
     * 获取模块的类加载器
     *
     * @return 返回类加载器
     */
    ClassLoader getModuleClassLoader();

    /**
     * 执行模块相关功能的注册
     *
     * @param moduleJarFile 模块对应的jar包对象
     */
    void register(JarFile moduleJarFile);

    /**
     * 准备注销注册信息，旧模块（热更后将被替换的模块称为旧模块）注册表调用
     */
    void prepareUnregister();

    /**
     * 执行注销操作，旧模块调用
     */
    void unregister();
}
