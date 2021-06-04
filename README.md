# 介绍

flow-server-starter 项目是 `Flow Server` 的自动配置包。

Maven 项目中 pom.xml 这样引用依赖

```xml

<dependency>
    <groupId>org.tigerface.flow</groupId>
    <artifactId>flow-server-starter</artifactId>
    <version>${flow.version}</version>
</dependency>
```

## 功能

引用 flow-server-starter 的项目，可实现以下基本功能：

* 初始化 apache camel 上下文
* 通过 Http 接收上传，解析，部署 `Flow Json` 格式的流程定义
* 通过 RabbitMQ 接收管理消息，批量部署流程
* 部署的流程在运行时，会向 Elasticsearch 发送状态和监控消息

## `Flow Json` 格式说明

### 流程整体格式

  ```json
{
  "id": "flow_id_xxx",
  "name": "xxx",
  "desc": "流程说明",
  "version": "1.2.3",
  "status": "testing",
  "nodes": [
    {
      "id": "node_id_xxx",
      "type": "from",
      "props": {
        "comp": "rest",
        "method": "get",
        "path": "demo/{who}"
      }
    },
    {},
    {}
  ]
}
  ```

### `from` 流程入口节点格式

* `rest`：Rest Http 请求入口

    ```json
        {
          "id": "node_id_xxx",
          "type": "from",
          "props": {
            "comp": "rest",
            "method": "get",
            "path": "demo/{who}"
          }
        }
    ```
  生成的 dsl 是 `from("rest:<method>:<path>")`


* `direct`：内部调用入口

    ```json
        {
          "id": "node_id_xxx",
          "type": "direct",
          "props": {
            "comp": "direct"
          }
        }
    ```
  生成的 dsl 是 `from("direct:<flowName>")`

### 流程节点

* `to` 调用组件或内部流程

    ```json
        {
          "id": "node_id_xxx",
          "type": "to",
          "props": {
            "uri": "xxx"
          }
        }
    ```
  生成的 dsl 是 `.toD("<uri>")`

  *注意：是 `toD` 所以 uri 内可以包含表达式

* `transform` 转换器

    ```json
        {
          "id": "node_id_xxx",
          "type": "transform",
          "props": {
            "lang": "simple",
            "script": "${body}"
          }
        }
    ```
  生成的 dsl 是 `.transform(simple("<script>"))`
  注：参考 "使用编程语言"

### 使用编程语言（程序、脚本、表达式）

基本的引用如下

  ```json
    {
  "id": "node_id_xxx",
  "type": "transform",
  "props": {
    "lang": "simple",
    "script": "${body}"
  }
}
  ```

上面代码通过 `props` 属性指定怎么引用一段动态脚本。 其中，`lang` 指定语言类型，`script` 指定代码内容

类似格式定义的语言还有：

* constant
* simple
* spel
* jsonpath
* xpath
* groovy

另外几种，`props` 属性不同：

`header`

  ```json
      {
  "id": "node_id_xxx",
  "type": "transform",
  "props": {
    "lang": "header",
    "name": "xxx"
  }
}
  ```

生成的 dsl 是 `.transform(header("<name>"))`

`method`

  ```json
    {
  "id": "node_id_xxx",
  "type": "transform",
  "props": {
    "lang": "method",
    "ref": "fooService",
    "method": "fooMethod"
  }
}
  ```

生成的 dsl 是 `.transform(method("<ref>", "<method>"))`















