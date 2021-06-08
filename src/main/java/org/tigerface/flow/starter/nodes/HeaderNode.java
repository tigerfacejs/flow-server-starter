package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;

import java.util.List;
import java.util.Map;

@Slf4j
public class HeaderNode implements IFlowNode {
    RouteBuilder builder;

    public HeaderNode(RouteBuilder builder) {
        this.builder = builder;
    }

    @Override
    public <T extends ProcessorDefinition<T>>T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");

        List<Map> headers = (List<Map>)props.get("headers");

        for(Map group: headers) {
            String header = (String)group.get("header");
            String operation = (String)group.get("operation");

            if("setHeader".equalsIgnoreCase(operation)) {
                rd.setHeader(header, Exp.create(group));
            } else if("removeHeader".equalsIgnoreCase(operation)) {
                rd.removeHeader(header);
            }
        }

        log.info("创建 Header 节点");

        return rd;
    }
}
