package org.tigerface.flow.starter.route;

import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
//import org.apache.camel.component.elasticsearch.ElasticsearchComponent;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.tigerface.flow.starter.domain.Message;

import java.util.List;

import static org.apache.camel.language.groovy.GroovyLanguage.groovy;

public class SystemRoutes extends RouteBuilder {
//    private int port = 8086;

    @Autowired
    CamelContext camelContext;

    @Value("${flow.git.tmpdir}")
    private String path;

    @Value("${flow.git.url}")
    private String gitUrl;

    @Value("${flow.git.username}")
    private String gitUser;

    @Value("${flow.git.password}")
    private String gitPwd;

    @Value("${flow.command.queue}")
    private String commandQueue;

//    @Value("${flow.elasticsearch.url}")
//    private String esUrl;
//
//    @Value("${flow.elasticsearch.username:elastic}")
//    private String esUser;
//
//    @Value("${flow.elasticsearch.password}")
//    private String esPwd;

    @Override
    public void configure() throws Exception {
//        ElasticsearchComponent esComp = new ElasticsearchComponent();
//        esComp.setHostAddresses(esUrl);
//        esComp.setUser(esUser);
//        esComp.setPassword(esPwd);

//        camelContext.addComponent("elasticsearch-rest", esComp);
        /**
         * 开启全局StreamCaching
         * 若单独flow不需要StreamCaching，则在流程中用noStreamCaching来关闭当前flow的StreamCaching
         */
        camelContext.setStreamCaching(true);

        restConfiguration()
                .component("jetty")
                .port(8086)
                .enableCORS(true)
                .corsAllowCredentials(true)
//                .bindingMode(RestBindingMode.off)
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "Flow Server").apiProperty("api.version", "1.0.0")
                .apiProperty("cors", "true");

        // 基础流程，系统基本状态
        rest("/").get().description("引导入口").route()
                .transform().simple("这是一个 Flow Server，你可以通过 POST ../deploy 来部署一个流程。")
                .setHeader("Content-Type", constant("application/json; charset=UTF-8"))
                .group("系统流程").setId("Who");

        // Rest 部署流程入口
        rest("/flow")
                .description("流程服务")
                .consumes("application/json").produces("application/json")
                .post().description("部署流程")
                .param().name("flow").description("流程信息").type(RestParamType.body).endParam()
                .route()
                .to("direct:deploy")
                .setHeader("routeId", simple("${body}"))
                .to("direct:saveFlowToDB")
                .wireTap("direct:flow-node-synchronous")
                .end()
                .setBody(groovy("[errorCode:0, msg:'部署完成']"))
                .marshal().json()
                .setHeader("Content-Type", constant("application/json; charset=UTF-8"))
                .group("系统流程").setId("DeployFlow");

        // Rest 获取流程信息
        rest("/flow")
                .description("流程服务")
                .consumes("application/json").produces("application/json")
                .get("/{routeId}").description("获取流程信息")
                .param().name("routeId").description("流程 id").type(RestParamType.path).endParam()
                .route()
                .setBody(header("routeId"))
                .bean("deployService", "getFlow")
                .marshal().json()
                .setHeader("Content-Type", constant("application/json; charset=UTF-8"))
                .group("系统流程").setId("GetFlowInfo");
        // Rest 列出流程信息
        rest("/flows")
                .description("流程服务")
                .consumes("application/json").produces("application/json")
                .get().description("列出全部流程").outType(List.class)
                .route()
                .bean("deployService", "listFlows")
                .marshal().json()
                .setHeader("Content-Type", constant("application/json; charset=UTF-8"))
                .group("系统流程").setId("ListFlows");

        // Rest 列出流程信息
        rest("/subflows")
                .description("子流程服务")
                .consumes("application/json").produces("application/json")
                .get().description("列出全部子流程").route()
                .bean("deployService", "listDirectFlows")
                .marshal().json()
                .setHeader("Content-Type", constant("application/json; charset=UTF-8"))
                .group("系统流程").setId("ListSubFlows");

        // Rest 删除流程入口
        rest("/flow")
                .description("流程服务")
                .consumes("application/json").produces("application/json")
                .delete("/{routeId}").description("根据 id 删除流程").outType(Message.class)
                .param().name("routeId").description("流程 id").type(RestParamType.path).endParam()
                .route()
                .group("系统流程").id("RemoveFlow")
                .log("---remove-- \n${header.routeId}")
                .setBody(simple("${header.routeId}"))
                .bean("deployService", "remove")
                .choice().when().simple("${header.routeId} != null")
                .to("direct:removeFlowToDB")
                .wireTap("direct:flow-node-synchronous")
                .end()
                .setBody(groovy("[errorCode:0, msg:'删除完成']"))
                .otherwise()
                .setBody(groovy("[errorCode:1, msg:'参数 routeId 无效']"))
                .end()
                .setHeader("Content-Type", constant("application/json; charset=UTF-8"))
                .marshal().json();


        // 文件部署流程
        from("file://" + path + "?charset=utf-8&recursive=true&delete=true&include=.*\\.json$&exclude=package.json")
                .log(LoggingLevel.DEBUG, "---deploy-- \n${body}")
                .transform().method("deployService", "deploy")
                .routeGroup("系统流程").description("文件部署流程").setId("DeployFlowFromPath");

        // 部署主流程
        from("direct:deploy")
                .to("micrometer:counter:deployCounter")
                .group("系统流程").description("部署主流程").id("MainDeployFlow")
                .log(LoggingLevel.DEBUG, "---deploy-- \n${body}")
                .bean("deployService", "deploy");

        // 语法检查
        rest("/groovySyntaxCheck").post().route()
                .bean("deployService", "syntaxCheck")
                .group("系统流程").setId("groovySyntaxCheck");

        // 从DB查询全部流程，入口条件：无
        from("direct:listFlowsFromDB")
                .log("从数据库装入已存在的流程 {{flow.server}}")
                .setBody(simple("SELECT ROUTE_ID as 'routeId', FLOW_SERVER as 'server', FLOW_URI as 'uri', FLOW_GROUP as 'group', FLOW_DESC as 'desc', FLOW_JSON as 'json' FROM T_FLOWS WHERE FLOW_SERVER = '{{flow.server}}'"))
                .log(LoggingLevel.DEBUG, "\nSQL >>> ${body}")
                .to("jdbc:dataSource");

        // 从DB查询流程，入口条件: ${header.routeId}
        from("direct:getFlowFromDB")
                .setBody(simple("SELECT ROUTE_ID, FLOW_SERVER, FLOW_URI, FLOW_GROUP, FLOW_DESC, FLOW_JSON, CREATE_TIME, UPDATE_TIME FROM T_FLOWS " +
                        "WHERE ROUTE_ID = '${header.data[routeId]}'"))
                .log(LoggingLevel.DEBUG, "\ngetFlowFromDB\nSQL >>> ${body}")
                .to("jdbc:dataSource");
//
        // 插入新流程
        from("direct:addFlowToDB")
                .setBody(simple("INSERT INTO T_FLOWS (ROUTE_ID, FLOW_SERVER, FLOW_URI, FLOW_GROUP, FLOW_DESC, FLOW_JSON, CREATE_TIME, UPDATE_TIME)" +
                        " VALUES ('${header.data[routeId]}', '{{flow.server}}', '${header.data[uri]}', '${header.data[group]}', '${header.data[desc]}', :?data_json, now(), now())"))
                .log(LoggingLevel.DEBUG, "\naddFlowToDB\nSQL >>> ${body}")
                .to("jdbc:dataSource?useHeadersAsParameters=true");
//
        // 更新新流程
        from("direct:modifyFlowToDB")
                .setBody(simple("UPDATE T_FLOWS SET FLOW_SERVER = '{{flow.server}}', FLOW_URI = '${header.data[uri]}', FLOW_GROUP = '${header.data[group]}', FLOW_DESC = '${header.data[desc]}'," +
                        "FLOW_JSON = :?data_json, UPDATE_TIME = now() WHERE ROUTE_ID = '${header.data[routeId]}'"))
                .log(LoggingLevel.DEBUG, "\nmodifyFlowToDB\nSQL >>> ${body}")
                .to("jdbc:dataSource?useHeadersAsParameters=true");
//
        // 删除流程
        from("direct:removeFlowToDB")
                .setBody(simple("DELETE FROM T_FLOWS WHERE ROUTE_ID = '${header.routeId}'"))
                .log(LoggingLevel.INFO, "\nremoveFlowToDB\nSQL >>> ${body}")
                .to("jdbc:dataSource");

        // 启动时调用从数据库装入
        from("spring-event:AvailabilityChangeEvent")
                .filter().groovy("body instanceof org.springframework.boot.availability.AvailabilityChangeEvent")
                .filter().groovy("'CORRECT' == body.getState().toString()")
                .to("direct:initTerminal")
                .to("direct:loadFlows")
                .end();

        from("direct:initTerminal")
                .bean("terminalService", "initTerminal")
                .end();

        // 从数据库装入
        from("direct:loadFlows")
                .to("direct:listFlowsFromDB")
                .split(simple("${body}"))
                .log(LoggingLevel.DEBUG, "\nbody >>> \n${body}")
                .log(LoggingLevel.INFO, "数据来源：DB")
                .log(LoggingLevel.INFO, "数据ID：${body[routeId]}")
                .setBody(simple("${body[json]}"))
                .filter(groovy("!body.startsWith('{')")).transform().groovy("new String(Base64.getDecoder().decode(body.getBytes('UTF-8')))")
                .end()
                .filter().simple("${body} != null && ${body.length} > 0")
                .log(LoggingLevel.DEBUG, "---deploy-- \n${body}")
                .transform().method("deployService", "deploy")
                .end()
                .routeGroup("系统流程").description("数据库部署流程").routeId("DeployFlowFromDB");

        // 保存数据库
        from("direct:saveFlowToDB")
                .bean("deployService", "getFlow")
//                .claimCheck().operation("SET").key("flow")
                .setHeader("data_json", simple("${body[flow]}"))
//                .setBody(simple("${body[flow]}"))
//                .transform().groovy("Base64.getEncoder().encodeToString(body.getBytes('UTF-8'))")
//                .setHeader("flow", simple("${body}"))
//                .claimCheck().operation("GET").key("flow")
                .setHeader("data", groovy("[routeId:body.routeId, uri:body.uri, group:body.group, desc:body.desc, json:headers.flow]"))
                .log(LoggingLevel.DEBUG, "\nFor Query\n ${header.data}")
                .to("direct:getFlowFromDB")
                .log(LoggingLevel.DEBUG, "\nQuery Result:\n ${body}")
                .choice()
                .when(simple("${body.empty}"))
                .log(LoggingLevel.DEBUG, ">>> ${body}")
                .to("direct:addFlowToDB")
                .otherwise()
                .to("direct:modifyFlowToDB")
                .endChoice()
                .end();

//        // mq 发布测试
//        from("rest:post:mq")
//                .convertBodyTo(String.class)
//                .log("**** body **** ${body}")
//                .to("spring-rabbitmq:default?routingKey=flow")
//                .setHeader("Content-Type", constant("application/json; charset=UTF-8"))
//                .setId("SendHeartBeat");
//
//        // mq 订阅测试
//        from("spring-rabbitmq:flow.topic?queues=" + commandQueue)
//                .unmarshal().json(Map.class)
//                .log("**** from mq **** ${body}")
//                .to("direct:git")
//                .setBody(constant("{\"msg\":\"OK\"}"))
//                .setId("SubScribeCommandQueue");
//
//        // redis 读测试
//        rest().get("/redis?key={a}").route()
//                .setHeader("CamelRedis.Command", constant("GET"))
//                .setHeader("CamelRedis.Key", header("key"))
//                .log("****key**** ${CamelRedis.Key}")
//                .to("spring-redis:localhost:6379?redisTemplate=#redisTemplate")
//                .log("****body**** ${body}")
//                .setHeader("Content-Type", constant("text/html; charset=UTF-8"))
//                .setId("GetFromRedis");
//
//        // redis 写测试
//        rest("/redis").post().consumes("application/json").route().log("******** ${body}")
//                .setHeader("CamelRedis.Command", constant("SET"))
//                .setHeader("CamelRedis.Key")
//                .jsonpath("$.Key", false, String.class)
//                .setHeader("CamelRedis.Value")
//                .jsonpath("$.Value", false, String.class)
//                .to("spring-redis://localhost:6379?redisTemplate=#redisTemplate")
//                .transform().simple("${header.CamelRedis.Value}")
//                .setId("SetToRedis");
//
//        // mongodb 读测试
//        from("rest:get:mongo:/{_id}")
//                .setBody(header("_id"))
//                .convertBodyTo(ObjectId.class)
////                .setBody(simple("{\"_id\":${header._id}}"))
//                .log("****key**** ${body}")
////                .to("mongodb:mongoClient?database=flowdb&collection=flows&operation=findOneByQuery")
//                .to("mongodb:flowDB?database=flowdb&collection=flows&operation=findById")
//                .marshal().json(true)
//                .setHeader("Content-Type", constant("application/json; charset=UTF-8"))
//                .setId("GetFlowFromMongoDB");
//
//        // mongodb 写测试
//        from("rest:post:mongo")
//                .unmarshal().json(Map.class)
//                .log("**** body **** ${body}")
//                .to("mongodb:flowDB?database=flowdb&collection=flows&operation=save")
//                .setHeader("Content-Type", constant("application/json; charset=UTF-8"))
//                .setId("SaveFlowToMongoDB");
//
//        // mysql 读测试
//        from("rest:get:db:/{id}")
//                .log("****key**** ${header.id}")
//                .setBody(constant("select * from person where person_id = :?id"))
//                .to("jdbc:dataSource?useHeadersAsParameters=true")
//                .setHeader("Content-Type", constant("application/json; charset=UTF-8"))
//                .setId("GetDataFromMySql");
//
//        // mysql 写测试
//        from("rest:post:db")
//                .unmarshal().json(Map.class)
//                .log("**** body **** ${body[personId]} ${body.[personName]}")
//                .setBody(simple("insert into person (person_id, person_name) values(${body[personId]},'${body[personName]}')"))
////                .setBody(simple("insert into person (person_id, person_name) values ( 4, 'Hello')"))
//                .to("jdbc:dataSource")
//                .log("**** body **** ${body}")
//                .setHeader("Content-Type", constant("application/json; charset=UTF-8"))
//                .setId("InsertDataToMySql");
//
////        System.out.println("git://" + path + "/flow-json?operation=clone&branchName=master&remotePath=" + gitUrl + "=${body[payload][tagName]}&username=" + gitUser + "&password=" + gitPwd);
//
//        from("direct:git")
//                .to("exec:sh?args=-c \"rm -rf " + path + "/flow-json\"")
//                .toD("git://" + path + "/flow-json?operation=clone&branchName=master&remotePath=" + gitUrl + "&tagName=${body.payload.tagName}&username=" + gitUser + "&password=" + gitPwd)
//                .setId("PullFromGit");
//
//        from("rest:get:es/{ESIndexName}")
//                .setBody().groovy("return ['query':['match_all': new HashMap()]];")
//                .toD("elasticsearch-rest://docker-cluster?operation=Search&indexName=${header.ESIndexName}")
////                .log("**** body **** ${body}")
//                .marshal().json()
//                .setHeader("Content-Type", constant("application/json; charset=UTF-8"))
//                .setId("SearchElasticsearch");
//
//
//        from("rest:post:es/{ESIndexName}")
//                .unmarshal().json(Map.class)
//                .to("direct:toES")
//                .marshal().json()
//                .setHeader("Content-Type", constant("application/json; charset=UTF-8"));

//        from("direct:toES")
//                .toD("elasticsearch-rest://docker-cluster?operation=Index&indexName=${header.ESIndexName}")
//                .log("**** body **** ${body}")
//                .group("系统流程").description("写 elasticsearch 流程").setId("AddToElasticsearch");

        String uri = "websocket://0.0.0.0:7086/terminal";
        from(uri)
                .log(">>> Message received from WebSocket Client : ${body}")
                .setBody().simple(">> ${body}")
                .to(uri + "?sendToAll=true");

        from("direct:logAppendToTerminal")
                .to(uri + "?sendToAll=true");
    }
}
