package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;

import java.util.Map;

import static org.apache.camel.language.constant.ConstantLanguage.constant;
import static org.apache.camel.language.header.HeaderLanguage.header;

@Slf4j
public class SpringRedisNode implements IFlowNode {
    RouteBuilder builder;

    public SpringRedisNode(RouteBuilder builder) {
        this.builder = builder;
    }

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");

        String command = (String) props.get("command");
        Map key = (Map) props.get("key");
        Map value = (Map) props.get("value");
        String redisTemplate = (String) props.get("redisTemplate");

        rd.setHeader("CamelRedis.Key", Exp.create(key));
        if (command.equalsIgnoreCase("SET"))
            rd.setHeader("CamelRedis.Value", Exp.create(value));
        rd.to("spring-redis:localhost:6379?redisTemplate=#" + redisTemplate + "&command=" + command);

        log.info("读写 redis ");
        return rd;
    }
}
