package cn.laoshini.dk.agent;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Field;

import cn.laoshini.dk.transform.DangKangClassFileTransformer;

/**
 * 当康系统java agent功能入口
 *
 * @author fagarine
 */
public class DangKangAgent {

    public static Instrumentation inst;

    private DangKangAgent() {
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("Game Agent start....");
        System.out.println("agent Instrumentation:" + inst);
        System.out.println("agentmain DangKangAgent loader:" + DangKangAgent.class.getClassLoader());

        setInstrumentation(inst);
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("game premain agent start....");
        System.out.println("agent Instrumentation : " + DangKangAgent.inst);
        System.out.println("premain DangKangAgent loader : " + DangKangAgent.class.getClassLoader());

        setInstrumentation(inst);
    }

    /**
     * 检查java agent是否已加载，如果没有，尝试运行时动态加载
     */
    public static void checkAndLoadAgent() {
        if (inst == null) {
            System.out.println("准备动态加载dk-agent");
            System.out.println("DangKangAgent loader :" + DangKangAgent.class.getClassLoader());
            if (RuntimeAgentLoader.loadAgent()) {
                /*
                 * 当前类加载器不是SystemClassLoader时，可能出现的Instrumentation获取不到问题，原因：
                 * 如果当前类的加载器不是SystemClassLoader，即使动态加载agent成功，Instrumentation对象也只存在于被SystemClassLoader加载的DangKangAgent类中；
                 * 这种情况下，需要将Instrumentation对象赋值给当前DangKangAgent类，否则业务代码还是会找不到Instrumentation对象
                 */
                ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
                if (!DangKangAgent.class.getClassLoader().equals(systemClassLoader)) {
                    try {
                        Class<?> clazz = systemClassLoader.loadClass(DangKangAgent.class.getName());
                        Field field = clazz.getDeclaredField("inst");
                        Instrumentation instrumentation = (Instrumentation) field.get(clazz);
                        setInstrumentation(instrumentation);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void setInstrumentation(Instrumentation instrumentation) {
        if (inst == null) {
            inst = instrumentation;
            addDangKangTransformer();
        }
    }

    private static void addDangKangTransformer() {
        inst.addTransformer(DangKangClassFileTransformer.getInstance());
    }

    /**
     * 重定义类
     *
     * @param clazz 类
     * @param bytes 重定义后的类字节码数据
     * @throws UnmodifiableClassException 类不允许修改时抛出
     * @throws ClassNotFoundException 找不到类时抛出，实际上不会抛出
     */
    public static void redefineClass(Class<?> clazz, byte[] bytes)
            throws UnmodifiableClassException, ClassNotFoundException {
        ClassDefinition cd = new ClassDefinition(clazz, bytes);
        DangKangAgent.inst.redefineClasses(cd);
    }
}
