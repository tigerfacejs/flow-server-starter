package org.tigerface.flow.starter.service

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.model.RouteDefinition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.tigerface.flow.starter.domain.Flow

import java.text.SimpleDateFormat

/**
 * 流解析器
 */

@Slf4j
class FlowBuilder implements ApplicationContextAware {
    @Value('${flow.server}')
    private String currentServer;

    @Autowired
    ApplicationContext applicationContext

    Flow parse(String flowJson) {
        Flow flow = null;
        try {
            flow = new JsonSlurper().parseText(flowJson) as Flow;
            flow.setCurrentServer(this.currentServer);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return flow;
    }

    RouteBuilder build(Flow flow) {
        log.debug("创建流程 \n{}", flow);
        if (flow.nodes.size() < 1) throw new Exception("空流程")
        def builder = new RouteBuilder() {
            @Override
            void configure() throws Exception {
                FlowNodeFactory factory = new FlowNodeFactory(this);
                RouteDefinition rd;
                for (def node : flow.nodes) {
                    node.flow = flow;
                    // 不再兼容 node.eip，改用 node.type
//                    node.type = node.type ? node.type : node.eip;
                    rd = factory.createAndAppend(node, rd);
                }
                rd.routeId(flow.getRouteId());
                rd.routeDescription(flow.getJson());
                rd.group(flow.getGroup());
            }
        }
        return builder;
    }

    private toESLog(RouteDefinition rd, flow) {
        def now = new Date();
        rd.wireTap("direct:toES")
//                .newExchangeHeader("ESIndexName", constant("flowlog"))
//                .newExchangeBody(ExpressionBuilder.languageExpression("groovy",
//                                "return ['flowId': '${flow.getKey()}', 'requestTime': '${new SimpleDateFormat('yyyy-MM-dd hh:mm:ss.S').format(now)}', 'data': ${data}]"))
                .newExchange(new Processor() {
                    @Override
                    void process(Exchange exchange) throws Exception {
                        def headers = exchange.getIn().getHeaders();
                        def body = exchange.getIn().getBody();
//                        def bodyJson = (body instanceof String || body instanceof byte[]) ? body : JsonOutput.toJson(body);
                        def data = [
                                'flowId'     : flow.getKey(),
                                'requestTime': new SimpleDateFormat('yyyy-MM-dd hh:mm:ss.S').format(now),
                                'headers'    : headers,
                                'body'       : body
                        ];
                        exchange.getIn().setBody(JsonOutput.toJson(body));
                        exchange.getIn().setHeader("ESIndexName", "flowlog");
                    }
                })
                .end();
    }
}
