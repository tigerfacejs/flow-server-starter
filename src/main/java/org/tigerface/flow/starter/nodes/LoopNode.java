package org.tigerface.flow.starter.nodes;

import groovy.lang.GroovyClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Expression;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.LoopDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.SplitDefinition;
import org.tigerface.flow.starter.service.FlowNodeFactory;

import java.util.List;
import java.util.Map;

@Slf4j
public class LoopNode implements IFlowNode {
    RouteBuilder builder;

    public LoopNode(RouteBuilder builder) {
        this.builder = builder;
    }

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");

        String type = (String) props.get("type");

        Map loop = (Map) props.get("loop");

        LoopDefinition ld;
        if ("loop".equalsIgnoreCase(type)) {
            Expression loopExp = Exp.create(loop);
            ld = rd.loop(loopExp);
        } else if ("doWhile".equalsIgnoreCase(type)) {
            Predicate whileExp = PredicateExp.create(loop);
            ld = rd.loopDoWhile(whileExp);
        } else throw new RuntimeException("不能识别的循环类型 " + type);

        List<Map> nodes = (List<Map>) loop.get("nodes");
        if (!nodes.isEmpty()) {
            for (Map sub : nodes) {
                FlowNodeFactory factory = new FlowNodeFactory(builder);
                ld = (LoopDefinition) factory.createAndAppend(sub, ld);
            }
        }

        log.info("创建 Loop 节点");

        return rd;
    }
}
