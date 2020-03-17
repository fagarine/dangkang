package cn.laoshini.dk.module.loader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.InputStreamResource;

import cn.laoshini.dk.common.PropertiesReader;
import cn.laoshini.dk.common.SpringContextHolder;
import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.constant.PropertyKeyConstants;
import cn.laoshini.dk.module.registry.AggregateModuleRegistry;
import cn.laoshini.dk.util.FileUtil;
import cn.laoshini.dk.util.LogUtil;

/**
 * 模块系统类加载管理
 *
 * @author fagarine
 */
public class ModuleLoader extends ModuleLoaderContext {
    /**
     * 同一模块旧的加载器，重加载时需要使用
     */
    private ModuleLoader oldModuleLoader;

    /**
     * 记录模块的配置信息
     */
    private PropertiesReader configReader;

    /**
     * 记录模块名称
     */
    private String moduleName;

    /**
     * 记录模块jar包文件的最后修改时间
     */
    private long lastModifyTime;

    /**
     * 记录最后一次加载完成时间
     */
    private Date lastLoadedTime;

    /**
     * 记录最后一次执行出错时间
     */
    private long lastFailTime;

    /**
     * 记录模块中已加载到Spring容器中的配置名称
     */
    private List<String> propertySourceNames;

    /**
     * 当康系统内部功能聚合注册表
     */
    private AggregateModuleRegistry moduleRegistry;

    private boolean success;

    private ModuleLoader(File moduleFile, URLClassLoader parentClassLoader) throws MalformedURLException {
        super(new ModuleClassLoader(new URL[] { moduleFile.toURI().toURL() }, parentClassLoader), moduleFile);
    }

    public static ModuleLoader createInstance(File moduleFile, URLClassLoader parentClassLoader) {
        checkModuleFile(moduleFile);
        try {
            return new ModuleLoader(moduleFile, parentClassLoader);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("模块文件路径不正常，不能读取:" + moduleFile);
        }
    }

    private static void checkModuleFile(File moduleFile) {
        if (moduleFile == null || !moduleFile.exists() || !moduleFile.isFile() || !isValidModuleFile(moduleFile)) {
            throw new IllegalArgumentException("模块文件不正常，不能读取:" + moduleFile);
        }
    }

    private static boolean isValidModuleFile(File moduleFile) {
        String fileName = moduleFile.getName().toLowerCase();
        return fileName.endsWith(Constants.JAR_FILE_SUFFIX) || fileName.endsWith(Constants.ZIP_FILE_SUFFIX);
    }

    /**
     * 重加载模块代码
     */
    public void reloadModule() {
        moduleFile = new File(moduleFile.getPath());
        checkModuleFile(moduleFile);

        // 检查文件的最后修改时间，已加载过且没有修改的文件不重复加载（其实用文件的MD5值更可靠一些，但是对文件进行MD5操作可能比加载操作更耗时）
        if (moduleFile.lastModified() == lastModifyTime) {
            LogUtil.info("模块文件没有改变，跳过重加载:{}", moduleFile.getName());
            return;
        }

        if (oldModuleLoader != null) {
            oldModuleLoader.prepareCleanUp();
        }

        // 执行正常的加载逻辑
        loadModule();

        // 已执行注册操作的，暂不提供回滚
        if (success || moduleRegistry.isRegistered()) {
            if (oldModuleLoader != null) {
                oldModuleLoader.cleanUp();
                oldModuleLoader = null;
            }
        } else {
            // 回滚
            rollback();
            if (oldModuleLoader != null) {
                oldModuleLoader.rollback();
            }
        }
    }

    /**
     * 加载模块信息到系统中
     */
    public void loadModule() {
        // 读取jar包中的配置信息
        readJarFile();

        lastLoadedTime = new Date();
    }

    /**
     * 读取jar包文件，处理信息
     */
    private void readJarFile() {
        try (JarFile jarFile = new JarFile(moduleFile)) {
            SpringContextHolder.setSpringCurrentClassLoader(getClassLoader());

            // 执行功能注册逻辑
            AggregateModuleRegistry oldRegistry = oldModuleLoader == null ? null : oldModuleLoader.moduleRegistry;
            moduleRegistry = AggregateModuleRegistry.newInstance(this, oldRegistry);
            // 预备注册，做一些准备与验证操作
            moduleRegistry.prepareRegister(jarFile);

            // 在jar包中查找配置文件并读取配置信息
            readConfigFromPropertiesFile(jarFile);

            // 正式注册
            moduleRegistry.register(jarFile);

            LogUtil.start("成功加载模块: [{}]", getModuleName());

            lastModifyTime = moduleFile.lastModified();
            success = true;
        } catch (IOException e) {
            LogUtil.error(String.format("读取模块文件出错: %s", moduleFile.getPath()), e);
        } catch (Throwable t) {
            LogUtil.error(String.format("模块加载出错: %s", moduleFile.getPath()), t);
        } finally {
            SpringContextHolder.resetSpringCurrentClassLoader();
        }
    }

    /**
     * 在jar包中查找配置文件并读取配置信息
     *
     * @param jarFile jar包文件对象
     */
    private void readConfigFromPropertiesFile(JarFile jarFile) {
        propertySourceNames = new ArrayList<>();
        Enumeration<JarEntry> its = jarFile.entries();
        while (its.hasMoreElements()) {
            JarEntry entry = its.nextElement();

            if (!entry.isDirectory() && FileUtil.isModuleConfigFile(entry.getName())) {
                // 配置文件存在的情况下，读取配置文件信息
                try (InputStream inputStream = jarFile.getInputStream(entry)) {
                    Properties properties = new Properties();
                    if (FileUtil.isYamlFile(entry.getName())) {
                        // yaml配置文件，转换为Properties对象
                        YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();
                        factoryBean.setResources(new InputStreamResource(inputStream));
                        properties = factoryBean.getObject();
                    } else {
                        properties.load(inputStream);
                    }

                    if (configReader == null) {
                        configReader = PropertiesReader.newReaderByProperties(properties);
                    } else {
                        configReader.copyPropertiesIfAbsent(properties);
                    }

                    // 配置项数据添加到Spring容器
                    SpringContextHolder.addProperties(entry.getName(), properties);
                    propertySourceNames.add(entry.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 如果未找到配置信息，生成一个空的配置信息对象
        if (configReader == null) {
            configReader = PropertiesReader.newEmptyFileReader();
        }
    }

    /**
     * 获取模块名称
     *
     * @return 返回模块名称
     */
    public String getModuleName() {
        if (moduleName == null) {
            findModuleName();
        }

        return moduleName;
    }

    /**
     * 查找模块名称（先从配置文件中查找，如果找不到，则使用文件名作为模块名）
     */
    private void findModuleName() {
        if (configReader.containsValidKey(PropertyKeyConstants.MODULE_NAME_KEY)) {
            moduleName = configReader.get(PropertyKeyConstants.MODULE_NAME_KEY);
        } else {
            String fileName = moduleFile.getName();
            moduleName = fileName.substring(0, fileName.lastIndexOf("."));
        }
    }

    /**
     * 预备清空数据
     */
    public void prepareCleanUp() {
        try {
            SpringContextHolder.setSpringCurrentClassLoader(getClassLoader());
            moduleRegistry.prepareUnregister();
        } finally {
            SpringContextHolder.resetSpringCurrentClassLoader();
        }
    }

    public void rollback() {
        try {
            SpringContextHolder.setSpringCurrentClassLoader(getClassLoader());
            moduleRegistry.rollback();
        } finally {
            SpringContextHolder.resetSpringCurrentClassLoader();
        }
    }

    /**
     * 清空数据
     */
    public void cleanUp() {
        this.moduleFile = null;
        this.configReader = null;

        // 清除注册信息
        unregister();

        propertySourceNames.clear();
        propertySourceNames = null;
        oldModuleLoader = null;
        moduleRegistry = null;

        // 关闭类加载器
        getClassLoader().release();
    }

    private void unregister() {
        moduleRegistry.unregister();

        if (propertySourceNames != null && !propertySourceNames.isEmpty()) {
            SpringContextHolder.removePropertiesList(propertySourceNames);
        }
    }

    public long getLastModifyTime() {
        return lastModifyTime;
    }

    public Date getLastLoadedTime() {
        return lastLoadedTime;
    }

    public ModuleLoader getOldModuleLoader() {
        return oldModuleLoader;
    }

    public void setOldModuleLoader(ModuleLoader oldModuleLoader) {
        this.oldModuleLoader = oldModuleLoader;
    }

    public boolean isSuccess() {
        return success || moduleRegistry.isRegistered();
    }

    public long getLastFailTime() {
        return lastFailTime;
    }

    public void setLastFailTime(long lastFailTime) {
        this.lastFailTime = lastFailTime;
    }
}
