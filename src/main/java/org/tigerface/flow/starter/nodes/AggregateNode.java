package org.tigerface.flow.starter.nodes;

import groovy.lang.GroovyClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Expression;
import org.apache.camel.Predicate;
import org.apache.camel.model.AggregateDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.tigerface.flow.starter.service.FlowNodeFactory;

import java.util.List;
import java.util.Map;

@Slf4j
public class AggregateNode extends FlowNode {

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");

        Map<String, Object> aggregate = (Map<String, Object>) props.get("aggregate");
        String aggregationStrategy = (String) props.get("aggregationStrategy");
        Map<String, Object> completion = (Map<String, Object>) props.get("completion");

        Expression correlationExp = Exp.create(aggregate);
        Predicate completionPredicate = PredicateExp.create(completion);

        AggregateDefinition ad = null;
        if (aggregationStrategy != null && aggregationStrategy.length() > 0) {
            final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            final GroovyClassLoader groovyClassLoader = new GroovyClassLoader(tccl);
            Class aggregationStrategyClazz = groovyClassLoader.parseClass(aggregationStrategy);

            try {
                Object aggregator = aggregationStrategyClazz.newInstance();
                ad = rd.aggregate(correlationExp, (AggregationStrategy) aggregator).completion(completionPredicate);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("解析动态 AggregationStrategy 聚合策略脚本失败");
            }
        } else {
            ad = rd.aggregate(correlationExp).completion(completionPredicate);
        }

        List<Map> nodes = (List<Map>) aggregate.get("nodes");
        if (!nodes.isEmpty()) {
            for (Map sub : nodes) {
                FlowNodeFactory factory = new FlowNodeFactory(builder);
                ad = factory.createAndAppend(sub, ad);
            }
        }

        log.info("创建 aggregate 节点");

        return rd;
    }
}
