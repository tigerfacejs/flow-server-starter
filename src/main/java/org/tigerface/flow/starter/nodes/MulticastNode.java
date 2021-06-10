package org.tigerface.flow.starter.nodes;

import groovy.lang.GroovyClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.MulticastDefinition;
import org.apache.camel.model.ProcessorDefinition;

import java.util.List;
import java.util.Map;

/*
    {
        "type": "multicast",
        "props": {
            "timeout": "500",
            "multicast": ["direct:a", "direct:b", "direct:c"]
        }
    }
*/
@Slf4j
public class MulticastNode extends FlowNode {

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String timeout = (String) props.get("timeout");
        List<String> multicast = (List<String>) props.get("multicast");

        if (multicast != null && multicast.size() > 0) {
            MulticastDefinition md;
            String script = (String) (props.get("aggregationStrategy") != null ? props.get("aggregationStrategy") : props.get("script"));

            if (script != null && script.length() > 0) {
                final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
                final GroovyClassLoader groovyClassLoader = new GroovyClassLoader(tccl);
                Class aggregationStrategyClazz = groovyClassLoader.parseClass(script);

                try {
                    Object aggregator = aggregationStrategyClazz.newInstance();
                    md = rd.multicast((AggregationStrategy) aggregator);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("解析动态 AggregationStrategy 聚合策略脚本失败");
                }
            } else {
                md = rd.multicast();
            }

            md.stopOnException();
            md.parallelProcessing();
            md.timeout(timeout != null ? Long.valueOf(timeout) : 500);

            for (String uri : multicast) {
                md.toD(uri);
            }
        }

        log.info("创建 multicast 节点");
        return rd;
    }
}
