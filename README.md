<h1 align="center">Java Object Searcher | java内存对象搜索辅助工具</h1>

<p align="center">
  <img title="portainer" src='https://img.shields.io/badge/version-0.1.0-brightgreen.svg' />
  <img title="portainer" src='https://img.shields.io/badge/java-1.7.*-yellow.svg' />
  <img title="portainer" src='https://img.shields.io/badge/license-MIT-red.svg' />
</p>


## 0x01 工具简介

```
#############################################################
   Java Object Searcher v0.01
   author: c0ny1<root@gv7.me>
   github: http://github.com/c0ny1/java-object-searcher
#############################################################
```

配合IDEA在Java应用运行时，对内存中的对象进行搜索。比如可以可以用挖掘request对象用于回显，辅助构造java内存webshell等场景。

## 0x02 知识储备

使用之前必须了解的三个概念

#### 2.1 搜索器
根据要搜索什么样的对象，选择对应的搜索器，目前项目有三类。

* JavaObjectSearcher 普通搜索器
* SearchRequstByBFS 通过广度优先搜索requst对象搜索器
* SearchRequstByRecursive 通过深度优先搜索requst对象搜索器(递归实现)

#### 2.2 关键字 & 黑名单

关键字是搜索目标对象的关键，可以目标三个属性`属性名`(field_name),`属性值`(field_value)和`属性类型`(field_type)。

比如想搜索属性名为table同时属性值为test的对象，还搜索属性名`request`同时属性类型包含`RequestInfo`关键字的，对应的逻辑表达试如下：

```$xslt
(field_name = table & field_value = test) || (field_name = request & field_type = RequestInfo)
```

编写代码如下：

```java
List<Keyword> keys = new ArrayList<>();
keys.add(new Keyword.Builder().setField_name("table").setField_type("test").build());
keys.add(new Keyword.Builder().setField_name("request").setField_type("RequestInfo").build());
```

黑名单是定义哪些属性中不可能存有要搜索的目标对象，防止无意义的搜索，浪费时间。如果把上面的例子当做黑名单，编写的代码也是类似的。

```java
List<Blacklist> blacklists = new ArrayList<>();
blacklists.add(new Blacklist.Builder().setField_name("table").setField_value("test").build());
blacklists.add(new Blacklist.Builder().setField_name("request").setField_type("RequestInfo").build());
```

## 0x03 使用步骤

**1. 将`java-object-searcher-<version>.jar`引入到目标应用的classpath中，或者可以放在jdk的ext目录(一劳永逸)**

**2. 编写调用代码搜索目标对象**

以搜索request对象为例，选好搜索器，并根据要搜索的目标特点构造好关键字(必须)和黑名单(非必须)，可写如下搜索代码到IDEA的`Evaluate`中执行。

```java
//设置搜索类型包含Request关键字的对象
List<Keyword> keys = new ArrayList<>();
keys.add(new Keyword.Builder().setField_type("Request").build());
//定义黑名单
List<Blacklist> blacklists = new ArrayList<>();
blacklists.add(new Blacklist.Builder().setField_type("java.io.File").build());
//新建一个广度优先搜索Thread.currentThread()的搜索器
SearchRequstByBFS searcher = new SearchRequstByBFS(Thread.currentThread(),keys);
// 设置黑名单
searcher.setBlacklists(blacklists);
//打开调试模式,会生成log日志
searcher.setIs_debug(true);
//挖掘深度为20
searcher.setMax_search_depth(20);
//设置报告保存位置
searcher.setReport_save_path("D:\\apache-tomcat-7.0.94\\bin");
searcher.searchObject();
```
关于新增的对象寻找：
```java
me.gv7.tools.josearcher.searcher.SearchObjectByBFS s = new me.gv7.tools.josearcher.searcher.SearchObjectByBFS(Thread.currentThread(),1905487603);
java.util.List<me.gv7.tools.josearcher.entity.Blacklist> blacklists = new java.util.ArrayList<>();
blacklists.add(new me.gv7.tools.josearcher.entity.Blacklist.Builder().setField_name("threads").build());
s.setReport_save_path("D:\\logs");
s.setBlacklists(blacklists);
s.setMax_search_depth(15);
s.searchObject();
s.getResult();
```


## 0x04 更多
* [半自动化挖掘request实现多种中间件回显](http://gv7.me/articles/2020/semi-automatic-mining-request-implements-multiple-middleware-echo/)
