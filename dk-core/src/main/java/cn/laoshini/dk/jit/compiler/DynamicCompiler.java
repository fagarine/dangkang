package cn.laoshini.dk.jit.compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.exception.JitException;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.Md5Util;
import cn.laoshini.dk.util.StringUtil;

/**
 * Java源文件动态编译功能支持
 *
 * @author fagarine
 */
public class DynamicCompiler {
    private static String classRootDir = System.getProperty("user.dir");

    private static final List<String> DEFAULT_OPTIONS = Arrays.asList("-d", classRootDir);

    public static void compile(File srcFile) {
        if (SpringBootDynamicCompiler.isSpringBootStart()) {
            SpringBootDynamicCompiler.compileFile(srcFile);
        } else {
            compileSingle(srcFile);
        }
    }

    public static void compileFiles(List<File> srcFiles) {
        if (SpringBootDynamicCompiler.isSpringBootStart()) {
            SpringBootDynamicCompiler.compileFiles(srcFiles);
        } else {
            compileSrcFiles(srcFiles, DEFAULT_OPTIONS);
        }
    }

    /**
     * 编译源文件
     *
     * @param options 传入编译器的参数项
     * @param srcFiles 需要编译的源文件
     */
    public static void compile(List<String> options, File... srcFiles) {
        compileSrcFiles(Arrays.asList(srcFiles), options);
    }

    /**
     * 编译源文件
     *
     * @param srcFiles 需要编译的源文件
     * @param options 传入编译器的参数项
     */
    public static void compileSrcFiles(List<File> srcFiles, List<String> options) {
        if (CollectionUtil.isEmpty(srcFiles)) {
            return;
        }

        LogUtil.info("compile options:{}", options);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileMgr = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileMgr.getJavaFileObjectsFromFiles(srcFiles);
        // 用来获取编译错误时的错误信息
        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileMgr, collector, options, null, compilationUnits);
        if (!task.call()) {
            for (Diagnostic<? extends JavaFileObject> diagnostic : collector.getDiagnostics()) {
                LogUtil.info(diagnostic.getMessage(Locale.getDefault()));
            }
            throw new JitException("compile.files.error", "编译源文件出错，files:" + srcFiles);
        }
        try {
            fileMgr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 编译单个Java源文件
     *
     * @param file 需要编译的Java源文件，必须是Java源文件，不能是其他文件或目录
     */
    public static void compileSingle(File file) {
        if (!file.exists() || file.isDirectory()) {
            throw new JitException("compile.file.error", "要编译的源文件无效，文件路径:" + file.getPath());
        }

        if (!file.getName().toLowerCase().endsWith(Constants.JAVA_FILE_SUFFIX)) {
            throw new JitException("compile.file.error", "要编译的源文件不是JAVA源文件，文件路径:" + file.getPath());
        }

        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        int result;
        try {
            result = javac.run(null, null, null, "-d", classRootDir, file.getPath());
        } catch (Throwable e) {
            throw new JitException("compile.file.error", "编译源文件出错，源文件路径:" + file.getPath(), e);
        }

        if (result != 0) {
            throw new JitException("compile.file.fail", "编译源文件失败，源文件路径:" + file.getPath());
        }
    }

    private static void removeClassFileIfExists(String srcFilePath) {
        String classFilePath = srcFilePath.substring(0, srcFilePath.lastIndexOf(".")) + ".class";
        File classFile = new File(classFilePath);
        if (classFile.exists()) {
            if (classFile.delete()) {
                LogUtil.debug("类文件已存在，移除类文件:{}", classFilePath);
            }
        }
    }

    private static boolean classIsGenerated(String srcFilePath) {
        String classFilePath = srcFilePath.substring(0, srcFilePath.length() - 4) + "class";
        File classFile = new File(classFilePath);
        return classFile.exists();
    }

    /**
     * 编译指定文件路径的所有Java源文件（递归查找文件夹下的所有Java源文件）
     *
     * @param filePath 文件路径，可以是目录，也可以是文件的路径
     */
    public static void compileSrcFileInDir(String filePath) {
        if (StringUtil.isEmptyString(filePath)) {
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }

        // 递归查找Java源文件
        List<File> srcFileList = new ArrayList<>();
        getJavaFiles(srcFileList, file);

        // 过滤掉曾经编译过，且后来没有改变过的文件
        //        Map<String, String> newMd5Map = filterCompiledFile(srcFileList, md5Map);

        // 编译Java源文件
        compileSrcFiles(srcFileList, DEFAULT_OPTIONS);
        //        return newMd5Map;
    }

    /**
     * 过滤掉编译过，且后来没有改动过的文件
     *
     * @param srcFileList
     * @param md5Map
     * @return
     */
    private static Map<String, String> filterCompiledFile(List<File> srcFileList, Map<String, String> md5Map) {
        Map<String, String> newMd5Map = new HashMap<>();
        srcFileList.removeIf(file -> {
            String fileName = file.getName();
            String newMd5 = Md5Util.getFileMD5(file);
            newMd5Map.put(fileName, newMd5);
            String md5 = md5Map.get(fileName);
            return null != md5 && md5.equals(newMd5);
        });
        return newMd5Map;
    }

    /**
     * 递归查找所有的Java源文件
     *
     * @param srcFileList
     * @param file
     * @return
     */
    private static void getJavaFiles(List<File> srcFileList, File file) {
        if (null != file && file.exists()) {
            File[] files;
            if (file.isDirectory()) {
                files = file.listFiles();
                if (files != null && files.length > 0) {
                    for (File f : files) {
                        // 递归查找文件夹下的所有文件
                        getJavaFiles(srcFileList, f);
                    }
                }
            } else {
                addJavaFile(srcFileList, file);
            }
        }
    }

    /**
     * 添加Java源文件
     *
     * @param srcFileList
     * @param file
     */
    private static void addJavaFile(List<File> srcFileList, File file) {
        if (null == file || !file.exists()) {
            return;
        }

        if (file.getName().toLowerCase().endsWith(Constants.JAVA_FILE_SUFFIX)) {
            srcFileList.add(file);
        }
    }
}
