# 钉当软件设计架构

## 软件架构

钉当的软件架构图如下所示：

![软件架构图](http://gitlab.alibaba-inc.com/dingdang/dingdang/raw/Dean/resources/software.png)
    
图中白色的组件为所用的外部服务，主要包含三个服务：钉钉机器人消息接口服务、Notify消息推送服务、rabbitmq消息推送服务和数据库服务：

- 钉钉机器人消息接口服务
    
    这是整个应用与钉钉机器人对接的接口，虽然称为“钉钉机器人消息接口服务”，然而事实上是应用为钉钉机器人提供服务。钉钉机器人提供了将消息通过HTTP协议转发至指定的服务，并将返回的消息（遵循一定格式）格式化后发到聊天群中。

- 应用生命周期消息推送服务

    钉当的一大核心功能就是监听应用的生命周期事件，当应用构建、创建变更、发布部署、写基线等事件开始和完成时，钉当能够将这些事件及时推送至钉钉群，提醒用户。目前钉当使用[Aone envCenter和Aone Mix通过Notify提供的消息推送服务以及海浪发布系统通过rabbitmq提供的发布状态推送服务](http://docs.alibaba-inc.com/pages/viewpage.action?pageId=450856431#)获取相应的事件消息。

- 数据库服务

    为了实现应用管理、聊天群匹配和消息历史存储，需要使用数据库服务。
    
## 钉当交互类型

钉当主要包含两条消息路径，对应两种业务流程，一是主动推送式，一是请求响应式。其中`MessageHandler`是负责消息收发的中转站。`MessageHandler`主要提供了两个接口：

```
public boolean pushMessage(String message, long appId, String lifeCycle)
```

```
public String receiveMessage(String message, String groupKey, String instructorId, String instructorName)
```

分别对应主动推送式和请求响应式两种消息路径。

当用户主动`@机器人`时，消息会经`receiveMessage`传给适当的处理器（在这里即`AppManager`)，并将处理器返回的字符串格式化后返回给机器人，作为聊天消息的回复。
当机器人主动推送消息至聊天群中时，消息经由`pushMessage`通过`web hook`的方式推送至聊天群中。

值得一提的是，Spring只存在于请求响应式的消息路径中（这里指spring提供的web服务），这是因为请求响应式主要通过web服务实现，spring为web服务提供了良好的支持，但主动推送式是通过`web hook`的方式即以HTTP协议向指定的url发起`POST`请求来实现的，不需要用到Spring的功能。

---

下面针对两种消息路径说明其中的组件功能和相互之间的消息传递。

- 主动推送式

EventListener组件通过Notify服务监听事件，当事件到来时，EventListener将解读其中的json文本，提取摘要信息，并格式化为可向用户展示的字符串，将字符串传递给MessageHandler，由其负责将消息推送到聊天群中。
    
注意到，pushMessage接口除了消息参数以外还有两个参数，一是appId，一是lifeCycle。appId参数即应用的id，目的是为了匹配聊天群。EventListener监听到的每个事件都对应一个应用，推送消息时需要将该应用id传递给MessageHandler，而MessageHandler以该应用id为索引，查找数据库中订阅该应用的所有聊天群的web hook url，将消息以web hook的方式推送至所有订阅该应用的聊天群中。
    
- 请求响应式

在请求响应式的业务流程中，由用户在聊天群中主动发起请求并@聊天机器人。配置了外部服务后，机器人将该消息转发至配置的服务（通过HTTP协议的GET请求）。这一web服务由Spring提供，Spring的主要路由入口如下：
```
@RequestMapping("/request")
public String request(@RequestParam(value="sys.用户输入", required = false)String args,
                      @RequestParam(value="sys.ding.conversationId") String groupKey,
                      @RequestParam(value="sys.ding.senderId") String instructorId,
                      @RequestParam(value="sys.ding.senderNick") String instructorName)
```
    
这四个参数由钉钉机器人提供，分别对应用户输入的指令，聊天群id，用户id和用户昵称。Spring接收到请求后将请求连带参数转发给MessageHandler（通过上述的receiveMessage函数）。MessageHandler收到消息后先查看聊天群的web hook是否配置（当前只支持手动配置web hook，添加机器人后应当第一时间配置web hook），如果没有配置则结束流程并提示用户配置web hook。如果用户配置了web hook，则将消息再转发给AppManager.

AppManager则负责应用的订阅和管理，首先，为了在群里收到应用生命周期事件的消息推送，需要在AppManager进行订阅（如果不想再收到可以退订），用户可以通过指令`subscribe app [appid\appname]通过应用id或应用名进行订阅。AppManager收到MessageHandler传递过来的消息后先提取其中的app参数（可能是id，可能是名字），查看对应的应用是否订阅（通过查询数据库），如果没有则结束流程并提示用户先订阅应用。如果应用已订阅，再将消息转发给InstructionManager。

InstructionManager则负责指令的归档、查询和转发。为了方便用户对指令历史的管理，钉当提供两种归档和查询方法。一种称为lifeCycle，对应应用的生命周期事件，一种称为topic，对应用户自定义的主题。当用户通过指令发起应用生命周期内的命令时（如构建、部署等），InstructionManager自动为指令归档入对应的lifeCycle中。另外，钉当支持自定义主题，方便用户自定义归档方式，通过在指令后加上"$[topic]"，钉当会将该指令归档入该topic下，用户便可以通过该topic查询到该指令。最后，钉当为指令寻找匹配的执行器，如果执行器存在，则将消息转发给执行器，由执行器执行具体的操作，否则提示指令错误。

考虑到之后可能的扩展，用一个指令执行器工厂InstructionHandlerFactory负责匹配与指令对应的执行器。目前这一映射使用HashMap方式硬编码，之后可以改为配置的方式，更灵活地匹配执行器。

可以看到，整个过程非常类似委托者模式，只是没有明确写出一个委托者接口和管理者类，因为各组件之间没有委托者之间的并列关系。各层组件收到消息后提取需要的信息，进行消息审核和校验（比如确认应用订阅），无误后才将消息再转发至下一层。

具体的类UML图如下：

![UML](http://gitlab.alibaba-inc.com/dingdang/dingdang/raw/Dean/resources/UML.png)

主要细化了dao层和domain层，标注黄色背景的是dao对象，标注绿色背景的则是领域对象，领域对象主要包含聊天群ChatGroup，订阅关系Subscription，指令Instruction和钉当推送的消息Notification。各对象的主要属性及含义如下：

- ChatGroup
    - groupKey: 对应群的唯一key，由钉钉提供，是一串字符串
    - webHookUrl: 对应群的web hook，由用户配置
    - (id): 只存在于数据库中，仅用来索引

- Subscription
    - chatGroupId: 对应聊天群的id，通过该id可索引对应的群
    - appId: 订阅的应用id
    - appName: 订阅的应用名称
    
- Instruction
    - instructorId: 指令发起者的钉钉id，由机器人提供，通过该id可@对应的用户
    - instructorName: 指令发起人的钉钉昵称
    - instruction: 指令内容
    - time: 指令发起时间
    - appId: 指令涉及的应用id（如果不对应某一应用则为-1）
    - appName: 指令涉及的应用名称（如果不对应某一应用则为空）
    - lifeCycle: 指令对应的生命周期（如果没有则为空）
    - topic: 指令自定义的主题（如果没有则为空）
    
- Notification
    - message: 推送的消息内容
    - appId: 对应的应用id
    - appName: 对应的应用名称
    - lifeCycle: 对应的生命周期
    - groupId: 对应的群id
    - time: 推送时间