package cn.laoshini.dk.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.util.FileCopyUtils;

import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.constant.ModuleConstant;
import cn.laoshini.dk.domain.common.HotfixFile;
import cn.laoshini.dk.exception.BusinessException;

/**
 * 文件基础操作工具类
 *
 * @author fagarine
 */
public class FileUtil {
    private FileUtil() {
    }

    public static final FileFilter CLASS_FILE_FILTER = FileUtil::isClassFile;

    public static String getProjectRoot() {
        return System.getProperty("user.dir");
    }

    public static String getProjectPath(String relativePath) {
        return getProjectRoot() + File.separator + relativePath;
    }

    public static String readFileToString(String filepath) throws IOException {
        return FileCopyUtils.copyToString(new FileReader(filepath));
    }

    public static String readFileToString(File filepath) throws IOException {
        return FileCopyUtils.copyToString(new FileReader(filepath));
    }

    public static void writeFile(String filepath, String content) {
        try (FileWriter writer = new FileWriter(filepath)) {
            writer.write(content);
        } catch (IOException e) {
            LogUtil.error("文件写入失败:" + filepath);
        }
    }

    public static void writeFile(File file, String content) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        } catch (IOException e) {
            LogUtil.error("文件写入失败:" + file);
        }
    }

    public static void writeFile(String filepath, InputStream inputStream) {
        writeFile(new File(filepath), inputStream);
    }

    public static void writeFile(File file, InputStream inputStream) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            FileCopyUtils.copy(inputStream, fileOutputStream);
        } catch (IOException e) {
            throw new BusinessException("write.file.error", "数据写入文件出错:" + file.getPath(), e);
        }
    }

    public static File createFile(String filepath) {
        if (StringUtil.isEmptyString(filepath)) {
            throw new BusinessException("file.path.empty", "文件路径不能为空");
        }

        File file = new File(filepath);
        if (file.isDirectory()) {
            throw new BusinessException("file.path.illegal", "文件路径不能为目录:" + filepath);
        } else if (!file.exists()) {
            try {
                if (!file.mkdirs() || !file.createNewFile()) {
                    throw new BusinessException("create.file.denied", "文件创建失败:" + filepath);
                }
            } catch (IOException e) {
                throw new BusinessException("create.file.error", "文件创建出错:" + filepath, e);
            }
        }
        return file;
    }

    /**
     * 判断文件是否是模块的配置文件
     *
     * @param fileName 文件名
     * @return 返回判断结果
     */
    public static boolean isModuleConfigFile(String fileName) {
        if (validateModuleConfigFileSuffix(fileName)) {
            String name = fileName.substring(0, fileName.lastIndexOf("."));
            for (Pattern pattern : ModuleConstant.MODULE_CONFIG_FILE_PATTERNS) {
                if (pattern.matcher(name).find()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 验证文件名是否复合模块配置文件的后缀名
     *
     * @param fileName 文件名
     * @return 返回验证结果
     */
    public static boolean validateModuleConfigFileSuffix(String fileName) {
        for (String suffix : ModuleConstant.MODULE_CONFIG_FILE_SUFFIX) {
            if (fileName.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断文件是否是yaml配置文件
     *
     * @param fileName 文件名
     * @return 返回判断结果
     */
    public static boolean isYamlFile(String fileName) {
        for (String yamlFileSuffix : Constants.YAML_FILE_SUFFIX) {
            if (fileName.endsWith(yamlFileSuffix)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isClassFile(File file) {
        return file.getName().toLowerCase().endsWith(Constants.CLASS_FILE_SUFFIX);
    }

    public static String readClassFileFullName(File f) {
        if (!isClassFile(f)) {
            return null;
        }

        try {
            byte[] bytes = FileCopyUtils.copyToByteArray(f);
            DynamicClassLoader myLoader = new DynamicClassLoader();
            Class<?> targetClazz = myLoader.loadClassFile(bytes);
            return targetClazz.getName();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static class DynamicClassLoader extends ClassLoader {
        Class<?> loadClassFile(byte[] b) {
            return this.defineClass(null, b, 0, b.length);
        }
    }

    public static void readHotfixFiles(File file, List<HotfixFile> fileList) {
        if (file.isDirectory()) {
            File[] files = file.listFiles(CLASS_FILE_FILTER);
            if (files != null && files.length > 0) {
                for (File f : files) {
                    readHotfixFiles(f, fileList);
                }
            }
        } else {
            String fullClassName = readClassFileFullName(file);
            if (fullClassName != null) {
                HotfixFile hotfixFile = new HotfixFile();
                hotfixFile.setFullClassName(fullClassName);
                hotfixFile.setFilePath(file.getAbsolutePath());
                hotfixFile.setLastModifyTime(file.lastModified());
                fileList.add(hotfixFile);
            }
        }
    }

    public static void clearHotfixDir(File curFile, Map<String, Long> fileTimeMap) {
        if (curFile.isDirectory()) {
            File[] files = curFile.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    clearHotfixDir(file, fileTimeMap);
                }
            }
        } else {
            try {
                String fullClassName = readClassFileFullName(curFile);
                if (fullClassName != null) {
                    fileTimeMap.put(fullClassName, curFile.lastModified());
                }

                if (curFile.delete()) {
                    LogUtil.info("移除热修复类文件 :" + curFile.getName());
                }
            } catch (Exception e) {
                LogUtil.info(String.format("file:%s", curFile.getName()), e);
            }

        }
    }
}
