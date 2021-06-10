package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ClaimCheckOperation;
import org.apache.camel.model.ProcessorDefinition;

import java.util.Map;

@Slf4j
public class ClaimCheckNode extends FlowNode {

    @Override
    public <T extends ProcessorDefinition<T>>T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        rd.claimCheck(ClaimCheckOperation.valueOf((String) props.get("operation")), (String) props.get("key"));
        log.info("创建 claimCheck 节点");
        return rd;
    }
}
