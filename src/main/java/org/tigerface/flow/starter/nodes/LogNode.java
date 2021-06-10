package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;

import java.util.Map;

@Slf4j
public class LogNode extends FlowNode {

    @Override
    public <T extends ProcessorDefinition<T>>T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");

        String message = (String)props.get("message");
        String level = (String)props.get("level");

        if (level!=null) {
            rd.log(LoggingLevel.valueOf(level), message);
        } else {
            rd.log(message);
        }

        log.info("创建 log 节点");

        return rd;
    }
}
