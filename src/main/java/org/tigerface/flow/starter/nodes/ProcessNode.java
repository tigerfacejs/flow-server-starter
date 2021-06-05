package org.tigerface.flow.starter.nodes;

import groovy.lang.GroovyClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;

import java.util.Map;

@Slf4j
public class ProcessNode implements IFlowNode {
    RouteBuilder builder;

    public ProcessNode(RouteBuilder builder) {
        this.builder = builder;
    }

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String script = (String)(props.get("processor") != null ? props.get("processor") : props.get("script"));

        if (script != null && script.length() > 0) {
            final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            final GroovyClassLoader groovyClassLoader = new GroovyClassLoader(tccl);
            Class processorClazz = groovyClassLoader.parseClass(script);

            try {
                Object aggregator = processorClazz.newInstance();
                rd.process((Processor) aggregator);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("解析动态 Processor 脚本失败");
            }
        }
        log.info("创建 process 节点");

        return rd;
    }
}
