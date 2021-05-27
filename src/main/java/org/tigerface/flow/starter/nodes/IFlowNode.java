package org.tigerface.flow.starter.nodes;

import org.apache.camel.model.ProcessorDefinition;

import java.util.Map;

public interface IFlowNode {
    <T extends ProcessorDefinition<T>>T createAndAppend(Map<String, Object> props, T rd);
}
