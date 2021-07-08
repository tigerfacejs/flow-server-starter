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
public class LoopNode extends FlowNode {

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");

        String type = (String) props.get("type");
        boolean copy = props.get("copy") != null ? "true".equalsIgnoreCase(props.get("copy").toString()) : false;

        Map loop = (Map) props.get("loop");

        LoopDefinition ld;
        if ("loop".equalsIgnoreCase(type)) {
            Expression loopExp = Exp.create(loop);
            ld = rd.loop(loopExp);
        } else if ("doWhile".equalsIgnoreCase(type)) {
            Predicate whileExp = PredicateExp.create(loop);
            ld = rd.loopDoWhile(whileExp);
        } else throw new RuntimeException("不能创建的循环类型 " + type);

        if (copy) ld.copy();
        ld.breakOnShutdown();

        List<Map> nodes = (List<Map>) loop.get("nodes");
        if (!nodes.isEmpty()) {
            for (Map sub : nodes) {
                FlowNodeFactory factory = new FlowNodeFactory(builder);
                ld = (LoopDefinition) factory.createAndAppend(sub, ld);
            }
        }

        ld.end();

        log.info("创建 Loop 节点");

        return rd;
    }
}
