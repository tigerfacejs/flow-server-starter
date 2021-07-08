package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.WireTapDefinition;

import java.util.List;
import java.util.Map;

@Slf4j
public class WireTapNode extends FlowNode {

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        Object copy = props.get("copy");

        WireTapDefinition wtd = rd.wireTap((String) props.get("uri"));
        if (copy != null && copy.toString().equalsIgnoreCase("false")) {
            wtd = wtd.copy(false);
        } else {
            wtd = wtd.copy(true);
        }

        Map body = (Map<String, Object>) props.get("body");
        if (body != null && body.get("script") != null && body.get("script").toString().length() > 0) {
            wtd = wtd.newExchangeBody(Exp.create(body));
        }
        for (Map header : (List<Map>) props.get("headers")) {
            if (header != null && header.get("script") != null && header.get("script").toString().length() > 0) {
                wtd = wtd.newExchangeHeader((String) header.get("header"), Exp.create(header));
            }
        }
        log.info("创建 wireTap 节点");

        return rd;
    }
}
