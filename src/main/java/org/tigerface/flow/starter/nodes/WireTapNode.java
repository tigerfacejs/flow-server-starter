package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.WireTapDefinition;

import java.util.List;
import java.util.Map;

@Slf4j
public class WireTapNode implements IFlowNode {
    RouteBuilder builder;

    public WireTapNode(RouteBuilder builder) {
        this.builder = builder;
    }

    @Override
    public <T extends ProcessorDefinition<T>>T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");

        WireTapDefinition wtd = rd.wireTap((String)props.get("uri"));
        wtd.newExchangeBody(Exp.create((Map<String, Object>) props.get("body")));
        for (Map header : (List<Map>)props.get("headers")) {
            wtd.newExchangeHeader((String)header.get("header"), Exp.create(header));
        }
        log.info("创建 wireTap 节点");

        return rd;
    }
}
