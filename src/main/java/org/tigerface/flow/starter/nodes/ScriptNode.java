package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;

import java.util.Map;

@Slf4j
public class ScriptNode extends FlowNode {

    @Override
    public <T extends ProcessorDefinition<T>>T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");

        rd.script(Exp.create(props));
        log.info("创建 script 节点");

        return rd;
    }
}
