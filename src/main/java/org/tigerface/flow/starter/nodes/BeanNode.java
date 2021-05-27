package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;

import java.util.Map;

@Slf4j
public class BeanNode implements IFlowNode {
    RouteBuilder builder;

    public BeanNode(RouteBuilder builder) {
        this.builder = builder;
    }

    @Override
    public <T extends ProcessorDefinition<T>>T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        rd.bean(props.get("bean"), (String)props.get("method"));
        log.info("创建 bean 节点");
        return (T) rd;
    }
}
