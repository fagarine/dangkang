/**
 * 该包为可配置功能的多样化实现和运行时选择使用哪种具体实现，提供支持
 * <p>
 * 何谓功能的多样化实现：表示一个功能可以有多种实现方式，且每种实现方式拥有自己与其他实现方式区别的key
 * </p>
 * <p>
 * 这么做的好处：
 * <ul>
 * <li>将功能与其他依赖的功能解耦，功能之间通过接口调用，不需要知道具体实现，甚至不需要关心实现类是否是单例</li>
 * <li>方便功能的统一管理，实现类的实例对象由{@link cn.laoshini.dk.function.VariousWaysManager}管理，
 * 统一通过其 {@link cn.laoshini.dk.function.VariousWaysManager#getCurrentImpl(Class, Object...) 获取方法} 获取功能的实现对象</li>
 * <li>实现依赖最小化，可以针对某个功能的某种实现单独去做一个项目，用户只需要单独加入这个项目的依赖即可，避免其他代码干扰</li>
 * </ul>
 * </p>
 * <p>
 * 约定：
 * <ol>
 * <li>功能定义类（一般为接口类）必须使用{@link cn.laoshini.dk.annotation.ConfigurableFunction}注解标记，且指定其配置key</li>
 * <li>如果用户使用了项目的可插拔模块功能，功能定义类尽量不要在可插拔模块中定义，否则模块卸载后，跨模块的调用将可能导致无法预料的问题</li>
 * <li>建议在功能定义时，尽量职责最小化，避免相互干扰</li>
 * <li>如果希望设置某个功能当前的默认实现，不是基本实现方式（约定基本实现的key为{@link cn.laoshini.dk.constant.Constants#DEFAULT_PROPERTY_NAME "DEFAULT"}），
 * 需要在配置项中进行配置（或者在项目中不提供基本实现，只提供这一种实现方式）</li>
 * <li>用户可以使用{@link cn.laoshini.dk.annotation.FunctionDependent}注解标记Field，系统会自动注入其依赖</li>
 * <li>可配置功能的依赖注入发生在容器初始化完成后（具体情况参见{@link cn.laoshini.dk.manager.DangKangAnnotationProcessor}），
 * 所以在容器初始化完成前，请不要调用可配置功能的实现对象</li>
 * <li>如果希望对象在可配置功能依赖注入后做某些事情，可以使用{@link cn.laoshini.dk.annotation.FunctionDependent#initMethod()}记录下方法名，
 * 系统会在依赖注入后查找方法并执行，请保证需要执行的方法是无参方法</li>
 * <li>可配置功能的总配置项（非具体功能的配置项）定义在{@link cn.laoshini.dk.autoconfigure.DangKangFunctionProperties}中，
 * 如果 {@link cn.laoshini.dk.autoconfigure.DangKangFunctionProperties#vacant 检查模式} 被设置为不允许依赖缺失（默认为允许），
 * 那么使用了{@link cn.laoshini.dk.annotation.ConfigurableFunction}注解标记的功能类，在系统启动时至少要有一个实现类，否则会启动失败
 * </li>
 * <li>系统允许实现key重复，但是，实现key相同的，<b>后加载的实现会覆盖掉先加载的</b></li>
 * </ol>
 * </p>
 * <p>
 * 关于可配置功能对于当前默认实现类的选择，遵循以下规则：
 * <ol>
 * a. 用户通过配置项选择了默认实现的：
 * <li>从所有实现查找对应实现，如果找到，则设置为默认实现</li>
 * <li>未找到对应实现，抛出{@link cn.laoshini.dk.exception.BusinessException 异常}通知用户未找到对应实现类</li>
 * </ol>
 * <ol>
 * b. 用户未选择默认实现的：
 * <li>优先查找基础实现（实现key为{@link cn.laoshini.dk.constant.Constants#DEFAULT_PROPERTY_NAME 基础实现的key}），如果存在，则设置基础实现为默认实现</li>
 * <li>如果未找到基础实现，则从当前所有实现中随机取出一种作为默认实现</li>
 * <li>如果当前没有任何实现类，查看当前 {@link cn.laoshini.dk.autoconfigure.DangKangFunctionProperties#vacant 检查模式} 是否允许实现对象为空，
 * 允许为空则打印日志，跳过设置；否则抛出{@link cn.laoshini.dk.exception.BusinessException 异常}通知用户未找到任何实现类</li>
 * </ol>
 * </p>
 * 特别说明：
 * <p>
 * 使用Spring结合{@link javax.annotation.Resource}注解等方式，也可以完成指定实现类的依赖注入，且功能强大，为什么还要单独实现这样的功能呢？
 * </p>
 * <p>
 * 主要考虑原因有以下几点：
 * <ol>
 * <li>不受Spring容器托管的类（指被依赖的类不受Spring托管，用户在自己的项目中不一定会使用Spring），Spring无法处理</li>
 * <li>Spring不能通过配置项，指定某个功能的某个实现类为当前公用的默认实现（目的是动态指定和更改依赖的具体实现对象）</li>
 * <li>Spring的使用不够灵活，比如有时希望功能实现类是非单例的，使用Spring可能使问题变得更麻烦（这主要体现在与上面一条配合使用时麻烦）</li>
 * <li>本项目要支持（jar包级别，详见:{@link cn.laoshini.dk.module 外置模块}）的热插拔，如果模块更新后，原本的被依赖项的实现类没有了，
 * Spring容器难以通知到声明依赖的对象，这可能导致原依赖对象不能释放等不可测问题，而可配置功能系统会主动重置该值，或根据配置项动态选择</li>
 * </ol>
 * </p>
 *
 * @author fagarine
 * @see cn.laoshini.dk.annotation.ConfigurableFunction
 * @see cn.laoshini.dk.annotation.FunctionVariousWays
 * @see cn.laoshini.dk.annotation.FunctionDependent
 * @see cn.laoshini.dk.function.VariousWaysManager
 * @see cn.laoshini.dk.function.ConfigurableFunctionInjector
 * @see cn.laoshini.dk.transform.javassist.FunctionInjectionModifier
 * @see cn.laoshini.dk.autoconfigure.DangKangFunctionProperties
 */
package cn.laoshini.dk.function;