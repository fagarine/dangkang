package cn.laoshini.dk.manager;

import java.io.File;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.laoshini.dk.annotation.ResourceHolder;
import cn.laoshini.dk.autoconfigure.DangKangBasicProperties;
import cn.laoshini.dk.common.SpringContextHolder;
import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.module.BusinessPause;
import cn.laoshini.dk.module.loader.ModuleLoader;
import cn.laoshini.dk.util.ClassUtil;
import cn.laoshini.dk.util.LogUtil;

/**
 * 插拔式功能模块管理类
 *
 * @author fagarine
 */
@ResourceHolder
public enum ModuleManager {
    /**
     * 使用枚举实现单例
     */
    INSTANCE;

    private URLClassLoader parentClassLoader;

    /**
     * 记录模块的加载对象
     */
    private Map<String, ModuleLoader> moduleLoaderMap = new ConcurrentHashMap<>();

    /**
     * key: 模块名称{@link ModuleLoader#getModuleName()}
     */
    private Map<String, ModuleLoader> moduleNameMap = new ConcurrentHashMap<>();

    /**
     * 初始化模块系统
     */
    public static void initModuleSystem() {

        initLoaderContext();

        loadModules();
    }

    private static void initLoaderContext() {
        if (INSTANCE.parentClassLoader == null) {
            URLClassLoader parent = (URLClassLoader) ModuleManager.class.getClassLoader();
            if (parent == null) {
                parent = (URLClassLoader) ClassLoader.getSystemClassLoader();
            }
            INSTANCE.parentClassLoader = parent;
        }
    }

    /**
     * 加载（重加载）模块文件
     */
    public static void loadModules() {
        // 系统类库路径
        String moduleRoot = getModuleRootDir();
        File moduleDir = new File(moduleRoot);
        if (!moduleDir.exists()) {
            LogUtil.start("外置模块目录不存在，尝试创建:" + moduleDir.getPath());
            if (!moduleDir.mkdirs()) {
                throw new BusinessException("file.create.fail", "创建外置模块目录失败，请检查用户权限等配置:" + moduleDir.getPath());
            }
        } else if (moduleDir.isFile()) {
            throw new BusinessException("module.not.directory", "外置模块根目录已被文件名占用，请修改配置或移除文件:" + moduleDir.getPath());
        }

        // 获取所有的.jar和.zip文件
        File[] jarFiles = moduleDir.listFiles(
                (file) -> file.isFile() && (file.getName().toLowerCase().endsWith(Constants.JAR_FILE_SUFFIX) || file
                        .getName().toLowerCase().endsWith(Constants.ZIP_FILE_SUFFIX)));

        if (jarFiles == null || jarFiles.length == 0) {
            LogUtil.start("未找到外置模块文件");
            return;
        }

        BusinessPause.pauseAll();
        long start = System.currentTimeMillis();
        try {
            for (File file : jarFiles) {
                ModuleLoader moduleLoader = INSTANCE.moduleLoaderMap.get(file.getAbsolutePath());

                if (moduleLoader != null) {
                    // 检查文件的最后修改时间，修改过的文件需要重新创建一个加载器
                    if (moduleLoader.getLastModifyTime() != file.lastModified()) {
                        moduleLoader = createModuleLoader(file, moduleLoader);
                    }
                    moduleLoader.reloadModule();
                } else {
                    moduleLoader = createModuleLoader(file);
                    moduleLoader.loadModule();
                }
                INSTANCE.moduleNameMap.put(moduleLoader.getModuleName(), moduleLoader);
            }
        } finally {
            LogUtil.info("模块加载耗时:[{}]ms", System.currentTimeMillis() - start);
            BusinessPause.releaseAll();
        }
    }

    private static ModuleLoader createModuleLoader(File file) {
        ModuleLoader moduleLoader = ModuleLoader.createInstance(file, INSTANCE.parentClassLoader);
        INSTANCE.moduleLoaderMap.put(file.getAbsolutePath(), moduleLoader);
        return moduleLoader;
    }

    private static ModuleLoader createModuleLoader(File file, ModuleLoader oldModuleLoader) {
        ModuleLoader moduleLoader = ModuleLoader.createInstance(file, INSTANCE.parentClassLoader);
        moduleLoader.setOldModuleLoader(oldModuleLoader);
        INSTANCE.moduleLoaderMap.put(file.getAbsolutePath(), moduleLoader);
        return moduleLoader;
    }

    /**
     * 获取所有已加载的模块名称
     *
     * @return 返回所有已加载的模块名称
     */
    public static List<String> getModuleNames() {
        return new ArrayList<>(INSTANCE.moduleNameMap.keySet());
    }

    /**
     * 从系统中移除模块
     *
     * @param moduleName 模块名称
     */
    public static void removeModule(String moduleName) {
        ModuleLoader moduleLoader = INSTANCE.moduleNameMap.remove(moduleName);
        if (moduleLoader == null) {
            throw new BusinessException("module.not.found", String.format("在已加载模块未找到名称为 [%s] 的模块", moduleName));
        }

        INSTANCE.moduleLoaderMap.remove(moduleLoader.getModuleFilePath());
        moduleLoader.cleanUp();
    }

    /**
     * 获取模块文件目录路径
     *
     * @return 返回外置模块根目录
     */
    public static String getModuleRootDir() {
        String moduleDir = SpringContextHolder.getBean(DangKangBasicProperties.class).getModule();
        return System.getProperty("user.dir") + moduleDir + "/";
    }

    /**
     * 在模块中查找类并返回
     *
     * @param className 类的全限定名
     * @return 如果未找到类，将会返回null
     */
    public static Class<?> findModuleClass(String className) {
        Class<?> clazz;
        for (ModuleLoader loader : INSTANCE.moduleLoaderMap.values()) {
            if ((clazz = ClassUtil.getClass(loader.getClassLoader(), className)) != null) {
                return clazz;
            }
        }
        return null;
    }

}
