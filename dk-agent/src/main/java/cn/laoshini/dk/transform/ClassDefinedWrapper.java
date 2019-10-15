package cn.laoshini.dk.transform;

import java.security.ProtectionDomain;

/**
 * 已定义类相关信息，用于在{@link AbstractClassFileModifier}对象间作为上下文传递
 *
 * @author fagarine
 */
public class ClassDefinedWrapper {

    private ClassLoader loader;

    private String className;

    private Class<?> classBeingRedefined;

    private ProtectionDomain protectionDomain;

    private byte[] classfileBuffer;

    public ClassDefinedWrapper(ClassLoader loader, String className, Class<?> classBeingRedefined) {
        this.loader = loader;
        this.className = className;
        this.classBeingRedefined = classBeingRedefined;
    }

    public ClassDefinedWrapper(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        this.loader = loader;
        this.className = className;
        this.classBeingRedefined = classBeingRedefined;
        this.protectionDomain = protectionDomain;
        this.classfileBuffer = classfileBuffer;
    }

    public ClassLoader getLoader() {
        return loader;
    }

    public void setLoader(ClassLoader loader) {
        this.loader = loader;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Class<?> getClassBeingRedefined() {
        return classBeingRedefined;
    }

    public void setClassBeingRedefined(Class<?> classBeingRedefined) {
        this.classBeingRedefined = classBeingRedefined;
    }

    public ProtectionDomain getProtectionDomain() {
        return protectionDomain;
    }

    public void setProtectionDomain(ProtectionDomain protectionDomain) {
        this.protectionDomain = protectionDomain;
    }

    public byte[] getClassfileBuffer() {
        return classfileBuffer;
    }

    public void setClassfileBuffer(byte[] classfileBuffer) {
        this.classfileBuffer = classfileBuffer;
    }
}
