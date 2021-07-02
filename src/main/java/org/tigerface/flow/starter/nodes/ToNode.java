package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;

import java.util.Map;

@Slf4j
public class ToNode extends FlowNode {

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String uri = (String) props.get("uri");

//        rd.process(new Processor() {
//            @Override
//            public void process(Exchange exchange) throws Exception {
//                Object body = exchange.getMessage().getBody();
//                System.out.println("LOG TO >>> uri = " + uri + ", body class = " + body.getClass().getName());
//            }
//        });

        rd.toD(uri);
        log.info("创建 to 节点");
        return rd;
    }
}
