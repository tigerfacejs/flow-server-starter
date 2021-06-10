package org.tigerface.flow.starter.nodes;

import groovy.lang.GroovyClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;

import java.util.Map;

import static org.apache.camel.builder.Builder.method;

@Slf4j
public class DynamicRouterNode extends FlowNode {

    @Override
    public <T extends ProcessorDefinition<T>>T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String script = (String) (props.get("dynamicRouter") != null ? props.get("dynamicRouter") : props.get("script"));

        if (script != null && script.length() > 0) {
            final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            final GroovyClassLoader groovyClassLoader = new GroovyClassLoader(tccl);
            Class dynamicRouterClazz = groovyClassLoader.parseClass(script);

            try {
                rd.dynamicRouter(method(dynamicRouterClazz, "router"));
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("解析 DynamicRouter Bean 脚本失败");
            }
        } else throw new RuntimeException("DynamicRouterNode 必须是 Bean ，包含名为 router 的方法，且缺省返回 null");

        log.info("创建 dynamicRouter 节点");

        return rd;
    }
}
