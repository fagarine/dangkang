## 项目介绍
### 项目名称来由
dangkang（当康），当康名字来源于《山海经》中的瑞兽，“﹝ 钦山 ﹞有兽焉，其状如豚而有牙，其名曰当康，其鸣自叫，见则天下大穰。”

### 项目简介
当康系统，是一个Java编写的游戏服务器框架，封装了一些常用的游戏服务器功能，为游戏服务器开发提供支持。

### 项目目标
- 提供可扩展的功能和模块
- 提供项目的不停服热更代码
- 支持使用脚本实现业务代码
- 支持模块（jar包级别）的热插拔和更新

## 项目说明
### 项目结构
```text
dangkang
├── dk-agent -- 代码热修复和模块热更新基础支持
├── dk-autoconfigure -- 项目配置项自动配置支持
├── dk-common -- 通用工具类、常量、注解
├── dk-core -- 项目核心功能和部分游戏功能接口
├── dk-game-core -- 通用游戏功能支持
├── dk-game-starter -- 项目整合依赖和快速启动服务接口
└── dk-impl-basic -- 可配置功能的一些基础实现，新增可配置功能的基本实现都在该模块下添加
    └── dk-basic-da -- 关于数据访问的一些基础实现
    └── dk-basic-excel -- excel读写功能的基础支持
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
    - 其他一些通用的如应用内缓存、数据访问、executor等功能
- dk-game-core
    - 提供可直接使用的游戏服务器功能
    - 提供机器人功能实现，包含行为树和有限状态机
    - 其他游戏中常用功能
- dk-game-starter
    - 提供项目快速启动接口
- dk-impl-basic
    - 提供一些非必要功能的独立实现

## 使用示例
### 快速启动
引入项目依赖
```xml
<dependency>
    <groupId>cn.laoshini.dk</groupId>
    <artifactId>dk-game-starter</artifactId>
    <version>${dangkang.version}</version>
</dependency>
```
只启动当康容器和基础功能支持
```java
public class GameStartMain {
    public static void main(String[] args){
        // 只需要最基础的功能（不包含当康系统容器），这个最好是放在应用启动main方法的第一行
        DangKangStarter.engineStart();
        // 上面的方法被调用时会自动将dk-agent包加载到javaagent，如果项目中已经指定了javaagent，可以通过以下方法指定：
        DangKangGameStarter.setInstrumentation(instrumentation);
        // 上面两种方式，取其一种使用即可

        // 项目包路径前缀，用于组件扫描和功能注入等
        String[] packages = new String[] {"com.company.xxx"};

        // 使用下面方式，会启动当康系统容器，支持的功能更多（比如可配置功能的自动注入等）
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
如果要使用部分游戏特有功能，还需要引入dk-game-core依赖
```xml
<dependencies>
    <dependency>
        <groupId>cn.laoshini.dk</groupId>
        <artifactId>dk-game-starter</artifactId>
        <version>${dangkang.version}</version>
    </dependency>
    <dependency>
        <groupId>cn.laoshini.dk</groupId>
        <artifactId>dk-game-core</artifactId>
        <version>${dangkang.version}</version>
    </dependency>
</dependencies>
```
启动代码如下
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

        // 可以通过如下方法，获取所有运行中的游戏服务器id
        GameServers.getAllServerId();
    }
}
```
具体功能[详见net包](/dk-core/src/main/java/cn/laoshini/dk/net/package-info.java)

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
        // 热修复代码class文件存放目录为项目根目录/hotfix目录下；这个目录名可配置，配置项名称为：dk.hotfix

        // 如果未启动当康系统容器，可以直接使用工具类热修复代码
        HotfixUtil.redefineClass(hotfixFile);

        // 如果启动了当康系统容器，添加class文件到热修复文件目录后，通过以下方式调用热修复功能
        SpringContextHolder.getBean(HotfixManager.class).doHotfix("unique key");
    }
}
```

### 关于外置模块
- 外置模块是指项目功能独立于项目启动包之外，可以在项目启动后热插拔与更新的功能模块
- 外置模块以jar为载体，一个jar包对应一个模块
- 外置模块的jar包默认放在项目根目录下/modules目录，可通过配置项（dk.module）设置

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

## 其他
欢迎star/fork，欢迎学习/使用/交流，期待你的加入和贡献代码！
