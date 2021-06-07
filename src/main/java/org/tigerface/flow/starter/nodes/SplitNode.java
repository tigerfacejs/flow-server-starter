package org.tigerface.flow.starter.nodes;

import groovy.lang.GroovyClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Expression;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.SplitDefinition;
import org.tigerface.flow.starter.service.FlowNodeFactory;

import java.util.List;
import java.util.Map;

@Slf4j
public class SplitNode implements IFlowNode {
    RouteBuilder builder;

    public SplitNode(RouteBuilder builder) {
        this.builder = builder;
    }

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");

        String aggregationStrategy = (String) props.get("aggregationStrategy");
        Map split = (Map) props.get("split");
        Expression splitExp = Exp.create(split);

        SplitDefinition sd;

        if (aggregationStrategy != null && aggregationStrategy.length() > 0) {
            final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            final GroovyClassLoader groovyClassLoader = new GroovyClassLoader(tccl);
            Class aggregationStrategyClazz = groovyClassLoader.parseClass(aggregationStrategy);

            try {
                Object aggregator = aggregationStrategyClazz.newInstance();
                sd = rd.split(splitExp, (AggregationStrategy) aggregator);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("解析动态 AggregationStrategy 聚合策略脚本失败");
            }
        } else {
            sd = rd.split(splitExp);
        }

        List<Map> nodes = (List<Map>) split.get("nodes");
        if (!nodes.isEmpty()) {
            for (Map sub : nodes) {
                FlowNodeFactory factory = new FlowNodeFactory(builder);
                sd = (SplitDefinition) factory.createAndAppend(sub, sd);
            }
        }

        log.info("创建 Split 节点");

        return rd;
    }
}
