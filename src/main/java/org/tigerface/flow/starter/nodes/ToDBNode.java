package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;

import java.util.Map;

@Slf4j
public class ToDBNode extends FlowNode {

    @Override
    public <T extends ProcessorDefinition<T>> T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        String dataSource = (String) props.get("dataSource");
        Map sql = (Map) props.get("sql");

        rd.setBody(Exp.create(sql));
        rd.to("jdbc:" + dataSource);

        log.info("创建 toDB 节点");
        return rd;
    }
}
