package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;

import java.util.Map;

import static org.apache.camel.language.constant.ConstantLanguage.constant;
import static org.apache.camel.language.header.HeaderLanguage.header;

@Slf4j
public class SpringRedisNode extends FlowNode {

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");

        String command = (String) props.get("command");
        Map key = (Map) props.get("key");
        Map value = (Map) props.get("value");
        Map expire = (Map) props.get("expire");

        String redisTemplate = (String) props.get("redisTemplate");

        rd.setHeader("CamelRedis.Key", Exp.create(key));
        if (command.equalsIgnoreCase("SET")) {
            rd.setHeader("CamelRedis.Value", Exp.create(value));
            rd.to("spring-redis:localhost:6379?redisTemplate=#" + redisTemplate + "&command=" + command);
            if(expire.get("script")!=null && ((String)expire.get("script")).length()>0) {
                rd.setHeader("CamelRedis.Timeout", Exp.create(expire));
                rd.to("spring-redis:localhost:6379?redisTemplate=#" + redisTemplate + "&command=EXPIRE");
            }
        } else {
            // GET or DEL
            rd.to("spring-redis:localhost:6379?redisTemplate=#" + redisTemplate + "&command=" + command);
        }

        log.info("读写 redis ");
        return rd;
    }
}
