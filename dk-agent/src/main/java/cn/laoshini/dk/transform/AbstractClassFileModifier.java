package cn.laoshini.dk.transform;

/**
 * 定义修改Class字节码信息功能的抽象类
 *
 * @author fagarine
 */
public abstract class AbstractClassFileModifier implements IClassByteCodeCache {

    /**
     * 执行类修改逻辑
     * <p>
     * 注意：如果本类的子类重写了父类的一些限制方法（如{@link #requiredPackages()}），并返回了不为空的值，
     * 该方法被调用时，将会检查传入类是否符合这些检查条件，如果不符合，将什么都不会执行
     * </p>
     *
     * @param wrapper 类相关信息
     * @return 如果类被修改，返回true
     */
    public boolean transform(ClassDefinedWrapper wrapper) {
        boolean changed = doPrepareTransformerWork(wrapper);
        if (checkConditions(wrapper)) {
            if (doTransform(wrapper)) {
                changed = true;
            }
        } else {
            System.out.println("class不满足条件:" + wrapper.getClassName());
        }
        return changed;
    }

    /**
     * 执行具体的类修改操作
     *
     * @param wrapper 类相关信息
     * @return 如果类被修改，返回true
     */
    protected abstract boolean doTransform(ClassDefinedWrapper wrapper);

    /**
     * 一些实现类必须要做的前置准备工作，可以通过实现该方法完成，该方法会在{@link #checkConditions(ClassDefinedWrapper)}前执行
     *
     * @param wrapper 类相关信息
     * @return 如果类被修改，返回true
     */
    protected abstract boolean doPrepareTransformerWork(ClassDefinedWrapper wrapper);

    /**
     * 检查传入类是否符合条件
     *
     * @param wrapper 类相关信息
     * @return 返回检查结果
     */
    protected boolean checkConditions(ClassDefinedWrapper wrapper) {
        return checkClassPackage(wrapper.getClassName()) && checkAnnotations(wrapper);
    }

    /**
     * 检查传入了是否在指定的包体下
     *
     * @param className 类名称
     * @return 返回传入了是否在指定的包体下
     */
    protected boolean checkClassPackage(String className) {
        String[] packages = requiredPackages();
        if (null == packages || packages.length == 0) {
            return true;
        }

        for (String packageName : packages) {
            if (className.startsWith(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查传入类是否添加了必须的注解
     *
     * @param wrapper 类相关信息
     * @return 返回传入类是否添加了必须的注解
     */
    protected boolean checkAnnotations(ClassDefinedWrapper wrapper) {
        return false;
    }

    /**
     * 项目基础包过滤路径，不是该包路径下的类不做transformer检查
     *
     * @return 返回基础包路径约束
     */
    public String[] basePackage() {
        return new String[0];
    }

    /**
     * 返回需要处理的类所在的包路径，默认没有限制
     * <p>
     * 注意：如果该方法返回一个非空的数组，将会检查传入类所在的包是否符合要求，如果不符合，则会被认为是不需要执行修改的类，将不会对类进行修改
     * </p>
     *
     * @return 返回包路径要求
     */
    protected String[] requiredPackages() {
        return new String[0];
    }

    /**
     * 返回需要被修改的类必须添加的注解类名称，默认没有限制
     * <p>
     * 注意：如果该方法返回一个非空的数组，将会检查传入类的注解信息，如果该类没有添加这些注解，则会被认为是不需要执行修改的类，将不会对类进行修改
     * </p>
     *
     * @return 返回注解要求
     */
    protected String[] requiredAnnotations() {
        return new String[0];
    }
}
