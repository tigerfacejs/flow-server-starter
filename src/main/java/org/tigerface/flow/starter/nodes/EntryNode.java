package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.model.ProcessorDefinition;

import java.util.Map;

@Slf4j
public abstract class EntryNode extends FlowNode {
    public abstract String getUri(Map node);
    public abstract <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node);
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        return this.createAndAppend(node);
    }
}
