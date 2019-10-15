package cn.laoshini.dk.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.exception.BusinessException;

/**
 * @author fagarine
 */
public class ZipUtil {
    private ZipUtil() {
    }

    /**
     * 解压文件（zip或jar文件）内指定目录下的内容到指定目录
     *
     * @param zipFilePath zip或jar文件路径
     * @param pathPrefix 文件筛选前缀
     * @param targetPath 目标保存路径
     */
    public static void uncompressFile(String zipFilePath, String pathPrefix, String targetPath) {
        if (!isZipOrJarFile(zipFilePath)) {
            return;
        }

        if (StringUtil.isEmptyString(targetPath)) {
            throw new BusinessException("zip.target.null", "zip文件解压后的目录不能为空, zip file:" + zipFilePath);
        }

        File targetFile = new File(targetPath);
        if (!targetFile.exists()) {
            if (!targetFile.mkdirs()) {
                throw new BusinessException("create.dir.fail", "解压目录创建失败:" + targetPath);
            }
        } else if (targetFile.isFile()) {
            throw new BusinessException("uncompress.dir.invalid", "解压路径不能为文件:" + targetPath);
        }

        String startPath = StringUtil.isEmptyString(pathPrefix) ? null : pathPrefix;
        if (startPath != null && (startPath.endsWith("/") || startPath.endsWith("\\"))) {
            startPath = startPath.substring(0, startPath.length() - 1);
        }

        try (ZipFile zipFile = new ZipFile(new File(zipFilePath))) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            ZipEntry entry;
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                if (startPath == null || entry.getName().startsWith(startPath)) {
                    InputStream inputStream = zipFile.getInputStream(entry);
                    String filePath = targetFile.getPath();
                    if (startPath != null) {
                        filePath = filePath + entry.getName().substring(startPath.length());
                    }
                    File file = new File(filePath);
                    if (!file.exists()) {
                        if (!file.createNewFile()) {
                            throw new BusinessException("create.file.fail", "文件创建失败:" + filePath);
                        }
                    } else if (file.equals(targetFile)) {
                        // 解压根目录，跳过
                        continue;
                    }

                    // 写入文件
                    FileUtil.writeFile(file, inputStream);
                }
            }
        } catch (IOException e) {
            LogUtil.error("解压zip文件出错:" + zipFilePath, e);
        }
    }

    public static boolean isZipOrJarFile(String filepath) {
        if (StringUtil.isEmptyString(filepath)) {
            return false;
        }

        if (!filepath.endsWith(Constants.ZIP_FILE_SUFFIX) && !filepath.endsWith(Constants.JAR_FILE_SUFFIX)) {
            return false;
        }

        return isZipOrJarFile(new File(filepath));
    }

    public static boolean isZipOrJarFile(File file) {
        return file != null && file.exists() && !file.isDirectory();
    }
}
