package org.tigerface.flow.starter.nodes;

import groovy.lang.GroovyClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Expression;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;

import java.util.Map;

@Slf4j
public class AggregateNode implements IFlowNode {
    RouteBuilder builder;

    public AggregateNode(RouteBuilder builder) {
        this.builder = builder;
    }

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String aggregationStrategy = (String) props.get("aggregationStrategy");

        Expression correlationExp = Exp.create((Map) props.get("correlation"));
        Predicate completionPredicate = PredicateExp.create((Map) props.get("completion"));

        if (aggregationStrategy != null && aggregationStrategy.length() > 0) {
            final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            final GroovyClassLoader groovyClassLoader = new GroovyClassLoader(tccl);
            Class aggregationStrategyClazz = groovyClassLoader.parseClass(aggregationStrategy);

            try {
                Object aggregator = aggregationStrategyClazz.newInstance();
                rd.aggregate(correlationExp, (AggregationStrategy) aggregator).completion(completionPredicate);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("解析动态 AggregationStrategy 聚合策略脚本失败");
            }
        } else {
            rd.aggregate(correlationExp).completion(completionPredicate);
        }

        log.info("创建 aggregate 节点");

        return rd;
    }
}
