package cn.laoshini.dk.transform;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import cn.laoshini.dk.transform.javassist.FunctionInjectionModifier;

/**
 * 当康游戏项目Class文件Transformer总入口
 *
 * @author fagarine
 */
public class DangKangClassFileTransformer implements ClassFileTransformer {

    private DangKangClassFileTransformer() {
        initData();
    }

    private static DangKangClassFileTransformer ins = new DangKangClassFileTransformer();

    public static DangKangClassFileTransformer getInstance() {
        return ins;
    }

    private List<AbstractClassFileModifier> modifiers;

    private Set<String> gameBasePackages;

    private Set<String> exclusivePackages;

    private synchronized void initData() {
        // 注册所有的transformer
        findAndRegisterClassFileModifiers();

        // 统计所有transformer的包过滤路径
        gameBasePackages = new CopyOnWriteArraySet<>();
        for (AbstractClassFileModifier transformer : modifiers) {
            if (transformer.basePackage() != null && transformer.basePackage().length > 0) {
                gameBasePackages.addAll(Arrays.asList(transformer.basePackage()));
            }
        }

        exclusivePackages = new CopyOnWriteArraySet<>();
        exclusivePackages.add("jdk.");
        exclusivePackages.add("sun.");
        exclusivePackages.add("java.");
        exclusivePackages.add("javax.");
        exclusivePackages.add("com.sun.");
        exclusivePackages.add("javassist.");
        exclusivePackages.add("org.groovy.");
        exclusivePackages.add("com.intellij.");
        exclusivePackages.add("org.jetbrains.");
        exclusivePackages.add("org.springframework.");
    }

    /**
     * 查找并注册类文件修改器对象
     */
    private void findAndRegisterClassFileModifiers() {
        // 注册类文件修改器对象
        modifiers = new ArrayList<>();
        modifiers.add(new FunctionInjectionModifier());
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        String name = className.replace("//", "/").replace('/', '.');

        if (!classFilter(name)) {
            return null;
        }

        ClassDefinedWrapper wrapper = new ClassDefinedWrapper(loader, name, classBeingRedefined, protectionDomain,
                classfileBuffer);
        try {
            boolean changed = false;
            IClassByteCodeCache byteCodeCache = null;
            for (AbstractClassFileModifier modifier : modifiers) {
                if (modifier.transform(wrapper) && !changed) {
                    changed = true;
                    byteCodeCache = modifier;
                }
            }

            // Class没有被改变时，返回null
            return changed ? byteCodeCache.classToBytes(name) : null;
        } catch (Exception e) {
            System.err.println("ClassFileTransformer出错:" + name);
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 对传入类进行简单的目录检查，过滤掉不是游戏项目的类
     *
     * @param className 类的全限定名
     * @return 返回是否通过过滤
     */
    private boolean classFilter(String className) {
        if (null == className) {
            return false;
        }

        for (String exclusivePackage : exclusivePackages) {
            if (className.startsWith(exclusivePackage)) {
                return false;
            }
        }

        // 如果未设置包名限制，默认允许所有的类通过检查
        if (null == gameBasePackages || gameBasePackages.isEmpty()) {
            return true;
        }

        for (String packagePrefix : gameBasePackages) {
            if (className.startsWith(packagePrefix)) {
                return true;
            }
        }
        return false;
    }
}
