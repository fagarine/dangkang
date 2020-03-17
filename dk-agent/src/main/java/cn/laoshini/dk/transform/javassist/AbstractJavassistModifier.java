package cn.laoshini.dk.transform.javassist;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationDefaultAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ClassFile;

import cn.laoshini.dk.transform.AbstractClassFileModifier;
import cn.laoshini.dk.transform.ClassDefinedWrapper;

/**
 * 使用Javassist修改Class字节码的抽象类
 *
 * @author fagarine
 */
public abstract class AbstractJavassistModifier extends AbstractClassFileModifier {

    @Override
    protected boolean doTransform(ClassDefinedWrapper wrapper) {
        CtClass ctClass = getCtClass(wrapper);
        if (ctClass != null) {
            return doTransform(wrapper, ctClass);
        }
        return false;
    }

    /**
     * 执行具体的类修改操作
     *
     * @param wrapper 类相关信息
     * @param ctClass 类
     * @return 如果类被修改，返回true
     */
    protected abstract boolean doTransform(ClassDefinedWrapper wrapper, CtClass ctClass);

    protected CtClass getCtClass(ClassDefinedWrapper wrapper) {
        try {
            return ClassPool.getDefault().get(wrapper.getClassName());
        } catch (Throwable t) {
            try {
                return ClassPool.getDefault().makeClassIfNew(new ByteArrayInputStream(wrapper.getClassfileBuffer()));
            } catch (Throwable t1) {
                t1.printStackTrace();
            }
        }

        System.out.println("CtClass创建失败:" + wrapper.getClassName());
        return null;
    }

    protected boolean classIsAssignFrom(CtClass ctClass, String interfaceClass) {
        try {
            for (CtClass classInterface : ctClass.getInterfaces()) {
                if (classInterface.getName().equalsIgnoreCase(interfaceClass)) {
                    return true;
                }
            }
        } catch (NotFoundException e) {
            // ignore
        }
        return false;
    }

    protected boolean isInvocationHandler(CtClass ctClass) {
        return classIsAssignFrom(ctClass, "java.lang.reflect.InvocationHandler");
    }

    @Override
    protected boolean checkAnnotations(ClassDefinedWrapper wrapper) {
        String[] annotations = requiredAnnotations();
        if (null == annotations || annotations.length == 0) {
            return true;
        }

        for (String annotationClass : annotations) {
            if (!classHaveAssignedAnnotation(getCtClass(wrapper), annotationClass)) {
                System.out.println(String.format("类 %s 不包含 %s 注解", wrapper.getClassName(), annotationClass));
                return false;
            }
        }
        return true;
    }

    /**
     * 将代码加入类的构造方法的方法体前
     *
     * @param cc 类
     * @param code 要插入的代码
     * @return 返回是否插入成功
     */
    protected boolean addCodeToConstructors(CtClass cc, String code) {
        if (code != null && !code.isEmpty()) {
            for (CtConstructor constructor : cc.getConstructors()) {
                try {
                    System.out.println(String.format("代码注入构造方法, method:%s.%s(), insertCode:%s", cc.getName(),
                            constructor.getName(), code));
                    constructor.insertBeforeBody(code);
                } catch (CannotCompileException e) {
                    System.out.println(String.format("尝试向构造方法[%s]添加代码出错", constructor.getName()));
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 将代码加入类的静态构造代码块中
     *
     * @param cc 类
     * @param code 要插入的代码
     * @return 返回是否插入成功
     */
    protected boolean addCodeToStaticInitializer(CtClass cc, String code) {
        if (code != null && !code.isEmpty()) {
            CtConstructor constructor = cc.getClassInitializer();
            try {
                if (constructor == null) {
                    constructor = cc.makeClassInitializer();
                }
                System.out.println(String.format("注入静态变量代码, class:%s, insertCode:%s", cc.getName(), code));
                constructor.insertBefore(code);
                return true;
            } catch (CannotCompileException e) {
                System.out.println("尝试注入静态变量代码出错");
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public byte[] classToBytes(String className) {
        try {
            CtClass ctClass = ClassPool.getDefault().get(className);
            return ctClass.toBytecode();
        } catch (NotFoundException | CannotCompileException | IOException e) {
            e.printStackTrace();
            return new byte[] { 0, 0 };
        }
    }

    /**
     * 在类上添加指定注解
     *
     * @param cc 类型信息
     * @param annotationClassName 注解类型名
     */
    protected void addAnnotationToClass(CtClass cc, String annotationClassName) {
        ClassFile cf = cc.getClassFile2();
        AttributeInfo info = new AnnotationDefaultAttribute(cf.getConstPool(), getClassBytes(annotationClassName));
        cf.addAttribute(info);
    }

    /**
     * 传入类是否添加了指定注解
     *
     * @param cc
     * @param annotationClass
     * @return
     */
    protected boolean classHaveAssignedAnnotation(CtClass cc, String annotationClass) {
        if (null == annotationClass || "".equals(annotationClass)) {
            return false;
        }

        try {
            return haveAssignedAnnotation(cc.getAnnotations(), annotationClass);
        } catch (ClassNotFoundException e) {
            System.out.println(String.format("检查类是否有指定注解出错, class:%s, annotation:%s", cc.getName(), annotationClass));
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 传入的Field是否被指定注解标记
     *
     * @param field
     * @param annotationClass
     * @return
     */
    protected boolean fieldHaveAssignedAnnotation(CtField field, String annotationClass) {
        if (null == annotationClass || "".equals(annotationClass)) {
            return false;
        }
        try {
            return haveAssignedAnnotation(field.getAnnotations(), annotationClass);
        } catch (ClassNotFoundException e) {
            System.out.println(
                    String.format("检查Filed是否有指定注解出错, field:%s, annotation:%s", field.toString(), annotationClass));
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 是否包含指定注解
     *
     * @param annotations
     * @param annotationClass
     * @return
     */
    protected boolean haveAssignedAnnotation(Object[] annotations, String annotationClass) {
        if (null == annotationClass || null == annotations || annotations.length == 0) {
            return false;
        }

        for (Object annotation : annotations) {
            if (annotationClass.equals(parseAnnotationName(annotation.toString()))) {
                return true;
            }
        }
        return false;
    }

    protected String parseAnnotationName(String annotationClassName) {
        if (annotationClassName == null || "".equals(annotationClassName)) {
            return "";
        }

        String name = annotationClassName.trim();
        int index = name.indexOf("@");
        if (index > -1) {
            int endIndex = name.indexOf("(");
            if (endIndex == -1) {
                return name.substring(index + 1);
            }
            return name.substring(index + 1, endIndex);
        }
        return name;
    }

}
