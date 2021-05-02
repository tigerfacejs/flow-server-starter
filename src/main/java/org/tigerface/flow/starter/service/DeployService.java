package org.tigerface.flow.starter.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.tigerface.flow.starter.domain.Flow;
import org.tigerface.flow.starter.domain.FlowStatus;

@Slf4j
public class DeployService {
    @Autowired
    CamelContext camelContext;

    @Autowired
    FlowBuilder flowBuilder;

    /**
     * 部署一个 Flow
     *
     * @param flowJson
     * @return
     * @throws Exception
     */
     /*
      Flow 的 status 属性决定部署为测试版本还是正式版本
      所以在部署前（调用 FlowParser 前）需要先设置 flow.status
      如果是草稿或测试，则部署为测试；如果是正式，则按版本部署

      rest 入口规则:
      rest:{method}:test:{path} (最后部署的测试版)
      rest:{method}:{path}（路由至最新版本，每次部署新版，会自动修改）
      rest:{method}:v{version}:{path} （实际部署版本）

      direct 入口规则：
      direct:{flowId}_test (最后部署的测试版)
      direct:{flowId}（路由至最新版本，每次部署新版，会自动修改）
      direct:{flowId}_v{version} （实际部署版本）
     */
    boolean deploy(String flowJson) throws Exception {
        log.info("开始部署");
        // entry 是 route 的唯一入口，也就是 from，最基本的是 rest 和 direct
        Flow flow = flowBuilder.parse(flowJson);
        String entry = flow.getEntry().replaceAll(":", "_");
        String fullEntry = flow.getFullEntry().replaceAll(":", "_");
        String idEntry = flow.getIDEntry().replaceAll(":", "_");
        String fullIDEntry = flow.getFullIDEntry().replaceAll(":", "_");

        // 清理无版本 direct
        if (camelContext.getRoute(idEntry) != null) {
            camelContext.removeRoute(idEntry);
        }

        // 清理带版本 direct
        if (camelContext.getRoute(fullIDEntry) != null) {
            camelContext.removeRoute(fullIDEntry);
        }

        // 清理无版本 rest
        if (camelContext.getRoute(entry) != null) {
            camelContext.removeRoute(entry);
        }

        // 清理带版本 rest
        if (camelContext.getRoute(fullEntry) != null) {
            camelContext.removeRoute(fullEntry);
        }

        // 部署 direct
        camelContext.addRoutes(flowBuilder.build(flow));

        // 如果不是 direct，添加定义的入口，指向 direct
        if (entry.startsWith("rest")) {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() {
                    from(flow.getEntry()).setHeader("Access-Control-Allow-Origin",constant("*")).to(flow.getFullIDEntry()).description(flow.getDesc()).setId(entry);
                    from(flow.getFullEntry()).setHeader("Access-Control-Allow-Origin",constant("*")).to(flow.getFullIDEntry()).description(flow.getDesc()).setId(fullEntry);
                }
            });
        }

        if (entry.startsWith("direct") && flow.getStatus().equals(FlowStatus.published.toString())) {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() {
                    from(flow.getEntry()).to(flow.getFullIDEntry()).description(flow.getDesc()).setId(entry);
                }
            });
        }


        return true;
    }
}
