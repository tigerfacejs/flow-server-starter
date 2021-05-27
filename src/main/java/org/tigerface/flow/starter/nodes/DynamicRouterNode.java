package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;

import java.util.Map;

@Slf4j
public class DynamicRouterNode implements IFlowNode {
    RouteBuilder builder;

    public DynamicRouterNode(RouteBuilder builder) {
        this.builder = builder;
    }

    @Override
    public <T extends ProcessorDefinition<T>>T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");

        rd.dynamicRouter(Exp.create(props));
        log.info("创建 dynamicRouter 节点");

        return rd;
    }
}
