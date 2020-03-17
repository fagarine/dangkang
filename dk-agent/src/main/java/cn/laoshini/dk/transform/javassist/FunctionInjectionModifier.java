package cn.laoshini.dk.transform.javassist;

import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;

import cn.laoshini.dk.transform.ClassDefinedWrapper;

/**
 * 可配置功能对象初始化代码注入
 *
 * @author fagarine
 */
public class FunctionInjectionModifier extends AbstractJavassistModifier {

    private static final String FUNCTION_ANNOTATION = "cn.laoshini.dk.annotation.FunctionDependent";

    @Override
    protected boolean doTransform(ClassDefinedWrapper wrapper, CtClass ctClass) {
        StringBuilder code = new StringBuilder();
        StringBuilder staticCode = new StringBuilder();
        for (CtField ctField : ctClass.getDeclaredFields()) {
            if (fieldHaveAssignedAnnotation(ctField, FUNCTION_ANNOTATION)) {
                if (Modifier.isStatic(ctField.getModifiers())) {
                    staticCode.append(functionInjectionCode(ctField.getName(), ctClass));
                    System.out.println(String.format("准备向类 %s 的静态变量 %s 注入可配置功能", ctClass.getName(), ctField.getName()));
                } else {
                    code.append(functionInjectionCode(ctField.getName()));
                    System.out.println(String.format("准备向类 %s 的变量 %s 注入可配置功能", ctClass.getName(), ctField.getName()));
                }
            }
        }

        boolean changed = code.length() > 0 && addCodeToConstructors(ctClass, code.toString());
        return (staticCode.length() > 0 && addCodeToStaticInitializer(ctClass, staticCode.toString())) || changed;
    }

    @Override
    protected boolean doPrepareTransformerWork(ClassDefinedWrapper wrapper) {
        return false;
    }

    private String functionInjectionCode(String filedName) {
        return "cn.laoshini.dk.transform.injection.ConfigurableFunctionInjectorProxy.getInstance().injectField(this, \""
               + filedName + "\");";
    }

    private String functionInjectionCode(String filedName, CtClass ctClass) {
        return "cn.laoshini.dk.transform.injection.ConfigurableFunctionInjectorProxy.getInstance().injectField("
               + ctClass.getName() + ".class, \"" + filedName + "\");";
    }

}
