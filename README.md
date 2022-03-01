# 介绍

flow-server-starter 项目主要功能如下：
* 初始化 Apache Camel Context
* 从数据表读取 JSON 格式的流程配置信息
* 部署流程

## 使用

Maven 项目中 pom.xml 这样引用依赖

```xml

<dependency>
    <groupId>org.tigerface.flow</groupId>
    <artifactId>flow-server-starter</artifactId>
    <version>${flow.version}</version>
</dependency>
```

## 流程格式

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
    ...
  ]
}
```















