## 项目介绍
### 项目名称来由
dangkang（当康），当康名字来源于《山海经》中的瑞兽，“﹝ 钦山 ﹞有兽焉，其状如豚而有牙，其名曰当康，其鸣自叫，见则天下大穰。”

### 项目简介
当康系统，是一个Java编写的游戏服务器框架，封装了一些常用的游戏服务器功能，为游戏服务器开发提供帮助。

### 已实现功能
- 快速启动游戏服务，让开发者只用关心业务逻辑
- 可配置功能支持
- 代码热修复和类编织支持
- 外置模块（jar包级别）的热插拔和更新
- 配置中心和GM后台
- 常用的游戏功能支持，如下
    + 简单的角度、碰撞计算
    + 敏感数据处理
    + 游戏进程内机器人功能
    + 随机名称生成器
    + 模拟游戏客户端
    + excel文件读写
    + GM功能支持
    + 基于数据库实现的全局ID自增器
    + 二维码读取和生成
    + 其他常用的工具类等

### 下一步计划
- 增加项目文档
- 增加示例项目
- 完善和优化已有功能，修补漏洞和BUG
- 提供UI操作界面
- 基于表达式或脚本的业务代码解释和执行，为不熟悉服务端开发的开发者提供支持

## 项目说明
### 项目结构
```text
dangkang
├── dk-agent -- 代码热修复和模块热更新基础支持
├── dk-autoconfigure -- 项目配置项自动配置支持，spring-boot项目使用
├── dk-common -- 通用工具类、常量、注解
├── dk-core -- 项目核心功能和部分游戏功能接口
├── dk-domain -- 项目中通用POJO类定义
├── dk-game-core -- 通用游戏功能支持
└── dk-impl-basic -- 常用工具和可配置功能的一些基础实现，新增可配置功能的基本实现都在该模块下添加
    ├── dk-basic-da -- 关于数据访问的一些基础实现，用于功能快速测试和验证，不建议用于实际项目
    ├── dk-basic-excel -- excel读写功能的基础支持，依赖POI
    ├── dk-config-center-jdbc -- 使用数据库实现的配置中心
    ├── dk-config-client -- 配置中心客户端支持（提供对非WEB项目和非spring boot项目的支持）
    ├── dk-db-id-incrementer -- 使用数据库实现的id自增器
    ├── dk-gm -- 当康游戏基础GM功能的实现
    ├── dk-gm-console -- 当康游戏GM后台功能的实现
    ├── dk-name-generator-cn -- 简体中文名称生成器，该项目生成的名称符合中文名传统起名习惯
    ├── dk-name-generator-foreign-cn -- 简体中文形式的外国人名称生成器实现项目
    └── dk-qr-code -- 二维码生成和解析功能
```
### 模块说明
- dk-agent
    - 使用javaagent，实现代码的热修复
    - 使用并增强ClassFileTransformer，提供热修复功能的快速扩展
    - 为组件注入提供支持
- dk-autoconfigure
    - 支持当康系统配置参数的IDE快速配置
- dk-common
    - 声明项目中通用的注解
    - 提供常用的工具类
    - 部分通用常量声明
- dk-core
    - 支持可配置功能使用和注入，[详见function包](/dk-core/src/main/java/cn/laoshini/dk/function/package-info.java)
    - 支持外置模块的热插拔和更新，[详见module包](/dk-core/src/main/java/cn/laoshini/dk/module/package-info.java)
    - 提供网络服务基础支持，[详见net包](/dk-core/src/main/java/cn/laoshini/dk/net/package-info.java)
    - 支持游戏服务器线程的快速启动，[详见register包](/dk-core/src/main/java/cn/laoshini/dk/register/Registers.java)
    - 提供SPEL表达式和JavaScript的解析和执行，[详见expression包](/dk-core/src/main/java/cn/laoshini/dk/expression/package-info.java)
    - 提供代码及时编译和部分游戏代码的生成，[详见jit包](/dk-core/src/main/java/cn/laoshini/dk/jit/package-info.java)
    - 提供项目快速启动接口，[详见starter包](/dk-core/src/main/java/cn/laoshini/dk/starter/package-info.java)
    - 其他一些通用的如应用内缓存、数据访问、executor、敏感信息处理等功能
- dk-domain
    - 项目中通用POJO类定义
- dk-game-core
    - 模拟游戏客户端，[详见client包](/dk-game-core/src/main/java/cn/laoshini/dk/client/package-info.java)
    - 提供可直接使用的游戏服务器功能，[详见server包](/dk-game-core/src/main/java/cn/laoshini/dk/server/package-info.java)
    - 提供机器人功能实现，包含行为树和有限状态机，[详见robot包](/dk-game-core/src/main/java/cn/laoshini/dk/robot/package-info.java)
    - 其他游戏中常用功能
- dk-impl-basic 
    - 提供一些非必要功能的独立实现

## 使用示例
### 快速启动
引入当康项目的github仓库
```xml
<repositories>
    <repository>
        <id>dangkang-repo</id>
        <url>https://raw.github.com/fagarine/dangkang/mvn-repo</url>
        <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
        </snapshots>
    </repository>
</repositories>
```
引入项目依赖
```xml
<dependency>
    <groupId>cn.laoshini.dk</groupId>
    <artifactId>dk-core</artifactId>
    <version>${dangkang.version}</version>
</dependency>
```
只启动当康容器和基础功能支持
```java
public class GameStartMain {
    public static void main(String[] args){
        // 项目包路径前缀，用于组件扫描和功能注入等
        String[] packages = new String[] {"com.company.xxx"};

        // 自己项目中没有使用Spring
        DangKangStarter.startByNonSpring(packages);
        // 或者
        DangKangGameStarter.get().packagePrefixes(packages).start();

        // 如果项目中使用了Spring，则应该使用当康系统来加载Spring配置文件
        ApplicationContext context = DangKangStarter.startBySpringXmlFile(packages, "classpath:applicationContext.xml");
        // 或者
        ApplicationContext context = DangKangGameStarter.get().packagePrefixes(packages).springConfigs("classpath:applicationContext.xml").start();
    }
}
```
快速启动游戏服务器线程
```java
public class GameStartMain {
    public static void main(String[] args){
        // 项目包路径前缀，用于组件扫描和功能注入等
        String[] packages = new String[] {"com.company.xxx"};

        ApplicationContext context = DangKangGameStarter.get().packagePrefixes(packages)
            // 添加一个TCP服务器线程注册器，具体功能详见IGameServerRegister
            .gameServer(Registers.newTcpGameServerRegister().setPort(9420).setGameName("游戏名称"))
            // 添加一个游戏消息类注册器，系统将通过注册的消息超类，扫描其所有子类，自动识别消息
            .message(Registers.newDangKangCustomMessageRegister())
            // 添加一个消息处理类注册器，系统通过消息id与扫描到的Handler关联，当消息到达时，自动识别并执行对应的handler逻辑
            .messageHandler(Registers.newDangKangMessageHandlerRegister())
            // 如果项目使用了Spring，填写Spring配置文件路径
            .springConfigs("classpath:applicationContext.xml")
            .start();

        // 可以通过如下方法，获取本进程中所有已注册的游戏服务器id
        GameServers.getAllServerId();
    }
}
```
游戏服功能[详见net包](/dk-core/src/main/java/cn/laoshini/dk/net/package-info.java)

如果要使用部分游戏特有功能，还需要引入dk-game-core依赖
```xml
<dependencies>
    <dependency>
        <groupId>cn.laoshini.dk</groupId>
        <artifactId>dk-core</artifactId>
        <version>${dangkang.version}</version>
    </dependency>
    <dependency>
        <groupId>cn.laoshini.dk</groupId>
        <artifactId>dk-game-core</artifactId>
        <version>${dangkang.version}</version>
    </dependency>
</dependencies>
```
其中已支持的功能：
- 游戏客户端模拟，使用netty实现
- 用户id、角色id自动生成的基本实现，使用时依赖id自增器功能
- 机器人（包括行为树和有限状态机两种形式）功能的支持

### 关于热修复
最小依赖方式，添加dk-agent依赖，启动项中，添加javaagent参数，如：java -cp -javaagent:dk-agent-1.0.0-SNAPSHOT.jar -jar game.jar GameStartMain
```xml
<dependency>
    <groupId>cn.laoshini.dk</groupId>
    <artifactId>dk-agent</artifactId>
    <version>${dangkang.version}</version>
</dependency>
```
以下代码示范如何调用代码热修复功能
```java
public class GameStartMain {
    public static void main(String[] args){
        // 如果使用最小依赖dk-agent，可通过如下方法热修复代码
        DangKangAgent.redefineClass(clazz, bytes);
        
        // 如果添加了dk-core依赖，但是未启动当康容器，可以直接使用工具类热修复代码
        HotfixUtil.redefineClass(hotfixFile);

        // 如果启动了当康系统容器，添加class文件到热修复文件目录后，通过以下方式调用热修复功能（热修复代码class文件默认存放目录为项目根目录/hotfix目录下；这个目录名可配置，配置项名称为：dk.hotfix）
        SpringContextHolder.getBean(HotfixManager.class).doHotfix("unique key");
    }
}
```

### 关于外置模块
- 外置模块是指项目功能独立于项目启动包之外，可以在项目启动后热插拔与更新的功能模块
- 外置模块以jar为载体，一个jar包对应一个模块
- 外置模块的jar包默认放在项目根目录下/modules目录，该目录可通过配置项（dk.module）设置

注意：要使用外置模块功能，需要先保证当康系统容器已启动成功，外置模块功能通过ModuleManager类调用

### 关于可配置功能
#### 说明
可配置功能，是为了实现功能的多样化实现，以及依赖功能的自动注入，以及功能实现快速选择。

#### 可配置功能的使用
- 声明配置功能接口
- 添加@ConfigurableFunction注解，并通过注解中的key()指定一个唯一的key，这个key也是通过配置项选择功能实现对象的配置项名称
- 给接口添加实现类
- 给实现类添加@FunctionVariousWays注解，如果不是默认实现，需要通过value()指定实现对象的名称
- 添加可配置功能依赖，通过全局变量方式添加，且需要给变量添加@FunctionDependent注解

注意：该功能需要在启动了当康系统容器时才能生效，具体[详见function包](/dk-core/src/main/java/cn/laoshini/dk/function/package-info.java)

### 关于配置中心
- 配置中心服务端是个springboot项目，可直接打包启动，依赖于数据库，已提供基本的游戏服配置信息处理接口，用户可在该基础上丰富更多功能。
- 配置中心客户端是个普通项目，可直接添加到项目依赖中，例如：
  ```xml
    <dependency>
        <groupId>cn.laoshini.dk</groupId>
        <artifactId>dk-config-client</artifactId>
        <version>${dangkang.version}</version>
    </dependency>
  ```
  其启动时依赖了几个配置项，具体配置项见DangKangConfigCenterProperties类，默认配置文件名为bootstrap.properties或config-client.properties，也可以是yaml文件，如果用户要使用自定义的文件名，需要通过如下方式指定：
  ```java
    public class GameStartMain {
        public static void main(String[] args){
            DangKangGameStarter.get()
            // 使用自定义配置文件作为配置中心客户端配置文件
            .configClientFile("xxx.properties")
            .packagePrefixes(packages).springConfigs("classpath:applicationContext.xml").start();
        }
    }
  ```
  如果想要使用配置信息的自动刷新功能，则需要在依赖了配置项的类上添加@RefreshScope注解，当配置信息更新后，通过配置中心服务器${contextPath}/game/server/refresh网络接口发起刷新操作。
  需要注意的是，配置中心的自动刷新依赖于MQ，系统默认使用rabbitmq

### 关于姓名生成器
添加依赖，中文名称生成器依赖：
```xml
<dependency>
    <groupId>cn.laoshini.dk</groupId>
    <artifactId>dk-name-generator-cn</artifactId>
    <version>${dangkang.version}</version>
</dependency>
```
中文形式的外国人名称生成器依赖：
```xml
<dependency>
    <groupId>cn.laoshini.dk</groupId>
    <artifactId>dk-name-generator-foreign-cn</artifactId>
    <version>${dangkang.version}</version>
</dependency>
```
使用示例：
```java
public class PlayerService {
    @FunctionDependent
    private Func<INameGenerator> nameGenerator;

    private List<String> batchName() {
        // 批量生成姓名，一次返回6个
        return nameGenerator.get().batchName(6);
    }
    
    private String newName() {
        // 随机生成并返回一个名称
        return nameGenerator.get().newName();
    }
}
```
更多细节可以查看[INameGenerator](/dk-core/src/main/java/cn/laoshini/dk/generator/name/INameGenerator.java)类和其实现类

### 工具类
- 日志工具类，当调用其中的方法记录日志时，会自动记录下调用类和方法名以及行号等信息，使用方式如下：
  ```text
      LogUtil.debug("就是想打个日志");
      LogUtil.info("游戏服[{}]开始启动", "我的游戏");
      LogUtil.error("这是个异常", e);
      LogUtil.c2sMessage("客户端到达消息:{}", message);
  ```
  要保证日志能正确输出，需在日志配置文件中添加一个名为"cn.laoshini.dk"的logger；
  需要注意的是，游戏交互消息日志独立配置，需要单独添加一个名为"DK_MESSAGE"的logger。
  具体详见[LogUtil](/dk-common/src/main/java/cn/laoshini/dk/util/LogUtil.java)
- Class工具类，提供了一系列批量查找类的方法，具体详见[ClassUtil](/dk-common/src/main/java/cn/laoshini/dk/util/ClassUtil.java)
- 反射工具类，提供了一些常用的反射工具，具体详见[ReflectUtil](/dk-common/src/main/java/cn/laoshini/dk/util/ReflectUtil.java)和[ReflectHelper](/dk-core/src/main/java/cn/laoshini/dk/util/ReflectHelper.java)
- JSON文件读写工具类，提供了简单的JSON文件读写功能，具体详见[JsonUtil](/dk-core/src/main/java/cn/laoshini/dk/util/JsonUtil.java)
- Excel文件读写工具类，提供了简单的Excel文件读写功能，具体详见[ExcelUtil](/dk-impl-basic/dk-basic-excel/src/main/java/cn/laoshini/dk/excel/ExcelUtil.java)
- 二维码生成和解析工具类，提供了简单的二维码图片读写功能，具体详见[QrCodeUtil](/dk-impl-basic/dk-qr-code/src/main/java/cn/laoshini/dk/qrcode/QrCodeUtil.java)

更多的工具类详见[dk-common的util包](/dk-common/src/main/java/cn/laoshini/dk/util)和[dk-core的util包](/dk-core/src/main/java/cn/laoshini/dk/util)

### 特别说明
由于该项目前后断断续续数次重构，但是项目中留有部分已不用的类未清除，且重构后未能得到验证，所以可能有些地方显得多余或不合理，后期会找时间清理一遍。

## 其他
欢迎star/fork，欢迎学习/使用/交流，期待你的加入和贡献代码！
