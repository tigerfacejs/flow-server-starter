package org.tigerface.flow.starter.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonDataFormat;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.spi.RestConfiguration;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

public class RestRoute extends RouteBuilder {
    private int port = 8086;

    @Value("${flow.git.tmpdir}")
    private String path;

    @Value("${flow.git.url}")
    private String gitUrl;

    @Value("${flow.git.username}")
    private String gitUser;

    @Value("${flow.git.password}")
    private String gitPwd;

    @Override
    public void configure() throws Exception {
        restConfiguration()
                .port(port)
                .enableCORS(true)
                .corsAllowCredentials(true)
                .bindingMode(RestBindingMode.off)
                .corsHeaderProperty("Access-Control-Allow-Origin", "*")
                .corsHeaderProperty("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, Authorization");

        // 基础流程，系统基本状态
        from("rest:get:who").transform().simple("这是一个 Flow Server，你可以通过 POST ..:" + port + "/deploy 来部署一个流程。")
                .setHeader("Content-Type", constant("application/json; charset=UTF-8")).setId("Who");

        // 推流程入口，仅用于测试部署流程
        from("rest:post:deploy").to("direct:deploy").description("热部署测试").setId("HotDeploy");

        from("file://" + path + "?charset=utf-8&recursive=true&delete=true&include=.*\\.json$&exclude=package.json")
                .log("---deploy-- \n${body}")
                .transform().method("deployService", "deploy")
                .description("文件部署流程")
                .setId("DeployFlowFromPath");

        // 部署主流程
        from("direct:deploy").log("---deploy-- \n${body}").bean("deployService", "deploy").description("内部部署流程").setId("DeployFlow");

        // mq 发布测试
        from("rest:post:mq")
                .convertBodyTo(String.class)
                .log("**** body **** ${body}")
                .to("spring-rabbitmq:default?routingKey=flow")
                .setHeader("Content-Type", constant("application/json; charset=UTF-8"))
                .setId("SendHeartBeat");

        // mq 订阅测试
        from("spring-rabbitmq:flow.topic?queues=flow-cmd&routingKey=command")
                .unmarshal().json(Map.class)
                .log("**** from mq **** ${body}")
                .to("direct:git")
                .setBody(constant("{\"msg\":\"OK\"}"))
                .setId("SubScribeCommandQueue");

        // redis 读测试
        rest().get("/redis?key={a}").route()
                .setHeader("CamelRedis.Command", constant("GET"))
                .setHeader("CamelRedis.Key", header("key"))
                .log("****key**** ${CamelRedis.Key}")
                .to("spring-redis:localhost:6379?redisTemplate=#redisTemplate")
                .log("****body**** ${body}")
                .setHeader("Content-Type", constant("text/html; charset=UTF-8"))
                .setId("GetFromRedis");

        // redis 写测试
        rest("/redis").post().consumes("application/json").route().log("******** ${body}")
                .setHeader("CamelRedis.Command", constant("SET"))
                .setHeader("CamelRedis.Key")
                .jsonpath("$.Key", false, String.class)
                .setHeader("CamelRedis.Value")
                .jsonpath("$.Value", false, String.class)
                .to("spring-redis://localhost:6379?redisTemplate=#redisTemplate")
                .transform().simple("${header.CamelRedis.Value}")
                .setId("SetToRedis");

        // mongodb 读测试
        from("rest:get:mongo:/{_id}")
                .setBody(header("_id"))
                .convertBodyTo(ObjectId.class)
//                .setBody(simple("{\"_id\":${header._id}}"))
                .log("****key**** ${body}")
//                .to("mongodb:mongoClient?database=flowdb&collection=flows&operation=findOneByQuery")
                .to("mongodb:flowDB?database=flowdb&collection=flows&operation=findById")
                .marshal().json(true)
                .setHeader("Content-Type", constant("application/json; charset=UTF-8"))
                .setId("GetFlowFromMongoDB");

        // mongodb 写测试
        from("rest:post:mongo")
                .unmarshal().json(Map.class)
                .log("**** body **** ${body}")
                .to("mongodb:flowDB?database=flowdb&collection=flows&operation=save")
                .setHeader("Content-Type", constant("application/json; charset=UTF-8"))
                .setId("SaveFlowToMongoDB");

        // mysql 读测试
        from("rest:get:db:/{id}")
                .log("****key**** ${header.id}")
                .setBody(constant("select * from person where person_id = :?id"))
                .to("jdbc:dataSource?useHeadersAsParameters=true")
                .setHeader("Content-Type", constant("application/json; charset=UTF-8"))
                .setId("GetDataFromMySql");

        // mysql 写测试
        from("rest:post:db")
                .unmarshal().json(Map.class)
                .log("**** body **** ${body[personId]} ${body.[personName]}")
                .setBody(simple("insert into person (person_id, person_name) values(${body[personId]},'${body[personName]}')"))
//                .setBody(simple("insert into person (person_id, person_name) values ( 4, 'Hello')"))
                .to("jdbc:dataSource")
                .log("**** body **** ${body}")
                .setHeader("Content-Type", constant("application/json; charset=UTF-8"))
                .setId("InsertDataToMySql");

//        System.out.println("git://" + path + "/flow-json?operation=clone&branchName=master&remotePath=https://github.com/tigerfacejs/flow-json.git&tagName=${body[payload][tagName]}&username="+gitUser+"&password="+gitPwd);

        from("direct:git")
                .to("exec:sh?args=-c \"rm -rf " + path + "/flow-json\"")
                .to("git://" + path + "/flow-json?operation=clone&branchName=master&remotePath="+gitUrl+"&tagName=${body[payload][tagName]}&username="+gitUser+"&password="+gitPwd)
                .setId("PullFromGit");
    }
}
