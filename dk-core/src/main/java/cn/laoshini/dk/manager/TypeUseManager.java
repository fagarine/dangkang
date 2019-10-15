package cn.laoshini.dk.manager;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.laoshini.dk.constant.UseTypeEnum;
import cn.laoshini.dk.domain.common.ClassDescriptor;
import cn.laoshini.dk.net.msg.ICustomDto;
import cn.laoshini.dk.util.ClassUtil;
import cn.laoshini.dk.util.CollectionUtil;
import cn.laoshini.dk.util.LogUtil;
import cn.laoshini.dk.util.Md5Util;
import cn.laoshini.dk.util.StringUtil;
import cn.laoshini.dk.util.TypeUtil;

/**
 * 常用类型注册管理，为表达式逻辑提供支持
 *
 * @author fagarine
 */
public enum TypeUseManager {
    /**
     * 使用枚举实现单例
     */
    INSTANCE;

    private Map<String, ClassDescriptor> types = new LinkedHashMap<>(64);

    public static void registerIntrinsicClasses() {
        ClassLoader cl = TypeUseManager.class.getClassLoader();

        // 常用POJO类
        List<Class<?>> classes = ClassUtil.getAllClassInPackage(cl, "cn.laoshini.dk.domain", true, null);
        for (Class<?> clazz : classes) {
            registerOrdinaryClass(clazz, null);
        }

        // 常用工具类
        registerUtilClass(CollectionUtil.class, "集合类型工具类");
        registerUtilClass(LogUtil.class, "公用日志打印工具类");
        registerUtilClass(Md5Util.class, "MD5工具类");
        registerUtilClass(StringUtil.class, "字符串工具类");
        registerUtilClass(TypeUtil.class, "类型工具类");

        // 自定义格式消息DTO类
        List<Class<?>> dtoClasses = ClassUtil
                .getAllClassByInterface(cl, new String[] { "cn.laoshini.dk.dto" }, ICustomDto.class);
        for (Class<?> clazz : dtoClasses) {
            registerCustomDtoClass((Class<? extends ICustomDto>) clazz);
        }
    }

    public static List<ClassDescriptor> getClasses(String className) {
        String name = StringUtil.isNotEmptyString(className) ? className.toLowerCase() : null;
        List<ClassDescriptor> descriptors = new LinkedList<>();
        for (ClassDescriptor descriptor : INSTANCE.types.values()) {
            if (name == null || descriptor.getSimpleName().contains(name)) {
                descriptors.add(descriptor);
            }
        }
        return descriptors;
    }

    public static void registerCustomDtoClass(Class<? extends ICustomDto> dtoClass) {
        registerClass(dtoClass, null, UseTypeEnum.DTO);
    }

    public static void registerSpringBean(String className, String simpleName) {
        ClassDescriptor descriptorVO = new ClassDescriptor();
        descriptorVO.setClassName(className);
        descriptorVO.setSimpleName(simpleName);
        descriptorVO.setType(UseTypeEnum.SPRING_BEAN.name());
        INSTANCE.types.put(descriptorVO.getSimpleName().toLowerCase(), descriptorVO);
    }

    public static void registerResourceHolders(Map<String, String> holders) {
        for (Map.Entry<String, String> entry : holders.entrySet()) {
            ClassDescriptor descriptorVO = new ClassDescriptor();
            descriptorVO.setSimpleName(entry.getKey());
            descriptorVO.setClassName(entry.getValue());
            descriptorVO.setType(UseTypeEnum.HOLDER.name());
            INSTANCE.types.put(descriptorVO.getSimpleName().toLowerCase(), descriptorVO);
        }
    }

    private static void registerOrdinaryClass(Class<?> clazz, String comment) {
        registerClass(clazz, comment, UseTypeEnum.ORDINARY);
    }

    private static void registerUtilClass(Class<?> utilClass, String comment) {
        registerClass(utilClass, comment, UseTypeEnum.UTIL);
    }

    private static void registerClass(Class<?> clazz, String comment, UseTypeEnum useType) {
        ClassDescriptor descriptorVO = new ClassDescriptor();
        descriptorVO.setClassName(clazz.getName());
        descriptorVO.setSimpleName(clazz.getSimpleName());
        descriptorVO.setType(useType.name());
        descriptorVO.setComment(comment);
        INSTANCE.types.put(descriptorVO.getSimpleName().toLowerCase(), descriptorVO);
    }

}
