package cn.laoshini.dk.common;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import cn.laoshini.dk.constant.Constants;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.StringUtil;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 配置文件读取
 *
 * @author fagarine
 */
public class PropertiesReader {

    private Properties properties;

    private PropertiesReader() {
    }

    public static PropertiesReader newEmptyFileReader() {
        return newReaderByFile(null);
    }

    public static PropertiesReader newReader(String filePath) {
        return newReaderByFile(new File(filePath));
    }

    public static PropertiesReader newReaderByProperties(Properties p) {
        PropertiesReader propertiesReader = new PropertiesReader();
        propertiesReader.properties = new Properties();
        if (p != null) {
            propertiesReader.copyPropertiesIfAbsent(p);
        }
        return propertiesReader;
    }

    public static PropertiesReader newReaderByFile(File file) {
        PropertiesReader propertiesReader = new PropertiesReader();
        Properties properties = new Properties();
        propertiesReader.properties = properties;
        if (file == null || file.exists() || !file.isFile()) {
            return propertiesReader;
        }

        try (Reader reader = new FileReader(file)) {
            properties.load(reader);
        } catch (IOException e) {
            LogUtil.error("读取配置文件出错:" + file.getPath(), e);
        }

        return propertiesReader;
    }

    public void copyProperties(Properties p) {
        if (p != null) {
            for (String name : p.stringPropertyNames()) {
                properties.put(name, new String(p.getProperty(name).getBytes(ISO_8859_1), UTF_8));
            }
        }
    }

    public void copyPropertiesIfAbsent(Properties p) {
        if (p != null) {
            for (String name : p.stringPropertyNames()) {
                if (!containsValidKey(name)) {
                    properties.put(name, new String(p.getProperty(name).getBytes(ISO_8859_1), UTF_8));
                }
            }
        }
    }

    public void copyProperties(PropertiesReader reader) {
        if (reader != null) {
            copyProperties(reader.getProperties());
        }
    }

    public void copyPropertiesIfAbsent(PropertiesReader reader) {
        if (reader != null) {
            copyPropertiesIfAbsent(reader.getProperties());
        }
    }

    public void put(String key, Object value) {
        properties.put(key, value);
    }

    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }

    public boolean containsValidKey(String key) {
        return StringUtil.isNotEmptyString(get(key));
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public Integer getInteger(String key) {
        String value = get(key);
        if (value != null) {
            return Integer.parseInt(value);
        }
        return null;
    }

    public Integer getInteger(String key, Integer defaultValue) {
        String value = get(key);
        if (value != null) {
            return Integer.parseInt(value);
        }
        return defaultValue;
    }

    public Long getLong(String key) {
        String value = get(key);
        if (value != null) {
            return Long.parseLong(value);
        }
        return null;
    }

    public Long getLong(String key, Long defaultValue) {
        String value = get(key);
        if (value != null) {
            return Long.parseLong(value);
        }
        return defaultValue;
    }

    public String[] getArray(String key) {
        String value = get(key);
        if (value != null) {
            return value.trim().split(Constants.SEPARATOR_COMMA);
        }
        return null;
    }

    public String[] getArrayNotNull(String key) {
        String value = get(key);
        if (value != null) {
            return value.trim().split(Constants.SEPARATOR_COMMA);
        }
        return new String[0];
    }

    public String[] getArray(String key, String separator) {
        String value = get(key);
        if (value != null) {
            return value.trim().split(separator);
        }
        return null;
    }

    public Properties getProperties() {
        return properties;
    }
}
