/**
 * 该包下定义当康系统游戏服务器相关功能和接口，在dk-core项目中，不实现具体的游戏业务，游戏相关基础业务在dk-game-core中实现
 * <p>
 * 关于当康系统游戏服网络交互的架构思路：<br>
 * </p>
 * 由于初衷是做一个拿来即用、可扩展、可配置、易上手的游戏服务器平台，所以一开始设计时，就考虑着网络连接要支持TCP、HTTP，甚至可能会有UDP；
 * <p><br>
 * 在消息格式上，则提供了三种选择：JSON、Protobuf以及自定义格式的消息，其中：<br>
 * 1. JSON对应的是DTO、VO、BO这样的JavaBean，我设想的使用场景是HTTP服务器，但是实际上应该没人这么用，太容易破解，简直在诱惑人犯罪；<br>
 * 2. Protobuf，这个用过的人都知道，来自于其本身的设计，自带一定的反破解能力，像Spring、Netty这些都对其有支持，看上去简直完美；
 * 但是，其配置和生成Java消息类是个很烦的过程（纯个人感受），另外，由于本项目主打不停服更新，这个问题就显得不是很友好了；<br>
 * 3. 自定义格式消息，这个在我看来具有无限的扩展性，但是对于不了解这方面的人，有一个学习的过程，要求比较高；
 * 但是，本系统会提供自定义消息格式的表达式，支持表达式生成消息或者通过表达式自动生成、编译、加载消息类文件，<br>
 * 综上，我比较倾向使用自定义消息；
 * 以上这些，用户都不需要太多了解，因为所有这些格式的消息都会被转换为{@link cn.laoshini.dk.net.msg.ReqMessage}进入Handler
 * </p>
 * <p><br>
 * 下面说Handler，本系统中关于网络交互中的Handler是指：专门处理消息的类，其顶层接口为:{@link cn.laoshini.dk.net.handler.IMessageHandler}<br>
 * 针对上面提到的三种消息格式，有三种不同的子接口，对于Handler类，想要系统实现自动检测、自动绑定消息id、自动调用其对象，必须满足以下条件：
 * </p>
 * <p>
 * 1. Handler类必须实现{@link cn.laoshini.dk.net.handler.IMessageHandler}接口，或者它的子接口；<br>
 * 2. Handler类必须添加@{@link cn.laoshini.dk.annotation.MessageHandle}注解，系统通过该注解实现handler与消息id的绑定；<br>
 * 3. Handler的实现类必须指定要处理的消息体的类型（通过泛型指定，消息体为空的除外），否则系统无法通过反射自动读取并填充消息体<br>
 * </p>
 *
 * @author fagarine
 * @see cn.laoshini.dk.net.handler.IMessageHandler
 * @see cn.laoshini.dk.net.msg.IMessage
 * @see cn.laoshini.dk.net.msg.AbstractMessage
 * @see cn.laoshini.dk.net.msg.ICustomDto
 * @see cn.laoshini.dk.net.msg.ICustomMessage
 * @see cn.laoshini.dk.net.msg.ReqMessage
 * @see cn.laoshini.dk.net.codec.IMessageCodec
 */
package cn.laoshini.dk.net;