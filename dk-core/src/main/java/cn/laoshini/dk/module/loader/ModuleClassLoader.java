package cn.laoshini.dk.module.loader;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.jar.JarFile;

import cn.laoshini.dk.util.LogUtil;

/**
 * 插拔式功能模块系统专用类加载器
 *
 * @author fagarine
 */
public class ModuleClassLoader extends URLClassLoader {

    public ModuleClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(className)) {
            LogUtil.trace("接收到类加载请求：'{}'", className);
            Class<?> clazz = findLoadedClass(className);
            if (clazz != null) {
                LogUtil.trace("找到已加载类：'{}'", className);
                return clazz;
            }

            try {
                clazz = getParent().loadClass(className);
                LogUtil.trace("在父加载器中找到类：'{}'", className);
                return clazz;
            } catch (ClassNotFoundException e) {
                LogUtil.trace("父加载器加载类失败，模块类加载器自己加载：'{}'", className);
            }

            return super.loadClass(className);
        }
    }

    public Class<?> getLoadedClass(String className) {
        Class<?> clazz = findLoadedClass(className);
        if (clazz != null) {
            LogUtil.trace("找到已加载类：'{}'", className);
            return clazz;
        }

        try {
            clazz = findClass(className);
            LogUtil.trace("查找已加载类，加载后找到类：'{}'", className);
            return clazz;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addURL(File file) {
        try {
            super.addURL(file.toURI().toURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public URL getResource(String name) {
        LogUtil.trace("尝试查找资源：'{}'", name);
        URL url = findResource(name);
        if (url != null) {
            LogUtil.trace("已找到资源：'{}'", name);
            return url;
        }

        LogUtil.trace("未找到资源：'{}'，委托父加载器查找");

        return getParent().getResource(name);
    }

    public void release() {
        try {
            clearCache();

            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clear URL caches.
     */
    private void clearCache() {
        for (URL url : getURLs()) {
            try {
                URLConnection connection = url.openConnection();
                if (connection instanceof JarURLConnection) {
                    JarFile jarFile = ((JarURLConnection) connection).getJarFile();
                    jarFile.close();
                }
            } catch (IOException ex) {
                // Ignore
            }
        }
    }
}
