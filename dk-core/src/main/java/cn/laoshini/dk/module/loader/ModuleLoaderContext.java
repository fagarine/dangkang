package cn.laoshini.dk.module.loader;

import java.io.File;

/**
 * 插拔式功能模块加载上下文对象
 *
 * @author fagarine
 */
public class ModuleLoaderContext {

    private ModuleClassLoader classLoader;

    /**
     * 模块jar包文件
     */
    protected File moduleFile;

    public ModuleLoaderContext(ModuleClassLoader classLoader, File moduleFile) {
        this.classLoader = classLoader;
        this.moduleFile = moduleFile;
    }

    public String getModuleFilePath() {
        return moduleFile.getAbsolutePath();
    }

    public ModuleClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ModuleClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public File getModuleFile() {
        return moduleFile;
    }
}
