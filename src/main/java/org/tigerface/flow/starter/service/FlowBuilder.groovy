package org.tigerface.flow.starter.service

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.model.RouteDefinition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.tigerface.flow.starter.domain.Flow

import java.text.SimpleDateFormat

/**
 * 流解析器
 */

@Slf4j
class FlowBuilder implements ApplicationContextAware {

    @Autowired
    ApplicationContext applicationContext

    Flow parse(String flowJson) {
        Flow flow = null;
        try {
            flow = new JsonSlurper().parseText(flowJson) as Flow;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return flow;
    }

    RouteBuilder build(Flow flow) {
        log.info("创建流程 \n{}", flow);
        if (flow.nodes.size() < 1) throw new Exception("空流程")
        def builder = new RouteBuilder() {
            @Override
            void configure() throws Exception {
                FlowNodeFactory factory = new FlowNodeFactory(this);
                RouteDefinition rd;
                for (def node : flow.nodes) {
                    node.flow = flow;
                    node.type = node.type ? node.type : node.eip;
                    rd = factory.createAndAppend(node, rd);
                }
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
                        def body = exchange.getIn().getBody();
                        def bodyJson = (body instanceof String || body instanceof byte[]) ? body : JsonOutput.toJson(body);
                        exchange.getIn().setBody(['flowId': flow.getKey(), 'requestTime': new SimpleDateFormat('yyyy-MM-dd hh:mm:ss.S').format(now), 'data': bodyJson]);
                        exchange.getIn().setHeader("ESIndexName", "flowlog");
                    }
                })
                .end();
    }
}
