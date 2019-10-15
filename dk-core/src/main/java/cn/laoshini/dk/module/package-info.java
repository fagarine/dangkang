/**
 * 当康系统外置模块（亦称为插件式模块、可插拔模块）相关功能包
 * <p>
 * 关于外置模块的概念：对应一个jar包，存放在指定的模块目录下（默认为项目classpath:/modules目录），可不停服执行jar包内容的加载、移除、更新
 * </p>
 * <br>
 * 实现原理：
 * <p>
 * 类的加载和移除通过项目自定义类加载器（参见{@link cn.laoshini.dk.module.loader.ModuleClassLoader}）实现，针对模块加载器，有以下特性：
 * <ul>
 * <li>1. 每个模块，也就是每个jar包，对应一个类加载器实例；</li>
 * <li>2. 进程启动时会自动加载模块目录下的所有模块；</li>
 * <li>3. 加载模块时，会扫描所有的类文件，查找被本项目自定义注解标记的类，并注册；</li>
 * <li>4. 项目使用了Spring容器，使用了Spring注解标记的类会被自动注册到容器中；</li>
 * <li>5. 当发起更新模块的请求时，没有改动的jar包（以文件的最后更新时间为依据）不会重新加载；</li>
 * <li>6. 发生改变的jar包，会先卸载原jar包的内容，再加载新jar包的内容</li>
 * </ul>
 * <p>
 * 如果你要添加一个自己的模块到项目中，必须在模块项目的根目录添加配置文件，填写相关配置项，以便于识别和管理，系统会在加载模块时读取其中的内容；<br>
 * 配置文件的名称格式参见：{@link cn.laoshini.dk.constant.ModuleConstant#MODULE_CONFIG_FILE_REG_EXP}，例如一个正确的文件名为:application-my-module.properties；<br>
 * 同时，为了能够在填写配置项时获得IDE的自动提示，请添加依赖项目cn.laoshini:dk-autoconfigure，这里面有本项目已定义的配置项，本系统自带的配置项均以'dk.'作为前缀
 * </p>
 *
 * <p>
 * 通过与网友的讨论，发现可能面临的问题：
 * 对于有状态的实例，其持有的状态（参数）在热更后怎么处理；
 * 一个解决方案是，通过公共池缓存参数来处理；但是这样会使得代码很丑陋，且会给开发人员增加麻烦，需要考虑如何解决这个问题
 * 实施的成本、风险和可靠性问题
 * 分布式和集群的情况下，如何实现
 * </p>
 *
 * @author fagarine
 * @see cn.laoshini.dk.module.loader.ModuleLoader
 * @see cn.laoshini.dk.module.loader.ModuleClassLoader
 * @see cn.laoshini.dk.manager.ModuleManager
 * @see cn.laoshini.dk.module.registry.AggregateModuleRegistry
 */
package cn.laoshini.dk.module;