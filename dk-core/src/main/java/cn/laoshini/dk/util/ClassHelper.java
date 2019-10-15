package cn.laoshini.dk.util;

import cn.laoshini.dk.jit.DynamicGenerator;
import cn.laoshini.dk.manager.ModuleManager;
import cn.laoshini.dk.module.loader.ModuleClassLoader;

/**
 * Class处理工具类
 *
 * @author fagarine
 */
public class ClassHelper {
    private ClassHelper() {
    }

    public static Class<?> getClass(ClassLoader classLoader, String className) {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static Class<?> getClass(String className) {
        ClassLoader cl = ClassHelper.class.getClassLoader();
        Class<?> clazz = getClass(cl, className);
        // 在当前类加载器中找不到，尝试从JIT类加载器中查找
        if (clazz == null) {
            clazz = getClass(DynamicGenerator.JIT_CLASS_LOADER, className);
        }
        return clazz;
    }

    /**
     * 根据类全名查找类，如果在系统类加载器中找不到，将会到传入的模块类加载器中查找
     *
     * @param classLoader 模块类加载器
     * @param className 类的全限定名
     * @return 返回类，如果没有找到，将会返回null
     */
    public static Class<?> getClassByModule(ModuleClassLoader classLoader, String className) {
        Class<?> clazz = getClass(className);
        // 如果在当前类加载器和JIT类加载器中找不到，则尝试从传入的模块类加载器中查找
        if (clazz == null) {
            clazz = getClass(classLoader, className);
        }
        return clazz;
    }

    /**
     * 根据类全名获取类，如果系统类加载器中找不到，将会到模块类加载器中查找
     *
     * @param className 类的全限定名
     * @return 返回类，如果没有找到，将会返回null
     */
    public static Class<?> getClassAnywhere(String className) {
        Class<?> clazz = getClass(className);

        // 如果在当前类加载器和JIT类加载器中找不到，则尝试从模块类加载器中查找
        if (clazz == null) {
            clazz = ModuleManager.findModuleClass(className);
        }
        return clazz;
    }
}
