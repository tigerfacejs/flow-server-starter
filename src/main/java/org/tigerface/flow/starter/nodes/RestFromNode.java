package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.tigerface.flow.starter.domain.Flow;

import java.util.Map;

@Slf4j
public class RestFromNode implements IFlowNode {
    RouteBuilder builder;

    public RestFromNode(RouteBuilder builder) {
        this.builder = builder;
    }

    @Override
    public <T extends ProcessorDefinition<T>>T createAndAppend(Map<String, Object> node, T rd) {
        Flow flow = (Flow) node.get("flow");
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String uri = "rest:"+(String)props.get("method")+":"+(String)props.get("path");
        ProcessorDefinition newRouteDef = this.builder.from(uri);

        if (flow.getKey() != null) newRouteDef.routeId(flow.getKey());
        else throw new RuntimeException("流程定义缺省关键属性：key");

        if (flow.getGroup() != null)
            newRouteDef.routeGroup(flow.getGroup());
        else throw new RuntimeException("流程定义缺省关键属性：group");

        if (flow.getJson() != null)
            newRouteDef.routeDescription(flow.getJson());
        else throw new RuntimeException("流程定义缺省关键属性：json");

        log.info("识别 rest from 节点");
        return (T)newRouteDef;
    }
}
