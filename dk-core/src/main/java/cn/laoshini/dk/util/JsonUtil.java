package cn.laoshini.dk.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import cn.laoshini.dk.domain.common.ConstTable;
import cn.laoshini.dk.domain.common.MultiConstTableContent;
import cn.laoshini.dk.exception.BusinessException;

/**
 * JSON格式数据读写工具类
 *
 * @author fagarine
 */
public class JsonUtil {
    private JsonUtil() {
    }

    private static File getJsonFileOnCheck(String filepath) {
        File file = new File(filepath);
        if (!file.exists() || file.isDirectory()) {
            throw new BusinessException("json.file.error", "JSON文件不存在:" + filepath);
        }
        return file;
    }

    public static String readJsonFileToString(String filepath) {
        String jsonStr = null;
        try {
            jsonStr = FileUtil.readFileToString(getJsonFileOnCheck(filepath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (StringUtil.isEmptyString(jsonStr)) {
            throw new BusinessException("json.file.empty", String.format("JSON文件[%s]内容为空，请检查文件", filepath));
        }
        return jsonStr;
    }

    private static String subName(String filepath) {
        filepath = filepath.replaceAll("\\\\", "/");
        return filepath.substring(filepath.lastIndexOf("/"), filepath.lastIndexOf("."));
    }

    /**
     * 读取JSON文件，并以MultiConstTableContent形式返回，其中单个对象（对应表的一行）的形式为Map&lt;name, value&gt;
     *
     * @param filepath 文件路径
     * @return 该方法不会返回null，但有可能抛出异常
     */
    public static MultiConstTableContent readMultiConstTableJsonFile(String filepath) {
        String jsonStr = readJsonFileToString(filepath);
        MultiConstTableContent jsonContent = JSON.parseObject(jsonStr, MultiConstTableContent.class);
        if (StringUtil.isEmptyString(jsonContent.getFileName())) {
            jsonContent.setFileName(subName(filepath));
        }
        return jsonContent;
    }

    /**
     * 读取记录了单个表单配置数据的JSON文件，并以ConstTable形式返回，其中单个对象（对应表的一行）的形式为Map&lt;name, value&gt;
     *
     * @param filepath 文件路径
     * @return 该方法不会返回null，但有可能抛出异常
     */
    public static ConstTable<Map<String, Object>> readConstTableJsonFile(String filepath) {
        String jsonStr = readJsonFileToString(filepath);
        ConstTable<Map<String, Object>> table = JSON.parseObject(jsonStr, ConstTable.class);
        if (StringUtil.isEmptyString(table.getTableName())) {
            table.setTableName(subName(filepath));
        }

        return table;
    }

    /**
     * 读取记录了单个表单配置数据的JSON文件，并以指定类型返回
     *
     * @param filepath 文件路径
     * @param entityType 单个对象类型
     * @param <E> 单个对象类型
     * @return 该方法不会返回null，但有可能抛出异常
     */
    public static <E> List<E> readBeanList(String filepath, Class<E> entityType) {
        String jsonStr = readJsonFileToString(filepath);
        return JSON.parseArray(jsonStr, entityType);
    }

    /**
     * 将对象以JSON字符串形式写入文件
     *
     * @param filepath 文件路径
     * @param object 待写入对象
     */
    public static void writeJsonFile(String filepath, Object object) {
        if (object == null) {
            object = "";
        }

        File file = FileUtil.createFile(filepath);

        String json;
        if (object instanceof String) {
            json = (String) object;
        } else {
            json = JSON.toJSONString(object);
        }
        FileUtil.writeFile(file, json);
    }

}
