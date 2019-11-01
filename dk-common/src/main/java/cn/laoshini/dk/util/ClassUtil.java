package cn.laoshini.dk.util;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author fagarine
 */
public class ClassUtil {
    private ClassUtil() {
    }

    /**
     * 取得jar包中某个包路径下所有继承了指定类（包括接口）的类
     *
     * @param jar jar包文件对象
     * @param classLoader 类加载器
     * @param packageName 包路径
     * @param classes 指定类型
     * @return 返回所有实现类
     */
    public static List<Class<?>> getAllClassByInterfaceInJar(JarFile jar, ClassLoader classLoader, String packageName,
            Class<?>... classes) {
        if (classes == null || classes.length == 0) {
            return Collections.emptyList();
        }

        // 获取包路径下所有的类
        return getAllClassInJarFile(jar, classLoader, packageName, (clazz) -> isSubClass(classes, clazz));
    }

    private static boolean isSubClass(Class<?>[] classes, Class<?> clazz) {
        for (Class<?> c : classes) {
            if (c.equals(clazz) || c.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 取得某个包路径下所有继承了指定类（包括接口）的类
     *
     * @param classLoader 类加载器
     * @param packageNames 包路径
     * @param classes 指定类型
     * @return 返回所有实现类
     */
    public static List<Class<?>> getAllClassByInterface(ClassLoader classLoader, String[] packageNames,
            Class<?>... classes) {
        if (classes == null || classes.length == 0) {
            return Collections.emptyList();
        }

        // 获取包路径下所有的类
        return getAllClassInPackages(classLoader, packageNames, (clazz) -> isSubClass(classes, clazz));
    }

    /**
     * 获取包目录下的所有被指定注解标记的类
     *
     * @param classLoader 类加载器
     * @param packageName 包路径
     * @param annotationClass 注解类
     * @return 返回所有符合条件的类
     */
    public static List<Class<?>> getAllClassByAnnotation(ClassLoader classLoader, String packageName,
            Class<? extends Annotation> annotationClass) {
        // 获取包路径下所有的类
        return getAllClassInPackage(classLoader, packageName, true, annotationFilter(annotationClass));
    }

    public static List<Class<?>> getAllClassInPackages(ClassLoader classLoader, String[] packages,
            Predicate<Class<?>> filter) {
        Set<Class<?>> classes = new HashSet<>();
        if (CollectionUtil.isNotEmpty(packages)) {
            for (String packageName : packages) {
                classes.addAll(getAllClassInPackage(classLoader, packageName, true, filter));
            }
        }
        return new ArrayList<>(classes);
    }

    public static List<Class<?>> getAllClassInJarFile(JarFile jarFile, ClassLoader classLoader, String[] packages,
            Predicate<Class<?>> filter) {
        Set<Class<?>> classes = new HashSet<>();
        if (CollectionUtil.isNotEmpty(packages)) {
            for (String packageName : packages) {
                // 获取包路径下所有的类
                classes.addAll(getAllClassInJarFile(jarFile, classLoader, packageName, filter));
            }
        }
        return new ArrayList<>(classes);
    }

    public static List<Class<?>> getClassByAnnotationInPackages(ClassLoader classLoader, String[] packages,
            Class<? extends Annotation> annotationClass) {
        // 获取包路径下所有的类
        return getAllClassInPackages(classLoader, packages, annotationFilter(annotationClass));
    }

    public static List<Class<?>> getClassByAnnotationAndParentClass(ClassLoader classLoader, String[] packages,
            Class<? extends Annotation> annotationClass, Class<?> parentClass) {
        // 获取包路径下所有的类
        return getAllClassInPackages(classLoader, packages, annotationAndParentFilter(annotationClass, parentClass));
    }

    private static Predicate<Class<?>> annotationAndParentFilter(Class<? extends Annotation> annotationClass,
            Class<?> parentClass) {
        return clazz -> clazz.isAnnotationPresent(annotationClass) && parentClass.isAssignableFrom(clazz);
    }

    public static List<Class<?>> getAllClassByAnnotationInJarFile(JarFile jarFile, ClassLoader classLoader,
            String packageName, Class<? extends Annotation> annotationClass) {
        // 获取包路径下所有的类
        return getAllClassInJarFile(jarFile, classLoader, packageName, annotationFilter(annotationClass));
    }

    /**
     * 取得某一个类所在包下的所有类名（不含迭代，仅用于以文件形式保存的类，Jar包形式的不行）
     *
     * @param classLocation 项目根目录
     * @param packageName 包全路径
     * @return 返回类名
     */
    public static String[] getPackageAllClassName(String classLocation, String packageName) {
        String realClassLocation = classLocation + "/" + packageName.replace('.', '/');
        File packageDir = new File(realClassLocation);
        if (packageDir.isDirectory()) {
            return packageDir.list();
        }
        return null;
    }

    /**
     * 从包package中获取所有的Class
     *
     * @param classLoader 类加载器
     * @param packageName 包路径
     * @param recursive 是否循环迭代
     * @param filter 筛选条件，只记录符合条件的类
     * @return 返回类
     */
    public static List<Class<?>> getAllClassInPackage(ClassLoader classLoader, String packageName, boolean recursive,
            Predicate<Class<?>> filter) {
        List<Class<?>> classList = new LinkedList<>();

        if (packageName == null) {
            packageName = "";
        }
        // 获取包的名字 并进行替换
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = classLoader.getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(classLoader, packageName, filePath, recursive, classList, filter);
                } else if ("jar".equals(protocol)) {
                    // 如果是jar包文件
                    // 定义一个JarFile
                    JarFile jar;
                    try {
                        // 获取jar
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        // 从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        // 同样的进行循环迭代
                        while (entries.hasMoreElements()) {
                            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            // 如果是以/开头的
                            if (name.charAt(0) == '/') {
                                // 获取后面的字符串
                                name = name.substring(1);
                            }
                            // 如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                // 如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    // 获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                // 如果可以迭代下去 并且是一个包
                                if (idx != -1 && recursive) {
                                    // 如果是一个.class文件 而且不是目录
                                    if (name.endsWith(".class") && !entry.isDirectory()) {
                                        // 去掉后面的".class" 获取真正的类名
                                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                                        String fullClassName = packageName + '.' + className;
                                        try {
                                            Class<?> clazz = classLoader.loadClass(fullClassName);
                                            if (filter == null || filter.test(clazz)) {
                                                classList.add(clazz);
                                            }
                                        } catch (Throwable e) {
                                            LogUtil.error("load class出错:" + fullClassName, e);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        LogUtil.error("读取jar文件内容出错", e);
                    }
                }
            }
        } catch (IOException e) {
            LogUtil.error("读取class出错", e);
        }

        return classList;
    }

    public static List<Class<?>> getAllClassInJarFile(JarFile jar, ClassLoader classLoader, String packageDirName,
            Predicate<Class<?>> filter) {
        List<Class<?>> classList = new LinkedList<>();
        // 从此jar包 得到一个枚举类
        Enumeration<JarEntry> entries = jar.entries();
        packageDirName = packageDirName == null ? "" : packageDirName;
        String packageName = packageDirName.replace('/', '.');
        // 进行循环迭代
        while (entries.hasMoreElements()) {
            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            // 如果是以/开头的
            if (name.charAt(0) == '/') {
                // 获取后面的字符串
                name = name.substring(1);
            }
            // 如果前半部分和定义的包名相同
            if (name.startsWith(packageDirName)) {
                int idx = name.lastIndexOf('/');
                // 如果以"/"结尾 是一个包
                if (idx != -1) {
                    // 获取包名 把"/"替换成"."
                    packageName = name.substring(0, idx).replace('/', '.');
                }
                // 如果可以迭代下去 并且是一个包
                if (idx != -1) {
                    // 如果是一个.class文件 而且不是目录
                    if (name.endsWith(".class") && !entry.isDirectory()) {
                        // 去掉后面的".class" 获取真正的类名
                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                        String fullClassName = packageName + '.' + className;
                        try {
                            Class<?> clazz = classLoader.loadClass(name);
                            if (filter == null || filter.test(clazz)) {
                                classList.add(clazz);
                            }
                        } catch (Throwable e) {
                            LogUtil.error("load class出错:" + fullClassName, e);
                        }
                    }
                }
            }
        }
        return classList;
    }

    /**
     * 以文件的形式来获取包下的所有Class
     *
     * @param packageName 起始包名称
     * @param packagePath 包路径
     * @param recursive 是否循环迭代
     * @param classes 记录Class的集合
     * @param filter 筛选条件，只记录符合条件的类
     */
    public static void findAndAddClassesInPackageByFile(ClassLoader classLoader, String packageName, String packagePath,
            final boolean recursive, List<Class<?>> classes, Predicate<Class<?>> filter) {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirFiles = dir.listFiles(
                // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
                file -> (recursive && file.isDirectory()) || (file.getName().endsWith(".class")));

        if (dirFiles == null || dirFiles.length == 0) {
            return;
        }

        // 循环所有文件
        for (File file : dirFiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                String pack = StringUtil.isEmptyString(packageName) ?
                        file.getName() :
                        packageName + "." + file.getName();
                findAndAddClassesInPackageByFile(classLoader, pack, file.getAbsolutePath(), recursive, classes, filter);
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    // 添加到集合中去
                    String name = StringUtil.isEmptyString(packageName) ? className : packageName + "." + className;
                    Class<?> clazz = classLoader.loadClass(name);
                    if (filter == null || filter.test(clazz)) {
                        classes.add(clazz);
                    }
                } catch (Throwable e) {
                    LogUtil.error("", e);
                }
            }
        }
    }

    /**
     * 内部类筛选器
     *
     * @return 返回筛选器Predicate对象
     */
    public static Predicate<Class<?>> innerClassFilter() {
        return (clazz) -> clazz.getName().contains("$");
    }

    /**
     * 返回一个指定注解标记类筛选器
     *
     * @param annotationClass 注解类
     * @return 返回筛选器Predicate对象
     */
    public static Predicate<Class<?>> annotationFilter(Class<? extends Annotation> annotationClass) {
        return (clazz) -> clazz.isAnnotationPresent(annotationClass);
    }

    /**
     * 使用指定类加载器中加载类，如果加载失败，返回null，不抛出异常
     *
     * @param classLoader 类加载器
     * @param className 类的全限定名
     * @return 该方法可能返回null
     */
    public static Class<?> getClass(ClassLoader classLoader, String className) {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
