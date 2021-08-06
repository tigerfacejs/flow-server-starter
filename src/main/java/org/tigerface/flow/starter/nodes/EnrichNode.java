package org.tigerface.flow.starter.nodes;

import groovy.lang.GroovyClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.tigerface.flow.starter.service.PluginManager;

import java.util.Map;

@Slf4j
public class EnrichNode extends FlowNode {

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String uri = (String) props.get("uri");
        String script = (String)(props.get("aggregationStrategy") != null ? props.get("aggregationStrategy") : props.get("script"));

        if (script != null && script.length() > 0) {
            final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            final GroovyClassLoader groovyClassLoader = new GroovyClassLoader(tccl);
            Class aggregationStrategyClazz = groovyClassLoader.parseClass(script);

            try {
                Object aggregator = aggregationStrategyClazz.newInstance();
                PluginManager.autowireBean(aggregator);

                rd.enrich(uri, (AggregationStrategy) aggregator);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("解析动态 AggregationStrategy 聚合策略脚本失败");
            }
        } else {
            rd.enrich(uri);
        }
        log.info("创建 enrich 节点");

        return rd;
    }
}
