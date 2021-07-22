package org.tigerface.flow.starter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.tigerface.flow.starter.domain.Flow;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 流解析器
 */

@Slf4j
public class FlowBuilder implements ApplicationContextAware {
    @Value("${flow.server}")
    private String currentServer;

    @Autowired
    ApplicationContext applicationContext;

    Flow parse(String flowJson) {
        Flow flow = null;
        try {
//            flow = new JsonSlurper().parseText(flowJson) as Flow;
            flow = new ObjectMapper().readValue(flowJson, Flow.class);
            flow.setCurrentServer(this.currentServer);
        } catch (Exception e) {
            log.error("解析流程时发生异常\n{}", e.getMessage(), e);
//            e.printStackTrace();
        }
        return flow;
    }

    public RouteBuilder build(Flow flow) throws Exception {
        log.debug("创建流程 \n{}", flow);
        if (flow.getNodes().size() < 1) throw new Exception("空流程");
        RouteBuilder builder = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                FlowNodeFactory factory = new FlowNodeFactory(this);
                RouteDefinition rd = null;
                boolean timerStarted = false;
                for (Map node : flow.getNodes()) {
                    node.put("flow", flow);
                    if (flow.getMicrometerId() != null && flow.getMicrometerId().length() > 0 && rd != null && !timerStarted) {
                        rd.to("micrometer:counter:" + flow.getMicrometerId()+"_counter");
                        rd.to("micrometer:timer:" + flow.getMicrometerId()+"_timer?action=start");
                        timerStarted = true;
                    }
                    rd = factory.createAndAppend(node, rd);
                }
                if(timerStarted) {
                    rd.to("micrometer:timer:" + flow.getMicrometerId()+"_timer?action=stop");
                }
                rd.routeId(flow.getRouteId());
                rd.routeDescription(flow.getJson());
                rd.group(flow.getGroup());
            }
        };
        return builder;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
