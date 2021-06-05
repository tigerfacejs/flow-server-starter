package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;

import java.util.Map;

@Slf4j
public class PropertyNode implements IFlowNode {
    RouteBuilder builder;

    public PropertyNode(RouteBuilder builder) {
        this.builder = builder;
    }

    @Override
    public <T extends ProcessorDefinition<T>>T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");

        String property = (String)props.get("property");
        String operation = (String)props.get("operation");

        if("setProperty".equalsIgnoreCase(operation)) {
            rd.setProperty(property, Exp.create(props));
        } else if("removeHeader".equalsIgnoreCase(operation)) {
            rd.removeProperty(property);
        }

        log.info("创建 Property 节点");

        return rd;
    }
}
