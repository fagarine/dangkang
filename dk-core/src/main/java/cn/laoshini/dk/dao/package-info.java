/**
 * 该包下定义了当康系统内建数据访问相关功能
 * <p>
 * 这里说一下项目中内建Dao的设计思路，也就是为什么这么设计：<br><br>
 * <p>
 * 当康系统的初衷，是为了提供一个拿来即用的、可定制功能的游戏服务器，也就是说这是为不熟悉服务器开发的人、懒人设计的；<br>
 * 基于以上原因，以及我的能力有限的问题，目前设定的目标服务对象偏向于弱联网游戏；<br>
 * 在我的设想中，这样的受众并不需要过多关心数据库问题，所以嵌入式数据库其实是最合适的，这样可以少了安装数据库和许多配置方面的烦恼；<br>
 * 但是关系型数据库，尤其是Mysql肯定是要支持的，而为了减少用户过多的代码工作，所以我在系统中大量使用注解、反射和配置，<br>
 * 这样用户只需要做一下数据库的初始化，配置一下数据库属性，就可以直接使用系统，后期还可以通过增加配置来扩展新功能。<br><br>
 * <p>
 * 所以数据库读写接口（参加 {@link cn.laoshini.dk.dao.IBasicDao}）并没有具体的实现类，设计中这应该交给具体项目来实现，<br>
 * 如键值对数据库访问对象（参见{@link cn.laoshini.dk.dao.IPairDbDao}）默认使用的是LevelDB，<br>
 * 关系型数据库读写功能（参见{@link cn.laoshini.dk.dao.IRelationalDbDao}）我是在单独的模块中实现的，目的是鼓励用户自己去实现，
 * 在需要时向项目中添加实现的依赖即可，并且可以通过配置项来选择使用哪个实现方式。<br>
 * </p>
 * <p>
 * 而用户有一个统一的调用入口类：{@link cn.laoshini.dk.dao.IDefaultDao}，这是一个可配置功能定义接口，其使用方式如下：
 * </p>
 * <pre>{@code
 *     @Service
 *     public class MyService {
 *
 *         // 方式1：使用注解自动注入（注入当前默认实现可以不指定实现key）：
 *         @FunctionDependent("implKey")
 *         private IDefaultDao defaultDao;
 *
 *         // 方式2：手动获取当前默认实现
 *         public IDefaultDao getCurrentDefaultDao() {
 *             return VariousWaysManager.getCurrentImpl(IDefaultDao.class);
 *         }
 *
 *         // 方式3：手动获取指定实现
 *         public IDefaultDao getDefaultDaoByKey(String implKey) {
 *             return VariousWaysManager.getFunctionImplByKey(IDefaultDao.class, implKey);
 *         }
 *     }
 * }</pre>
 * <p>
 * 关于可配置功能，具体可参见{@link cn.laoshini.dk.function}
 * </p>
 *
 * @author fagarine
 */
package cn.laoshini.dk.dao;

