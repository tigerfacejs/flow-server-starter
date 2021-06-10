package org.tigerface.flow.starter.nodes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;

import java.util.Map;

public abstract class FlowNode {
    protected RouteBuilder builder;
    public static boolean isEntry() {
        return false;
    }

    public void setBuilder(RouteBuilder builder) {
        this.builder = builder;
    }

    public abstract <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd);
}
