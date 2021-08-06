package org.tigerface.flow.starter.nodes;

import groovy.lang.GroovyClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Expression;
import org.apache.camel.model.MulticastDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RecipientListDefinition;
import org.tigerface.flow.starter.service.PluginManager;

import java.util.List;
import java.util.Map;

/*
    {
        "type": "recipientList",
        "props": {
            "stopOnException": "true",
            "recipientList": {
                "lang: "simple",
                "script": "${header.recipientList}"
            }
        }
    }
*/
@Slf4j
public class RecipientListNode extends FlowNode {

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String stopOnException = (String) props.get("stopOnException");
        String parallelProcessing = (String) props.get("parallelProcessing");
        Map<String, Object> recipientList = (Map<String, Object>) props.get("recipientList");
        Expression expression = Exp.create(recipientList);

        RecipientListDefinition rld;
        String script = (String) (props.get("aggregationStrategy") != null ? props.get("aggregationStrategy") : props.get("script"));

        if (script != null && script.length() > 0) {
            final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            final GroovyClassLoader groovyClassLoader = new GroovyClassLoader(tccl);
            Class aggregationStrategyClazz = groovyClassLoader.parseClass(script);

            try {
                Object aggregator = aggregationStrategyClazz.newInstance();
                PluginManager.autowireBean(aggregator);

                rld = rd.recipientList(expression).aggregationStrategy((AggregationStrategy) aggregator);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("解析动态 AggregationStrategy 聚合策略脚本失败");
            }
        } else {
            rld = rd.recipientList(expression);
        }

        if (stopOnException != null && Boolean.valueOf(stopOnException))
            rld.stopOnException();

        if (parallelProcessing != null && Boolean.valueOf(parallelProcessing))
            rld.parallelProcessing();

        log.info("创建 RecipientList 节点");
        return rd;
    }
}
