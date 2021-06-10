package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;

import java.util.Map;

@Slf4j
public class CorsNode extends FlowNode {

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        if (((String) props.get("credentials")).equalsIgnoreCase("true"))
            rd.setHeader("Access-Control-Allow-Credentials", builder.constant("true"));
        rd.setHeader("Access-Control-Allow-Headers", builder.constant(props.get("headers")));
        rd.setHeader("Access-Control-Allow-Origin", builder.header((String) props.get("origin")));
        rd.setHeader("Access-Control-Allow-Methods", builder.constant(props.get("methods")));
        rd.setHeader("Access-Control-Max-Age", builder.constant(props.get("maxAge")));
        log.info("创建 cors 节点");
        return rd;
    }
}
