package org.tigerface.flow.starter.nodes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Predicate;
import org.apache.camel.model.FilterDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.tigerface.flow.starter.service.FlowNodeFactory;

import java.util.List;
import java.util.Map;

@Slf4j
public class FilterNode extends FlowNode {

    @Override
    public <T extends ProcessorDefinition<T>>T createAndAppend(Map<String, Object> node, T rd) {
        Map<String, Object> props = (Map<String, Object>) node.get("props");
        Map filter = (Map) props.get("filter");

        Predicate exp = PredicateExp.create(filter);
        FilterDefinition fd = rd.filter(exp);

        List<Map> nodes = (List<Map>) filter.get("nodes");
        if (!nodes.isEmpty()) {
            for (Map sub : nodes) {
                FlowNodeFactory factory = new FlowNodeFactory(builder);
                fd = (FilterDefinition) factory.createAndAppend(sub, fd);
            }
        }

        log.info("创建 filter 节点");
        return rd;
    }
}
