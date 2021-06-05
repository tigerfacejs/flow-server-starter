package org.tigerface.flow.starter.route;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.elasticsearch.ElasticsearchComponent;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class RestRoute extends RouteBuilder {
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

    @Value("${flow.elasticsearch.url}")
    private String esUrl;

    @Value("${flow.elasticsearch.username:elastic}")
    private String esUser;

    @Value("${flow.elasticsearch.password}")
    private String esPwd;

    @Override
    public void configure() throws Exception {
        ElasticsearchComponent esComp = new ElasticsearchComponent();
        esComp.setHostAddresses(esUrl);
        esComp.setUser(esUser);
        esComp.setPassword(esPwd);

        camelContext.addComponent("elasticsearch-rest", esComp);
        camelContext.setStreamCaching(true);

        restConfiguration()
                .component("jetty")
                .port(8086)
                .enableCORS(true)
                .corsAllowCredentials(true)
                .bindingMode(RestBindingMode.off);


        // 基础流程，系统基本状态
        rest().get("/").route()
//        from("rest:get:who")
                .transform().simple("这是一个 Flow Server，你可以通过 POST ../deploy 来部署一个流程。")
                .setHeader("Content-Type", constant("application/json; charset=UTF-8"))
                .group("系统流程").description("检查入口").setId("Who");

        // Rest 部署流程入口
        rest().post("/flow").route()
//        from("rest:post:flow")
                .to("direct:deploy")
                .group("系统流程").description("部署流程入口").setId("DeployFlow");

        // Rest 获取流程信息
        rest().get("/flow/{id}").route()
//        from("rest:get:flow/{id}")
                .setBody(header("id"))
                .bean("deployService", "getFlow")
                .marshal().json()
                .setHeader("Content-Type", constant("application/json; charset=UTF-8"))
                .group("系统流程").description("获取流程信息").setId("GetFlowInfo");

        // Rest 列出流程信息
        rest().get("/flows").route()
//        from("rest:get:flows")
//                .setHeader("Access-Control-Allow-Credentials", constant("true"))
//                .setHeader("Access-Control-Allow-Headers", constant("Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers"))
////                .setHeader("Access-Control-Allow-Origin", constant("*"))
//                .setHeader("Access-Control-Allow-Origin", header("Origin"))
//                .setHeader("Access-Control-Allow-Methods", constant("GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS, CONNECT, PATCH"))
//                .setHeader("Access-Control-Max-Age", constant("3600"))
                .bean("deployService", "listFlows")
                .marshal().json()
                .setHeader("Content-Type", constant("application/json; charset=UTF-8"))
                .group("系统流程").description("列出全部流程").setId("ListFlows");

        // Rest 列出流程信息
        rest().get("/subflows").route()
                .bean("deployService", "listDirectFlows")
                .marshal().json()
                .setHeader("Content-Type", constant("application/json; charset=UTF-8"))
                .group("系统流程").description("列出全部直接流程").setId("ListSubFlows");

        // Rest 删除流程入口
        rest().delete("flow/{id}").route()
                .group("系统流程").description("删除流程入口").id("RemoveFlow")
//        from("rest:delete:flow/{id}")
                .setBody(header("id"))
                .log("---remove-- \n${body}")
                .bean("deployService", "remove")
                .marshal().json();


        // 文件部署流程
        from("file://" + path + "?charset=utf-8&recursive=true&delete=true&include=.*\\.json$&exclude=package.json")
                .log("---deploy-- \n${body}")
                .transform().method("deployService", "deploy")
                .routeGroup("系统流程").description("文件部署流程").setId("DeployFlowFromPath");

        // 部署主流程
        from("direct:deploy")
                .group("系统流程").description("部署主流程").id("MainDeployFlow")
                .log("---deploy-- \n${body}")
                .bean("deployService", "deploy")
                .marshal().json();


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

        from("direct:toES")
                .toD("elasticsearch-rest://docker-cluster?operation=Index&indexName=${header.ESIndexName}")
//                .log("**** body **** ${body}")
                .group("系统流程").description("写 elasticsearch 流程").setId("AddToElasticsearch");
    }
}
