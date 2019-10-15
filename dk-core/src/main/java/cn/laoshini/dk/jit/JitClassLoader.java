package cn.laoshini.dk.jit;

import java.io.File;
import java.net.URL;

import cn.laoshini.dk.exception.JitException;
import cn.laoshini.dk.module.loader.ModuleClassLoader;

/**
 * 运行时及时编译、加载的类加载器
 *
 * @author fagarine
 */
public class JitClassLoader extends ModuleClassLoader {
    public JitClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public <T> Class<T> loadClass(String classFilePath, String className) {
        File classFile = new File(classFilePath);
        if (!classFile.exists() || classFile.isDirectory()) {
            throw new JitException("class.file.missing", "类文件不存在:" + classFilePath);
        }

        addURL(classFile);

        try {
            return (Class<T>) loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new JitException("load.class.error", String.format("加载类[%s]失败, file:%s", className, classFilePath));
        }
    }
}
