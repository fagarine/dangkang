package cn.laoshini.dk.function;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.laoshini.dk.annotation.FunctionDependent;
import cn.laoshini.dk.exception.BusinessException;
import cn.laoshini.dk.util.StringUtil;

/**
 * 可配置功能的供应器，根据用户选择，获取对应的功能实现对象（适用于单例的功能实现对象）
 * <p>
 * 用户可以通过{@link cn.laoshini.dk.function.VariousWaysManager#getFunctionImplByKey(Class, String, Object...)}等方法手动获取实现对，
 * 或者通过{@link cn.laoshini.dk.annotation.FunctionDependent}注解标记，由容器自动注入相关依赖，
 * 为什么还要提供一个这样的类呢？
 * </p>
 * <p>
 * 根本原因在于，当康系统要支持外置模块的热插拔，当模块重加载后，被依赖对象可能发生改变（变成另一个对象或者没有了），对于依赖了这些对象的变量，
 * 如果是由系统注入的依赖还好，系统可以主动刷新它们的值；但是，对于用户手动获取被依赖对象，并将其赋值给其他变量的，系统难以追踪，也无法更新它们的值。
 * 基于以上问题考虑，如果能够将用户的依赖对象记录在系统明确可以追踪到的地方，不就可以在模块更新后及时刷新了吗？
 * </p>
 * 本类就是依据以上想法，提出的一个简单的解决方案，目的是让用户既可以免于考虑模块更新后带来的问题，也可以及时更新依赖对象。
 * 具体用法如下：
 * <pre>{@code
 * import cn.laoshini.dk.annotation.FunctionDependent;
 * import cn.laoshini.dk.dao.IDefaultDao;
 * import cn.laoshini.dk.dao.query.QueryUtil;
 * import cn.laoshini.dk.entity.HotfixRecord;
 *
 * public class MyComponent {
 *     // 方式1：使用注解自动注入（注入当前当前默认实现可以不指定实现key）
 *     @FunctionDependent("implKey")
 *     private Func<IDefaultDao> defaultDaoFunc;
 *
 *     // 方式2：手动获取当前默认实现
 *     public Func<IDefaultDao> newDefaultDaoFunc() {
 *         // a. 通过可配置功能的声明类获取
 *         Func<IDefaultDao> daoFunc = Func.ofFunctionInterface(IDefaultDao.class);
 *         // b. 通过可配置功能声明的配置项获取，IDefaultDao的功能声明key为"dk.default-dao"
 *         daoFunc = Func.ofFunctionKey("dk.default-dao");
 *         return daoFunc;
 *     }
 *
 *     // 方式3：手动获取指定实现
 *     public Func<IDefaultDao> newDefaultDaoFuncByKey(String implKey) {
 *         return Func.ofImplKey(IDefaultDao.class, implKey);
 *     }
 *
 *     // 对被依赖功能的用法演示，通过系统默认的DAO对象获取数据库中的所有热修复记录，该功能目前提供的实现类为单例
 *     public void usage() {
 *         // 1. 使用注解自动注入的Func对象
 *         Func<IDefaultDao> daoFunc = defaultDaoFunc;
 *         // 2. 使用手动创建的获取功能默认实现的Func对象
 *         daoFunc = newDefaultDaoFunc();
 *         // 3. 使用手动创建的获取功能指定实现的Func对象
 *         daoFunc = newDefaultDaoFuncByKey("implKey");
 *
 *         // 执行查询操作
 *         daoFunc.get().selectEntityList(HotfixRecord.class, QueryUtil.newListQueryCondition());
 *
 *         // 这个例子是使用单例对象，非单例对象可以参考RelationalDbDaoManager类中的使用
 *     }
 * }
 * }</pre>
 *
 * @author fagarine
 */
public class Func<F> {

    /**
     * 声明依赖可配置功能
     */
    private String name;

    private FunctionSupplier<F> functionSupplier;

    private Map<String, F> keyToFunction;

    private Func(String name) {
        this.name = name;
    }

    /**
     * 根据功能声明类和注解信息，创建一个提供可配置功能指定实现供应器对象
     *
     * @param functionInterface 可配置功能的声明类
     * @param dependent 标记依赖可配置功能的Field的注解
     * @param <F> 可配置功能类型
     * @return 返回一个可配置功能的供应器对象
     */
    public static <F> Func<F> ofDependent(Class<F> functionInterface, FunctionDependent dependent) {
        Func<F> func = new Func<>(toName(functionInterface.getName(), dependent.value()));
        func.functionSupplier = FunctionSupplier.ofFunctionDependent(func.name, functionInterface, dependent);
        FuncContainer.add(func);
        return func;
    }

    /**
     * 根据功能声明类和指定实现key，创建一个提供可配置功能指定实现供应器对象
     *
     * @param functionInterface 可配置功能的声明类
     * @param implKey 指向功能实现类的key
     * @param <F> 可配置功能类型
     * @return 返回一个可配置功能的供应器对象
     */
    public static <F> Func<F> ofImplKey(Class<F> functionInterface, String implKey) {
        Func<F> func = new Func<>(toName(functionInterface.getName(), implKey));
        func.functionSupplier = FunctionSupplier.ofImplKey(func.name, functionInterface, implKey);
        FuncContainer.add(func);
        return func;
    }

    /**
     * 根据可配置功能的声明类，创建一个提供可配置功能默认实现的供应器对象
     *
     * @param functionInterface 可配置功能的声明类
     * @param <F> 可配置功能类型
     * @return 返回一个可配置功能的供应器对象
     */
    public static <F> Func<F> ofFunctionInterface(Class<F> functionInterface) {
        Func<F> func = new Func<>(toName(functionInterface.getName(), null));
        func.functionSupplier = FunctionSupplier.ofFunctionInterface(func.name, functionInterface);
        FuncContainer.add(func);
        return func;
    }

    /**
     * 根据功能声明key，创建一个提供可配置功能默认实现的供应器对象
     *
     * @param functionKey 指向可配置功能的配置项key
     * @param <F> 可配置功能类型
     * @return 返回一个可配置功能的供应器对象
     */
    public static <F> Func<F> ofFunctionKey(String functionKey) {
        Func<F> func = new Func<>(toName("配置名为" + functionKey, null));
        func.functionSupplier = FunctionSupplier.ofFunctionKey(func.name, functionKey);
        FuncContainer.add(func);
        return func;
    }

    static String toName(String functionClassName, String implKey) {
        if (StringUtil.isNotEmptyString(implKey)) {
            return "功能" + functionClassName + "的key为[" + implKey + "]实现";
        } else {
            return "功能" + functionClassName + "的当前默认实现";
        }
    }

    /**
     * 获取功能的实现对象
     *
     * @return 返回对应的实现对象
     */
    public F get() {
        return functionSupplier.get();
    }

    /**
     * 获取功能的实现对象，带构造参数
     *
     * @param params 如果功能实现类需要新建实例，传入构造参数，非必须
     * @return 返回对应的实现对象
     */
    public F get(Object... params) {
        return functionSupplier.get(params);
    }

    /**
     * 获取功能的实现对象，与{@link #get(Object...)}方法不同之处在于，传入了构造参数的具体类型，主要针对构造参数中包含有原始类型，如int类型参数
     *
     * @param params 如果功能实现类需要新建实例，传入构造参数，允许为null
     * @param types 传入构造参数的类型，允许为null
     * @return 返回对应的实现对象
     */
    public F getWithType(Object[] params, Class[] types) {
        return functionSupplier.getByType(params, types);
    }

    /**
     * 获取功能的实现对象，根据传入的key在缓存记录中查找，如果已有关联对象，返回已创建对象
     *
     * @param key 区分其他对象的key，与具体业务相关
     * @param params 传入构造参数，非必须
     * @return 返回对应的实现对象
     */
    public F getByKey(String key, Object... params) {
        assertKeyNotNull(key);

        return getKeyToFunction().computeIfAbsent(key, k -> functionSupplier.get(params));
    }

    /**
     * 获取功能的实现对象，与{@link #getByKey(String, Object...)}方法不同之处在于，传入了构造参数的具体类型，主要针对构造参数中包含有原始类型，如int类型参数
     *
     * @param params 传入构造参数，允许为null
     * @param types 传入构造参数的类型，允许为null
     * @return 返回对应的实现对象
     */
    public F getByKeyWithType(String key, Object[] params, Class[] types) {
        assertKeyNotNull(key);

        return getKeyToFunction().computeIfAbsent(key, k -> functionSupplier.getByType(params, types));
    }

    /**
     * 刷新实现对象
     */
    void refresh() {
        if (keyToFunction != null) {
            keyToFunction.clear();
        }
        functionSupplier.refresh();
    }

    /**
     * 判断被依赖的功能是否有效
     *
     * @return 返回判断结果
     */
    public boolean isValid() {
        return functionSupplier.isValid();
    }

    /**
     * 清空
     */
    void clear() {
        name = null;
        functionSupplier.clear();
        keyToFunction.clear();
        keyToFunction = null;
    }

    private void assertKeyNotNull(String key) {
        if (key == null) {
            throw new BusinessException("cache.key.empty", "FuncCache获取可配置功能的key不能为空, field:" + getName());
        }
    }

    private synchronized Map<String, F> getKeyToFunction() {
        if (keyToFunction == null) {
            keyToFunction = new ConcurrentHashMap<>();
        }
        return keyToFunction;
    }

    public String getName() {
        return name;
    }

    public String getFunctionKey() {
        return functionSupplier.getFunctionKey();
    }
}
