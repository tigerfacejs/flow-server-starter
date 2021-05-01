package org.tigerface.flow.starter.config;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

public class RestRoute extends RouteBuilder {
    private int port = 8086;

    @Override
    public void configure() throws Exception {
        restConfiguration()
                .port(port)
                .enableCORS(true)
                .corsAllowCredentials(true)
                .bindingMode(RestBindingMode.off)
                .corsHeaderProperty("Access-Control-Allow-Origin", "*")
                .corsHeaderProperty("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, Authorization");

        from("rest:post:deploy").transform().method("deployService", "deploy").description("热部署流程").setId("HotDeploy");

        from("rest:get:who").transform().simple("这是一个 Flow Server，你可以通过 POST ..:"+port+"/deploy 来部署一个流程。").setHeader("Content-Type", constant("application/json; charset=UTF-8"));
    }
}
