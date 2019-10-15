package cn.laoshini.dk.util;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.jit.JitClassLoader;
import cn.laoshini.dk.module.loader.ModuleClassLoader;

/**
 * @author fagarine
 */
public class TypeHelper {
    private TypeHelper() {
    }

    /**
     * 传入类型是否是外置模块加载的类
     *
     * @param type 指定类型
     * @return 返回判断结果
     */
    public static boolean isModuleInnerType(Class<?> type) {
        return type.getClassLoader() instanceof ModuleClassLoader && !(type.getClassLoader() instanceof JitClassLoader);
    }

    /**
     * 传入类型是否不是外置模块加载的类
     *
     * @param type 指定类型
     * @return 返回判断结果
     */
    public static boolean notModuleType(Class<?> type) {
        return !isModuleInnerType(type);
    }

    public static <E> E mapToBean(Map<String, Object> params, Class<E> type) {
        // 自己实现的话需要通过反射，并处理字段名称等问题，这里偷个懒，借助fastjson实现，性能可能会有影响
        return JSON.parseObject(JSON.toJSONString(params), type);
    }

    public static <E> List<E> mapToBeanList(List<Map<String, Object>> paramMapList, Class<E> type) {
        // 自己实现的话需要通过反射，并处理字段名称等问题，这里偷个懒，借助fastjson实现，性能可能会有影响
        try {
            return JSON.parseArray(JSON.toJSONString(paramMapList), type);
        } catch (Exception e) {
            throw new BusinessException("", "数据转换出错", e);
        }
    }
}
